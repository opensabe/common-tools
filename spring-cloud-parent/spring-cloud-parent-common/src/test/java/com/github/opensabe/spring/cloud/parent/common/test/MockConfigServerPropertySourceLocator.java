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
package com.github.opensabe.spring.cloud.parent.common.test;

import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockConfigServerPropertySourceLocator implements PropertySourceLocator {

    private static final Map<String, Object> properties = new ConcurrentHashMap<>();

    @Override
    public PropertySource<?> locate(Environment environment) {
        return new MapPropertySource("secretPropertySource", properties);
    }

    static void put(String key, String value) {
        properties.put(key, value);
    }
}
