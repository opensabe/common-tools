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
package io.github.opensabe.common.config;


import com.fasterxml.jackson.databind.Module;
import io.github.opensabe.common.jackson.TimestampModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JacksonCustomizedConfiguration {

    /**
     * 最终还是将module创建到spring容器，因为spi无法保证顺序，jsr310会比我们自定义的后加载，
     * 因此会覆盖掉我们自己的LocalDateTime序列化
     *
     * @return
     */
    @Bean
    public Module timstampModule () {
        return new TimestampModule();
    }
}
