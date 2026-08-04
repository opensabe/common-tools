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
package io.github.opensabe.spring.cloud.starter.third.client.webclient;

import java.util.Arrays;

import org.springframework.cloud.context.named.NamedContextFactory;

/**
 * ThirdPartyWebClientSpecification 类。
 * <p>Third Party Web Client Specification。</p>
 */
public class ThirdPartyWebClientSpecification implements NamedContextFactory.Specification {

/** name 字段。 */
    private final String name;

/** configuration 字段。 */
    private final Class<?>[] configuration;

    public ThirdPartyWebClientSpecification(String name, Class<?>[] configuration) {
        this.name = name;
        this.configuration = configuration;
    }

/**
 * 返回调试字符串。
 */
    @Override
    public String toString() {
        return "WebClientSpecification{" +
                "name='" + name + '\'' +
                ", configuration=" + Arrays.toString(configuration) +
                '}';
    }

/**
 * getName 方法。
 */
    @Override
    public String getName() {
        return name;
    }

/**
 * getConfiguration 方法。
 */
    @Override
    public Class<?>[] getConfiguration() {
        return configuration;
    }
}
