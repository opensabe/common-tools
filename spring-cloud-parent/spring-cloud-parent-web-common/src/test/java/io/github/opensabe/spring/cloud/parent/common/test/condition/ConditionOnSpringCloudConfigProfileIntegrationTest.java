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
package io.github.opensabe.spring.cloud.parent.common.test.condition;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.spring.cloud.parent.common.condition.ConditionOnSpringCloudConfigProfile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SpringCloudConfigProfile条件注解集成测试")
class ConditionOnSpringCloudConfigProfileIntegrationTest {

    @Configuration
    static class TestConfig {
        @Bean
        @ConditionOnSpringCloudConfigProfile(value = "test-profile", predicate = ConditionOnSpringCloudConfigProfile.Predicate.equals)
        public String equalsBean() {
            return "equals";
        }

        @Bean
        @ConditionOnSpringCloudConfigProfile(value = "test-\\d+", predicate = ConditionOnSpringCloudConfigProfile.Predicate.regex)
        public String regexBean() {
            return "regex";
        }

        @Bean
        @ConditionOnSpringCloudConfigProfile(value = "test-*", predicate = ConditionOnSpringCloudConfigProfile.Predicate.ant)
        public String antBean() {
            return "ant";
        }

        @Bean
        @ConditionOnSpringCloudConfigProfile(value = {"profile1", "profile2"}, predicate = ConditionOnSpringCloudConfigProfile.Predicate.equals)
        public String multipleProfilesBean() {
            return "multiple";
        }

        @Bean
        @ConditionOnSpringCloudConfigProfile(value = "online", predicate = ConditionOnSpringCloudConfigProfile.Predicate.contains)
        public String containsOnlineBean() {
            return "contains-online";
        }

        @Bean
        @ConditionOnSpringCloudConfigProfile(value = "!online", predicate = ConditionOnSpringCloudConfigProfile.Predicate.contains)
        public String notContainsOnlineBean() {
            return "not-contains-online";
        }
    }

    @Nested
    @DisplayName("equals谓词匹配测试")
    @SpringBootTest(properties = "spring.cloud.config.profile=test-profile")
    class EqualsPredicateTest {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("equals谓词应仅注册匹配的Bean")
        void testEqualsPredicate() {
            assertTrue(applicationContext.containsBean("equalsBean"));
            assertFalse(applicationContext.containsBean("regexBean"));
            assertTrue(applicationContext.containsBean("antBean"));
        }

        @SpringBootApplication(scanBasePackages = "io.github.opensabe.spring.cloud.parent.common.test.condition")
        public static class Main {
        }
    }

    @Nested
    @DisplayName("regex谓词匹配测试")
    @SpringBootTest(properties = "spring.cloud.config.profile=test-123")
    class RegexPredicateTest {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("regex谓词应仅注册匹配的Bean")
        void testRegexPredicate() {
            assertFalse(applicationContext.containsBean("equalsBean"));
            assertTrue(applicationContext.containsBean("regexBean"));
            assertTrue(applicationContext.containsBean("antBean"));
        }
    }

    @Nested
    @DisplayName("ant谓词匹配测试")
    @SpringBootTest(properties = "spring.cloud.config.profile=test-abc")
    class AntPredicateTest {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("ant谓词应仅注册匹配的Bean")
        void testAntPredicate() {
            assertFalse(applicationContext.containsBean("equalsBean"));
            assertFalse(applicationContext.containsBean("regexBean"));
            assertTrue(applicationContext.containsBean("antBean"));
        }
    }

    @Nested
    @DisplayName("多profile匹配测试-profile1")
    @SpringBootTest(properties = "spring.cloud.config.profile=profile1")
    class MultipleProfilesTest1 {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("profile1应匹配multipleProfilesBean")
        void testMultipleProfiles() {
            assertTrue(applicationContext.containsBean("multipleProfilesBean"));
        }
    }

    @Nested
    @DisplayName("多profile匹配测试-profile2")
    @SpringBootTest(properties = "spring.cloud.config.profile=profile2")
    class MultipleProfilesTest2 {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("profile2应匹配multipleProfilesBean")
        void testMultipleProfiles() {
            assertTrue(applicationContext.containsBean("multipleProfilesBean"));
        }
    }

    @Nested
    @DisplayName("多profile不匹配测试")
    @SpringBootTest(properties = "spring.cloud.config.profile=other")
    class MultipleProfilesNoMatchTest {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("不匹配的profile不应注册multipleProfilesBean")
        void testMultipleProfilesNoMatch() {
            assertFalse(applicationContext.containsBean("multipleProfilesBean"));
        }
    }

    @Nested
    @DisplayName("contains谓词-au-online")
    @SpringBootTest(properties = "spring.cloud.config.profile=au-online")
    class ContainsAuOnlineTest {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("au-online 应命中 contains online，不命中 !online")
        void auOnlineContainsOnline() {
            assertTrue(applicationContext.containsBean("containsOnlineBean"));
            assertFalse(applicationContext.containsBean("notContainsOnlineBean"));
        }
    }

    @Nested
    @DisplayName("contains谓词-us-online")
    @SpringBootTest(properties = "spring.cloud.config.profile=us-online")
    class ContainsUsOnlineTest {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("us-online 应命中 contains online")
        void usOnlineContainsOnline() {
            assertTrue(applicationContext.containsBean("containsOnlineBean"));
            assertFalse(applicationContext.containsBean("notContainsOnlineBean"));
        }
    }

    @Nested
    @DisplayName("contains谓词-精确online")
    @SpringBootTest(properties = "spring.cloud.config.profile=online")
    class ContainsExactOnlineTest {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("online 应命中 contains online")
        void exactOnlineContainsOnline() {
            assertTrue(applicationContext.containsBean("containsOnlineBean"));
            assertFalse(applicationContext.containsBean("notContainsOnlineBean"));
        }
    }

    @Nested
    @DisplayName("contains谓词-非线上test")
    @SpringBootTest(properties = "spring.cloud.config.profile=test")
    class ContainsNonOnlineTest {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("test 不应命中 contains online，应命中 !online")
        void testDoesNotContainOnline() {
            assertFalse(applicationContext.containsBean("containsOnlineBean"));
            assertTrue(applicationContext.containsBean("notContainsOnlineBean"));
        }
    }
} 