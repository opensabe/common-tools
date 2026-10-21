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

package io.github.opensabe.common.internalcall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Case-insensitive strip of mechanism headers, then single-value set of a new signature.
 */
public final class InternalCallRequestHeaderSupport {

    private InternalCallRequestHeaderSupport() {
    }

    public static List<String> mechanismHeaderNames(InternalCallProperties properties) {
        InternalCallProperties.Headers h = properties.getHeaders();
        List<String> names = new ArrayList<>();
        names.add(h.getCallerService());
        names.add(h.getCallerIp());
        names.add(h.getTs());
        names.add(h.getNonce());
        names.add(h.getSign());
        names.add(h.getVer());
        if (h.getEdgeToken() != null && !h.getEdgeToken().isBlank()) {
            names.add(h.getEdgeToken());
        }
        return names;
    }

    public static boolean isMechanismHeader(String name, Collection<String> mechanismNames) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        for (String mechanismName : mechanismNames) {
            if (mechanismName != null && mechanismName.toLowerCase(Locale.ROOT).equals(lower)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove configured mechanism headers (case-insensitive) from a mutable header map, then apply signed values.
     */
    public static void stripAndApply(
            Map<String, List<String>> headers,
            InternalCallProperties properties,
            InternalCallSignedHeaders signed
    ) {
        strip(headers, properties);
        if (signed == null) {
            return;
        }
        InternalCallProperties.Headers h = properties.getHeaders();
        headers.put(h.getCallerService(), List.of(signed.callerService()));
        headers.put(h.getCallerIp(), List.of(signed.callerIp()));
        headers.put(h.getTs(), List.of(signed.ts()));
        headers.put(h.getNonce(), List.of(signed.nonce()));
        headers.put(h.getSign(), List.of(signed.sign()));
        headers.put(h.getVer(), List.of(signed.ver()));
    }

    public static void strip(Map<String, List<String>> headers, InternalCallProperties properties) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        List<String> mechanism = mechanismHeaderNames(properties);
        List<String> toRemove = new ArrayList<>();
        for (String key : headers.keySet()) {
            if (isMechanismHeader(key, mechanism)) {
                toRemove.add(key);
            }
        }
        for (String key : toRemove) {
            headers.remove(key);
        }
    }

    /**
     * Consumer-style strip/set for frameworks that expose remove/set APIs (e.g. HttpHeaders).
     */
    public static void stripAndApply(
            BiConsumer<String, String> setHeader,
            Function<String, Boolean> removeHeader,
            InternalCallProperties properties,
            InternalCallSignedHeaders signed
    ) {
        for (String name : mechanismHeaderNames(properties)) {
            removeHeader.apply(name);
        }
        if (signed == null) {
            return;
        }
        InternalCallProperties.Headers h = properties.getHeaders();
        setHeader.accept(h.getCallerService(), signed.callerService());
        setHeader.accept(h.getCallerIp(), signed.callerIp());
        setHeader.accept(h.getTs(), signed.ts());
        setHeader.accept(h.getNonce(), signed.nonce());
        setHeader.accept(h.getSign(), signed.sign());
        setHeader.accept(h.getVer(), signed.ver());
    }

    public static String first(Map<String, ? extends Collection<String>> headers, String name) {
        if (headers == null || name == null) {
            return null;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, ? extends Collection<String>> e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().toLowerCase(Locale.ROOT).equals(lower)) {
                Collection<String> values = e.getValue();
                if (values == null || values.isEmpty()) {
                    return null;
                }
                return values.iterator().next();
            }
        }
        return null;
    }

    /**
     * Copy headers while redacting sign and edge-token values for logging.
     */
    public static Map<String, List<String>> redactForLog(Map<String, ? extends Collection<String>> headers,
                                                         InternalCallProperties properties) {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        if (headers == null) {
            return copy;
        }
        String sign = properties.getHeaders().getSign();
        String edge = properties.getHeaders().getEdgeToken();
        for (Map.Entry<String, ? extends Collection<String>> e : headers.entrySet()) {
            String key = e.getKey();
            if (key == null) {
                continue;
            }
            boolean redact = isMechanismHeader(key, List.of(sign, edge));
            List<String> values = new ArrayList<>();
            if (e.getValue() != null) {
                for (String v : e.getValue()) {
                    values.add(redact ? "***" : v);
                }
            }
            copy.put(key, values);
        }
        return copy;
    }
}
