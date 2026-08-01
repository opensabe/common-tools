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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Child-first class loader for conflicting JARs (e.g. an older Fastjson stack).
 * Parent is the platform loader so application Fastjson is not visible here.
 */
public final class IsolatedJarClassLoader extends URLClassLoader {

    public IsolatedJarClassLoader(URL... jars) {
        super(jars, ClassLoader.getPlatformClassLoader());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded != null) {
                return resolveIfNeeded(loaded, resolve);
            }
            if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.")
                    || name.startsWith("sun.") || name.startsWith("jdk.internal.")) {
                return resolveIfNeeded(super.loadClass(name, false), resolve);
            }
            try {
                return resolveIfNeeded(findClass(name), resolve);
            } catch (ClassNotFoundException ignored) {
                return resolveIfNeeded(super.loadClass(name, false), resolve);
            }
        }
    }

    private Class<?> resolveIfNeeded(Class<?> clazz, boolean resolve) {
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
