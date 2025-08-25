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
package io.github.opensabe.common.mybatis.properties;


import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.github.opensabe.common.mybatis.properties.CountryProperties.PREFIX;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = PREFIX)
@Getter
@Setter
public class CountryProperties {

    public final static String PREFIX = "country";

    /**
     * key: operId
     * value: country
     */
    private Map<String, String> map;
}
