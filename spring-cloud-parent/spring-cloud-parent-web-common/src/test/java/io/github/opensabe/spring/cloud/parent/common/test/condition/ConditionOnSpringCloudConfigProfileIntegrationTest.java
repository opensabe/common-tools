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

import io.github.opensabe.spring.cloud.parent.common.condition.ConditionOnSpringCloudConfigProfile;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionOnSpringCloudConfigProfileIntegrationTest {

    @Nested
    @SpringBootTest(properties = "spring.cloud.config.profile=test-profile")
    class EqualsPredicateTest {
        @SpringBootApplication(scanBasePackages = "io.github.opensabe.spring.cloud.parent.common.test.condition")
        public static class Main {}

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void testEqualsPredicate() {
            assertTrue(applicationContext.containsBean("equalsBean"));
            assertFalse(applicationContext.containsBean("regexBean"));
            assertTrue(applicationContext.containsBean("antBean"));
        }
    }

    @Nested
    @SpringBootTest(properties = "spring.cloud.config.profile=test-123")
    class RegexPredicateTest {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void testRegexPredicate() {
            assertFalse(applicationContext.containsBean("equalsBean"));
            assertTrue(applicationContext.containsBean("regexBean"));
            assertTrue(applicationContext.containsBean("antBean"));
        }
    }

    @Nested
    @SpringBootTest(properties = "spring.cloud.config.profile=test-abc")
    class AntPredicateTest {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void testAntPredicate() {
            assertFalse(applicationContext.containsBean("equalsBean"));
            assertFalse(applicationContext.containsBean("regexBean"));
            assertTrue(applicationContext.containsBean("antBean"));
        }
    }

    @Nested
    @SpringBootTest(properties = "spring.cloud.config.profile=profile1")
    class MultipleProfilesTest1 {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void testMultipleProfiles() {
            assertTrue(applicationContext.containsBean("multipleProfilesBean"));
        }
    }

    @Nested
    @SpringBootTest(properties = "spring.cloud.config.profile=profile2")
    class MultipleProfilesTest2 {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void testMultipleProfiles() {
            assertTrue(applicationContext.containsBean("multipleProfilesBean"));
        }
    }

    @Nested
    @SpringBootTest(properties = "spring.cloud.config.profile=other")
    class MultipleProfilesNoMatchTest {
        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void testMultipleProfilesNoMatch() {
            assertFalse(applicationContext.containsBean("multipleProfilesBean"));
        }
    }

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
    }
} 