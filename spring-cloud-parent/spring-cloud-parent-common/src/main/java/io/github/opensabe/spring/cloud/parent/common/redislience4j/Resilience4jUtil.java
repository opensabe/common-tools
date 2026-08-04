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
package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * Resilience4j 组件命名与 ID 生成工具。
 * <p>
 * 按 host:port 及可选路径/方法生成 CircuitBreaker 等组件的唯一标识。
 */
public class Resilience4jUtil {
    private static final Pattern ID = Pattern.compile("[0-9]{12}");

    /**
     * 从 URL 提取 {@code host:port} 实例标识。
     *
     * @param url 请求 URL
     * @return 实例标识
     */
    public static String getServiceInstance(URL url) {
        return getServiceInstance(url.getHost(), url.getPort());
    }

    /**
     * 拼接 host 与 port 为实例标识。
     *
     * @param host 主机名
     * @param port 端口
     * @return 实例标识
     */
    public static String getServiceInstance(String host, int port) {
        return host + ":" + port;
    }

    /**
     * 生成含方法签名的实例标识。
     *
     * @param url 请求 URL
     * @param method 目标方法
     * @return 实例+方法标识
     */
    public static String getServiceInstanceMethodId(URL url, Method method) {
        return getServiceInstance(url) + ":" + method.toGenericString();
    }

    /**
     * 生成含方法签名的实例标识。
     *
     * @param host 主机名
     * @param port 端口
     * @param method 目标方法
     * @return 实例+方法标识
     */
    public static String getServiceInstanceMethodId(String host, int port, Method method) {
        return getServiceInstance(host, port) + ":" + method.toGenericString();
    }

    /**
     * 生成含路径前缀的实例标识；过滤纯数字 ID 段并最多保留三段路径。
     *
     * @param host 主机名
     * @param port 端口
     * @param path 请求路径
     * @return 实例+路径标识
     */
    public static String getServiceInstanceMethodId(String host, int port, String path) {
        String[] split = StringUtils.split(path, "/");
        String suffix = split != null
                ? "/" + Arrays.stream(split).filter(Resilience4jUtil::shouldNotBeIgnored).limit(3).collect(Collectors.joining("/"))
                : "/";
        return getServiceInstance(host, port) + suffix;
    }

    /**
     * 路径段是否为非纯数字 ID（应保留在标识中）。
     *
     * @param split 路径段
     * @return 非纯 12 位数字时返回 {@code true}
     */
    private static boolean shouldNotBeIgnored(String split) {
        if (ID.matcher(split).find()) {
            return false;
        }
        return true;
    }
}
