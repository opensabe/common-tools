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
package io.github.opensabe.scheduler.autoconfig;

import java.util.ArrayList;
import java.util.List;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.github.opensabe.scheduler.autoconfig.health.SchedulerServerHealthIndicator;
import io.github.opensabe.scheduler.conf.Commander;
import io.github.opensabe.scheduler.conf.SchedulerProperties;
import io.github.opensabe.scheduler.health.HealthCheckJob;
import io.github.opensabe.scheduler.health.SimpleJobHealthService;
import io.github.opensabe.scheduler.jfr.JobExecuteObservationToJFRGenerator;
import io.github.opensabe.scheduler.listener.JobListener;
import io.github.opensabe.scheduler.listener.TaskCanRunListener;
import io.github.opensabe.scheduler.server.SchedulerServer;
import io.github.opensabe.scheduler.utils.JobStatisticsAPI;
import io.micrometer.core.instrument.MeterRegistry;

//@Configuration
//https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#changes-to-auto-configuration
/**
 * 调度任务模块 Spring Boot 自动配置。
 * <p>
 * 在 {@code scheduler.job.enable=true} 时注册 Commander、SchedulerServer、
 * 任务监听器、健康检查与 JFR 观测等 Bean。
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "scheduler.job", name = "enable")
@EnableConfigurationProperties(SchedulerProperties.class)
public class SchedulerJobAutoConfiguration {

    /**
     * 调度任务相关配置属性。
     */
    @Autowired
    private SchedulerProperties schedulerProperties;

    /**
     * 注册空的任务监听器列表，供业务侧追加自定义 {@link JobListener}。
     *
     * @return 可变的任务监听器列表
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "scheduler.job", name = "enable", havingValue = "true")
    public List<JobListener> jobListeners() {
        return new ArrayList<>();
    }

    /**
     * 创建并初始化 Redisson 驱动的任务指挥官。
     *
     * @param redissonClient Redisson 客户端
     * @return 已完成 {@code setUp()} 的 Commander 实例
     */
    @Bean(destroyMethod = "closeCommander")
    @ConditionalOnProperty(prefix = "scheduler.job", name = "enable", havingValue = "true")
    public Commander commander(final RedissonClient redissonClient) {
        Commander commander = new Commander(redissonClient, schedulerProperties);
        commander.setUp();
        return commander;
    }

    /**
     * 创建调度服务端，负责拉取与执行任务。
     *
     * @param commander           任务指挥官
     * @param applicationContext  Spring 应用上下文
     * @param environment         运行环境
     * @param jobListeners        任务监听器列表
     * @param redissonClient      Redisson 客户端
     * @param stringRedisTemplate Redis 字符串模板
     * @param meterRegistry       Micrometer 指标注册表
     * @return 调度服务端实例
     */
    @Bean(destroyMethod = "stop")
    @ConditionalOnClass(Commander.class)
    @ConditionalOnProperty(prefix = "scheduler.job", name = "enable", havingValue = "true")
    public SchedulerServer schedulerServer(final Commander commander, final ApplicationContext applicationContext,
                                           final Environment environment, final List<JobListener> jobListeners,
                                           final RedissonClient redissonClient, final StringRedisTemplate stringRedisTemplate, final MeterRegistry meterRegistry) {
        return new SchedulerServer(applicationContext, environment, jobListeners,
                redissonClient, stringRedisTemplate, schedulerProperties, meterRegistry, commander);
//        schedulerServer.start();
    }

    /**
     * 暴露任务统计查询 API。
     *
     * @param schedulerServer 调度服务端
     * @return 任务统计 API
     */
    @Bean
    @ConditionalOnClass(SchedulerServer.class)
    @ConditionalOnProperty(prefix = "scheduler.job", name = "enable", havingValue = "true")
    public JobStatisticsAPI jobStatisticsAPI(final SchedulerServer schedulerServer) {
        return new JobStatisticsAPI(schedulerServer);
    }

    /**
     * 注册任务可运行性监听器。
     *
     * @return 任务可运行性监听器
     */
    @Bean
    public TaskCanRunListener taskCanRunListener() {
        return new TaskCanRunListener();
    }

    /**
     * 注册将任务执行 Observation 写入 JFR 的生成器。
     *
     * @return JFR 观测生成器
     */
    @Bean
    public JobExecuteObservationToJFRGenerator jobExecuteObservationToJFRGenerator() {
        return new JobExecuteObservationToJFRGenerator();
    }

    /**
     * 注册基于 Redis 的简易任务健康检查服务。
     *
     * @param stringRedisTemplate Redis 字符串模板
     * @param schedulerProperties 调度配置
     * @return 简易任务健康服务
     */
    @Bean
    public SimpleJobHealthService simpleJobHealthService(StringRedisTemplate stringRedisTemplate, SchedulerProperties schedulerProperties) {
        return new SimpleJobHealthService(stringRedisTemplate, schedulerProperties);
    }

    /**
     * 注册周期性健康检查任务。
     *
     * @param simpleJobHealthService 简易任务健康服务
     * @return 健康检查任务
     */
    @Bean
    public HealthCheckJob healthCheckJob(SimpleJobHealthService simpleJobHealthService) {
        return new HealthCheckJob(simpleJobHealthService);
    }

    /**
     * 调度服务端健康指示器自动配置，在 Actuator 健康端点启用 {@code schedulerjob} 时生效。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(HealthIndicator.class)
    @ConditionalOnEnabledHealthIndicator("schedulerjob")
    public static class SchedulerServerHealthIndicatorAutoConfiguration {

        /**
         * 注册调度服务端 {@link HealthIndicator}。
         *
         * @param schedulerProperties     调度配置
         * @param schedulerServerProvider 调度服务端可选提供者
         * @return 健康指示器
         */
        @Bean
        public HealthIndicator schedulerJobHealthIndicator(SchedulerProperties schedulerProperties, ObjectProvider<SchedulerServer> schedulerServerProvider) {
            return new SchedulerServerHealthIndicator(schedulerProperties, schedulerServerProvider);
        }
    }
}
