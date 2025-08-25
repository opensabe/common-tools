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
package io.github.opensabe.common.mybatis.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.common.mybatis.jfr.ConnectionJFRGenerator;
import io.github.opensabe.common.mybatis.jfr.SQLExecuteJFRGenerator;
import io.github.opensabe.common.mybatis.monitor.MonitorTransactionAspect;
import io.github.opensabe.common.mybatis.properties.SqlSessionFactoryProperties;
import io.github.opensabe.common.secret.GlobalSecretManager;

@Configuration(proxyBeanMethods = false)
public class MonitorConfiguration {

    @Bean
    public MonitorTransactionAspect monitorTransactionAspect() {
        return new MonitorTransactionAspect();
    }

    /**
     * SQL执行时间监控上报JFR事件
     *
     * @return
     */
    @Bean
    public SQLExecuteJFRGenerator sqlExecutorJFRGenerator() {
        return new SQLExecuteJFRGenerator();
    }

    @Bean
    public ConnectionJFRGenerator connectionJFRGenerator() {
        return new ConnectionJFRGenerator();
    }

    @Bean
    public DatabaseSecretProvider databaseSecretProvider(GlobalSecretManager globalSecretManager, SqlSessionFactoryProperties sqlSessionFactoryProperties) {
        return new DatabaseSecretProvider(globalSecretManager, sqlSessionFactoryProperties);
    }
}
