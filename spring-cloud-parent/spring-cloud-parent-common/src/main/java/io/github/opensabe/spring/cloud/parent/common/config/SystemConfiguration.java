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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.spring.cloud.parent.common.system.MonitorMemoryRSS;
import lombok.extern.log4j.Log4j2;

/**
 * 系统级 Bean 配置。
 * <p>
 * 注册内存 RSS 定时采集与 Secret 属性脱敏 Provider。
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class SystemConfiguration {

    /**
     * 注册内存 RSS 与 cgroup 指标采集监听器。
     *
     * @return 内存监控 Bean
     */
    @Bean
    public MonitorMemoryRSS getMonitorMemoryRSS() {
        return new MonitorMemoryRSS();
    }

    /**
     * 注册 Secret 属性源脱敏 Provider。
     *
     * @param globalSecretManager 全局密钥管理器
     * @return Secret 属性源 Provider
     */
    @Bean
    public SecretPropertySourceProvider secretPropertySourceProvider (GlobalSecretManager globalSecretManager) {
        return new SecretPropertySourceProvider(globalSecretManager);
    }
}
