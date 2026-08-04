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
package io.github.opensabe.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.opensabe.spring.cloud.parent.common.web.Debug;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验 ExceptionConfiguration 按 profile 是否包含 online 切换 Debug。
 */
@DisplayName("ExceptionConfiguration Debug 按 profile 包含 online 切换")
class ExceptionConfigurationDebugProfileTest {

    @Nested
    @DisplayName("au-online → Debug 关闭")
    @SpringBootTest(
            classes = App.class,
            properties = "spring.cloud.config.profile=au-online"
    )
    class AuOnline {
        @Autowired
        private Debug debug;

        @Test
        @DisplayName("au-online 应关闭调试")
        void debugDisabled() {
            assertFalse(debug.isEnabled());
        }
    }

    @Nested
    @DisplayName("us-online → Debug 关闭")
    @SpringBootTest(
            classes = App.class,
            properties = "spring.cloud.config.profile=us-online"
    )
    class UsOnline {
        @Autowired
        private Debug debug;

        @Test
        @DisplayName("us-online 应关闭调试")
        void debugDisabled() {
            assertFalse(debug.isEnabled());
        }
    }

    @Nested
    @DisplayName("online → Debug 关闭")
    @SpringBootTest(
            classes = App.class,
            properties = "spring.cloud.config.profile=online"
    )
    class ExactOnline {
        @Autowired
        private Debug debug;

        @Test
        @DisplayName("online 应关闭调试")
        void debugDisabled() {
            assertFalse(debug.isEnabled());
        }
    }

    @Nested
    @DisplayName("test → Debug 开启")
    @SpringBootTest(
            classes = App.class,
            properties = "spring.cloud.config.profile=test"
    )
    class NonOnline {
        @Autowired
        private Debug debug;

        @Test
        @DisplayName("test 应开启调试")
        void debugEnabled() {
            assertTrue(debug.isEnabled());
        }
    }

    @SpringBootApplication
    static class App {
    }
}
