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
package io.github.opensabe.common.webflux.mybatis.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Import;

import io.github.opensabe.common.idgenerator.autoconfig.IdGeneratorAutoConfiguration;
import io.github.opensabe.common.mybatis.autoconf.MyBatisAutoConfiguration;
import io.github.opensabe.common.mybatis.configuration.WebInterceptorConfiguration;
import io.github.opensabe.common.redisson.autoconfig.LettuceAutoConfiguration;
import io.github.opensabe.common.redisson.autoconfig.MultiRedisAutoConfiguration;
import io.github.opensabe.common.redisson.autoconfig.RedissonAutoConfiguration;
import io.github.opensabe.common.s3.autoconf.AwsS3AutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;

/**
 * 仅用于 {@link WebFluxRoutingContextIntegrationTest}：只加载 WebFlux 与
 * {@link WebInterceptorConfiguration} 中的 operId / {@code WebFluxRoutingContext} 相关 Bean，不启动完整 MyBatis。
 * <p>
 * 显式排除类路径上被 {@code common-utils} 等引入的 MyBatis/Redis/Redisson 自动配置，避免本 IT 需要真实 Redis。
 */
@SpringBootApplication(
        scanBasePackages = "io.github.opensabe.common.webflux.mybatis.test",
        exclude = {
                MyBatisAutoConfiguration.class,
                RedissonAutoConfiguration.class,
                MultiRedisAutoConfiguration.class,
                LettuceAutoConfiguration.class,
                RedissonAutoConfigurationV2.class,
                RedisAutoConfiguration.class,
                RedisRepositoriesAutoConfiguration.class,
                IdGeneratorAutoConfiguration.class,
                AwsS3AutoConfiguration.class
        }
)
@Import(WebInterceptorConfiguration.class)
public class WebFluxRoutingContextTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebFluxRoutingContextTestApplication.class, args);
    }
}
