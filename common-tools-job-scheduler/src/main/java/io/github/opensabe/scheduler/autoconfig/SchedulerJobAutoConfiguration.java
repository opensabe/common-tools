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
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;

//@Configuration
//https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#changes-to-auto-configuration
@AutoConfiguration
@ConditionalOnProperty(prefix = "scheduler.job", name = "enable")
@EnableConfigurationProperties(SchedulerProperties.class)
public class SchedulerJobAutoConfiguration {

    @Autowired
    private SchedulerProperties schedulerProperties;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "scheduler.job", name = "enable", havingValue = "true")
    public List<JobListener> jobListeners() {
        return new ArrayList<>();
    }

    @Bean(destroyMethod = "closeCommander")
    @ConditionalOnProperty(prefix = "scheduler.job", name = "enable", havingValue = "true")
    public Commander commander(final RedissonClient redissonClient) {
        Commander commander = new Commander(redissonClient, schedulerProperties);
        commander.setUp();
        return commander;
    }

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

    @Bean
    @ConditionalOnClass(SchedulerServer.class)
    @ConditionalOnProperty(prefix = "scheduler.job", name = "enable", havingValue = "true")
    public JobStatisticsAPI jobStatisticsAPI(final SchedulerServer schedulerServer) {
        return new JobStatisticsAPI(schedulerServer);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(HealthIndicator.class)
    @ConditionalOnEnabledHealthIndicator("schedulerjob")
    public static class SchedulerServerHealthIndicatorAutoConfiguration {
        @Bean
        public HealthIndicator healthIndicator(SchedulerProperties schedulerProperties, ObjectProvider<SchedulerServer> schedulerServerProvider) {
            return new SchedulerServerHealthIndicator(schedulerProperties, schedulerServerProvider);
        }
    }

    @Bean
    public TaskCanRunListener taskCanRunListener() {
        return new TaskCanRunListener();
    }

    @Bean
    public JobExecuteObservationToJFRGenerator jobExecuteObservationToJFRGenerator() {
        return new JobExecuteObservationToJFRGenerator();
    }

    @Bean
    public SimpleJobHealthService simpleJobHealthService(StringRedisTemplate stringRedisTemplate, SchedulerProperties schedulerProperties) {
        return new SimpleJobHealthService(stringRedisTemplate, schedulerProperties);
    }

    @Bean
    public HealthCheckJob healthCheckJob(SimpleJobHealthService simpleJobHealthService) {
        return new HealthCheckJob(simpleJobHealthService);
    }
}
