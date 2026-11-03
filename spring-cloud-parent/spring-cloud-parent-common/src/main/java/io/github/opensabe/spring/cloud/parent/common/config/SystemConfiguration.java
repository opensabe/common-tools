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
package io.github.opensabe.spring.cloud.parent.common.config;

import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.spring.cloud.parent.common.secret.SecretPropertySourceProvider;
import io.github.opensabe.spring.cloud.parent.common.system.MonitorMemoryRSS;
import io.github.opensabe.spring.cloud.parent.common.system.MonitorProcessList;
import io.github.opensabe.spring.cloud.parent.common.system.ProcessListMonitorProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProcessListMonitorProperties.class)
public class SystemConfiguration {
    @Bean
    public MonitorMemoryRSS getMonitorMemoryRSS() {
        return new MonitorMemoryRSS();
    }

    @Bean
    @ConditionalOnProperty(prefix = "opensabe.process-monitor", name = "enabled", havingValue = "true")
    public MonitorProcessList monitorProcessList(ProcessListMonitorProperties properties) {
        return new MonitorProcessList(properties);
    }

    @Bean
    public SecretPropertySourceProvider secretPropertySourceProvider(GlobalSecretManager globalSecretManager) {
        return new SecretPropertySourceProvider(globalSecretManager);
    }
}
