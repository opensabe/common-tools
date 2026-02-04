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
package io.github.opensabe.common.redisson.test;

import io.github.opensabe.common.redisson.aop.scheduled.EnvironmentalRefreshable;
import io.github.opensabe.common.redisson.aop.scheduled.RedissonScheduledService;
import io.github.opensabe.common.redisson.observation.rlock.ObservedRLock;
import lombok.Setter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = RedissonScheduledTest.App.class,
        properties = "spring.application.name=redisson-scheduled")
@DisplayName("测试RedissonScheduled")
public class RedissonScheduledTest {

    /**
     * 必须使用SpyBean,否则不经过BeanPostProcessor
     */
    @MockitoSpyBean
    private Task task;

    @Autowired
    private ApplicationContext applicationContext;

    @SpringBootApplication
    public static class App {

        /**
         * 因为 RedissonScheduledListiner.init() 在spring启动时执行，因此不能使用@BeforeEach打桩
         */
        @Bean
        public RedissonClient redissonClient () {
            ObservedRLock lock = Mockito.mock(ObservedRLock.class);
            Mockito.doNothing().when(lock).lock();
            //必须覆盖一下这个方法，否则getLock会走进真正的方法
            Mockito.when(lock.getName()).thenReturn("testRefreshTask:leader");
            //覆盖了这个方法以后才能获取到锁
            Mockito.when(lock.isHeldByCurrentThread()).thenReturn(true);
            RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
            Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
            return  redissonClient;
        }

        @Bean
        public Task customerTask () {
            return new Task();
        }
    }



    @BeforeAll
    static void setup () {
        System.setProperty("task.interval", "2000");
    }

    @Test
    @DisplayName("测试动态修改时间间隔")
    void test1 () throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(4500);
        Mockito.verify(task, Mockito.times(2)).run();
        Mockito.clearInvocations(task);
        System.setProperty("task.interval", "3000");
        //System.properties不需要ContentRefresher.refresh也能自动获取到最新值
        //并且使用ContentRefresher.refresh后EnvironmentChangeEvent也没有这个key。
        //如果不引入spring cloud config，我们不太容易模拟EnvironmentChangeEvent,因此直接发送一个事件即可
        applicationContext.publishEvent(new EnvironmentChangeEvent(applicationContext, Set.of("task.interval")));

        TimeUnit.MILLISECONDS.sleep(10800);
        Mockito.verify(task, Mockito.times(3)).run();
    }

    public static class Task implements RedissonScheduledService, EnvironmentalRefreshable, EnvironmentAware {

        @Setter
        private Environment environment;

        @Override
        public String name() {
            return "testRefreshTask";
        }

        @Override
        public long fixedDelay() {
            return environment.getProperty("task.interval", Long.class);
        }

        @Override
        public long initialDelay() {
            return 0;
        }

        @Override
        public boolean stopOnceShutdown() {
            return false;
        }

        @Override
        public void run() {
            long fixedDelay = fixedDelay();
            System.out.println(System.currentTimeMillis()+" fixedDelay: "+fixedDelay);
            try {
                //为了防止定时任务提前排队，无法取消，执行时间比时间间隔稍微长点。
                TimeUnit.MILLISECONDS.sleep(BigDecimal.valueOf(fixedDelay * 1.2).longValue());
            } catch (InterruptedException e) {

            }
        }

        @Override
        public boolean test(Set<String> strings) {
            return strings.contains("task.interval");
        }
    }
}
