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
package io.github.opensabe.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * properties甯姪绫�
 * 榛樿鍔犺浇config.properties
 */
public class PropertyUtils {
    private static final String CONFIG = "config.properties";
    private static Log log = LogFactory.getLog(PropertyUtils.class);
    private static Map<String, String> configMap = new HashMap<String, String>();

    static {
        load(CONFIG);
    }

    public static String getProperty(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return configMap.get(key);
    }

    public static String getProperty(String key, String defaultValue) {
        if (StringUtils.isEmpty(key)) {
            return (StringUtils.isEmpty(defaultValue) ? null : defaultValue);
        }
        return (StringUtils.isEmpty(configMap.get(key)) ? defaultValue : configMap.get(key));
    }

    public static int getPropertyIntValue(String key, int defaultValue) {
        if (StringUtils.isEmpty(key)) {
            return defaultValue;
        }
        return (StringUtils.isEmpty(configMap.get(key)) || !isInt(configMap.get(key))) ? defaultValue : Integer
                .parseInt(configMap.get(key));
    }

    private static boolean isInt(String n) {
        try {
            Integer.parseInt(n);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @SuppressWarnings("rawtypes")
    private static void load(String name) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        Properties p = new Properties();
        try {
            if (is != null) {
                p.load(is);
            }
            if (CONFIG.equals(name)) {
                for (Map.Entry e : p.entrySet()) {
                    configMap.put((String) e.getKey(), (String) e.getValue());
                }
            }

        } catch (IOException e) {
            log.error("load property file failed. file name: " + name, e);
        }
    }
}