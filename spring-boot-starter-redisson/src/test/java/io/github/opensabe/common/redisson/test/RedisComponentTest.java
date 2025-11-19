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

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.observation.ObservedRedissonClient;
import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;
import io.github.opensabe.common.utils.SpringUtil;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.opensabe.common.redisson.observation.ObservedRedissonClient;
import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;

/**
 * 检查组件是否生效
 *
 * @author heng.ma
 */
@Import(RedisComponentTest.Config.class)
public class RedisComponentTest extends BaseRedissonTest {

    private final RedissonClient redissonClient;
    private final RedisConnectionFactory redisConnectionFactory;
    private final TestRestTemplate restTemplate;

    public static class Config {

        @Bean
        public TestAdvice testAdvice(StringRedisTemplate redisTemplate) {
            return new TestAdvice(redisTemplate);
        }

        @Bean
        public TestAdviser testAdviser (TestAdvice testAdvice) {
            TestAdviser adviser = new TestAdviser();
            adviser.setAdvice(testAdvice);
            return adviser;
        }

        @Bean
        public TestController testController() {
            return new TestController();
        }


        @Bean
        public CustomerHandler customerHandler () {
            return new CustomerHandler();
        }

    }

    public static class  CustomerHandler implements ObservationHandler<Observation.Context> {
        static final AtomicBoolean called = new AtomicBoolean(false);
        @Override
        public boolean supportsContext(Observation.Context context) {
            return true;
        }

        @Override
        public void onStop(Observation.Context context) {
            called.set(true);
        }
    }

    @Log4j2
    public static class TestAdvice implements MethodBeforeAdvice {


        public TestAdvice(StringRedisTemplate redisTemplate) {

        }

        @Override
        public void before(Method method, Object[] args, Object target) throws Throwable {
            log.info("--------------before---------------");
        }
    }

    public static class TestAdviser extends StaticMethodMatcherPointcutAdvisor {

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return TestController.class.isAssignableFrom(targetClass);
        }
    }

    @Log4j2
    @RestController
    public static class TestController {

        @GetMapping("/test")
        public String test() {
            Observation observation = SpringUtil.getBean(UnifiedObservationFactory.class).getCurrentObservation();
            log.info("----------------run------------------"+observation);
            return "test";
        }
    }

    @Autowired
    public RedisComponentTest(RedissonClient redissonClient, RedisConnectionFactory redisConnectionFactory, TestRestTemplate restTemplate) {
        this.redissonClient = redissonClient;
        this.redisConnectionFactory = redisConnectionFactory;
        this.restTemplate = restTemplate;
    }

    /**
     * 因为JFR单元测试默认没有打开，如果RedissonClientBeanPostProcessor没有生效不能及时检查到
     * 这个单元测试是为了确保RedissonClientBeanPostProcessor中的RedissonClient被替换为ObservedRedissonClient
     */
    @Test
    void testRedissonClient() {
        Assertions.assertInstanceOf(ObservedRedissonClient.class, redissonClient);
    }

    /**
     * 这个单元测试是为了确保RedissonAutoConfigurationV2中的连接池没有覆盖掉LettuceConnectionFactory的配置
     */
    @Test
    void testRedisConnectionFactory() {
        Assertions.assertInstanceOf(LettuceConnectionFactory.class, redisConnectionFactory);
    }


    /**
     * 以前的bug，手写adviser时，如果依赖了redisTemplate，会导致observation失效
     * 但是目前observation只在servlet环境下生效，在webflux环境下不生效
     */
    @Test
    void testObservation () {
        ResponseEntity<String> entity = restTemplate.getForEntity("/test", String.class);
        System.out.println(entity);
        Assertions.assertEquals("test", entity.getBody());
        Assertions.assertTrue(CustomerHandler.called.get());

    }
}
