/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.compat;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import tools.jackson.core.type.TypeReference;

import io.github.opensabe.common.utils.json.JsonUtil;

/**
 * 在 {@link IsolatedJarClassLoader} 中调用旧版 Fastjson；仅 {@link String}/{@code byte[]} 跨类加载器边界。
 */
public final class FastjsonCompatBridge {

    public static final String DEFAULT_OLD_VERSION = "2.0.51";

    private FastjsonCompatBridge() {
    }

    /**
     * Directory containing fastjson / fastjson2 / fastjson2-extension JARs for {@code oldVersion}.
     * Resolves via {@code fastjson.compat.libs.dir}, Maven {@code basedir}, this class's
     * {@code target/test-classes} sibling, then {@code ./target/compat-libs}.
     */
    public static Path requireCompatLibsDir() {
        String version = System.getProperty("fastjson.compat.old.version", DEFAULT_OLD_VERSION);
        Path dir = resolveCompatLibsDir();
        Path facade = dir.resolve("fastjson-" + version + ".jar");
        Path core = dir.resolve("fastjson2-" + version + ".jar");
        Path extension = dir.resolve("fastjson2-extension-" + version + ".jar");
        if (!Files.isRegularFile(facade) || !Files.isRegularFile(core) || !Files.isRegularFile(extension)) {
            throw new AssertionError(
                    "Missing Fastjson compat jars under " + dir.toAbsolutePath()
                            + " (need fastjson / fastjson2 / fastjson2-extension " + version
                            + "); run mvn generate-test-resources (copy-fastjson-compat)");
        }
        return dir;
    }

    static Path resolveCompatLibsDir() {
        String override = System.getProperty("fastjson.compat.libs.dir");
        if (override != null && !override.isBlank()) {
            return Path.of(override).toAbsolutePath().normalize();
        }
        String basedir = System.getProperty("basedir");
        if (basedir != null && !basedir.isBlank()) {
            Path fromBasedir = Path.of(basedir, "target", "compat-libs");
            if (Files.isDirectory(fromBasedir)) {
                return fromBasedir.toAbsolutePath().normalize();
            }
        }
        Path fromCodeSource = compatLibsBesideTestClasses();
        if (fromCodeSource != null) {
            return fromCodeSource;
        }
        return Path.of("target", "compat-libs").toAbsolutePath().normalize();
    }

    private static Path compatLibsBesideTestClasses() {
        try {
            URL location = FastjsonCompatBridge.class.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return null;
            }
            Path testClasses = Path.of(location.toURI());
            Path candidate = testClasses.resolveSibling("compat-libs");
            return Files.isDirectory(candidate) ? candidate.toAbsolutePath().normalize() : null;
        } catch (URISyntaxException | RuntimeException ignored) {
            return null;
        }
    }

    /** @deprecated use {@link #requireCompatLibsDir()} */
    @Deprecated
    public static Path requireOldJar() {
        String version = System.getProperty("fastjson.compat.old.version", DEFAULT_OLD_VERSION);
        return requireCompatLibsDir().resolve("fastjson-" + version + ".jar");
    }

    /**
     * Parse JSON with the isolated Fastjson stack and return that stack's canonical JSON string.
     */
    public static String reencode(Path compatLibsDir, String json) {
        return invoke(compatLibsDir, (jsonClass, jsonObjectClass) -> {
            Object parsed = jsonClass.getMethod("parse", String.class).invoke(null, json);
            return (String) jsonClass.getMethod("toJSONString", Object.class).invoke(null, parsed);
        });
    }

    /**
     * Build JSON from a JDK {@link Map} using the isolated Fastjson stack.
     */
    public static String toJsonFromMap(Path compatLibsDir, Map<String, Object> map) {
        return invoke(compatLibsDir, (jsonClass, jsonObjectClass) -> {
            Object jo = toJsonObject(jsonObjectClass, map);
            return (String) jsonClass.getMethod("toJSONString", Object.class).invoke(null, jo);
        });
    }

    /**
     * Parse JSON with the isolated stack, then materialize a Map on the parent loader via JsonUtil.
     */
    public static Map<String, Object> parseAsMap(Path compatLibsDir, String json) {
        String reencoded = reencode(compatLibsDir, json);
        return JsonUtil.parseObject(reencoded, new TypeReference<LinkedHashMap<String, Object>>() {
        });
    }

    private static Object toJsonObject(Class<?> jsonObjectClass, Map<String, Object> map) throws Exception {
        Object jo = jsonObjectClass.getConstructor().newInstance();
        Method put = jsonObjectClass.getMethod("put", String.class, Object.class);
        for (Map.Entry<String, Object> e : map.entrySet()) {
            put.invoke(jo, e.getKey(), convertValue(jsonObjectClass, e.getValue()));
        }
        return jo;
    }

    @SuppressWarnings("unchecked")
    private static Object convertValue(Class<?> jsonObjectClass, Object value) throws Exception {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Map<?, ?> nested) {
            return toJsonObject(jsonObjectClass, (Map<String, Object>) nested);
        }
        if (value instanceof List<?> list) {
            Class<?> jsonArrayClass = Class.forName("com.alibaba.fastjson.JSONArray", true, jsonObjectClass.getClassLoader());
            Object arr = jsonArrayClass.getConstructor().newInstance();
            Method add = jsonArrayClass.getMethod("add", Object.class);
            for (Object item : list) {
                add.invoke(arr, convertValue(jsonObjectClass, item));
            }
            return arr;
        }
        return String.valueOf(value);
    }

    @FunctionalInterface
    private interface IsolatedAction {
        String run(Class<?> jsonClass, Class<?> jsonObjectClass) throws Exception;
    }

    private static String invoke(Path compatLibsDir, IsolatedAction action) {
        try (IsolatedJarClassLoader cl = new IsolatedJarClassLoader(urlsIn(compatLibsDir))) {
            Class<?> jsonClass = Class.forName("com.alibaba.fastjson.JSON", true, cl);
            Class<?> jsonObjectClass = Class.forName("com.alibaba.fastjson.JSONObject", true, cl);
            return action.run(jsonClass, jsonObjectClass);
        } catch (Exception e) {
            throw new AssertionError("Isolated Fastjson invoke failed for " + compatLibsDir, e);
        }
    }

    private static URL[] urlsIn(Path dir) throws Exception {
        String version = System.getProperty("fastjson.compat.old.version", DEFAULT_OLD_VERSION);
        List<URL> urls = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> {
                String name = p.getFileName().toString();
                return name.endsWith(".jar") && name.contains(version);
            }).sorted().forEach(p -> {
                try {
                    urls.add(p.toUri().toURL());
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
        }
        if (urls.isEmpty()) {
            throw new AssertionError("No jars for version " + version + " in " + dir);
        }
        return urls.toArray(URL[]::new);
    }
}
