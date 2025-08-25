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
package io.github.opensabe.common.jfr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micrometer.observation.Observation;

@DisplayName("观察者到JFR生成器测试")
class ObservationToJFRGeneratorTest {

    private TestObservationToJFRGenerator generator;
    private TestContext testContext;
    private AnotherContext anotherContext;

    @BeforeEach
    void setUp() {
        generator = new TestObservationToJFRGenerator();
        testContext = new TestContext();
        anotherContext = new AnotherContext();
    }

    @Test
    @DisplayName("测试开始事件 - 当shouldGenerateOnStart返回true时应调用generateOnStart")
    void onStart_ShouldCallGenerateOnStart_WhenShouldGenerateOnStartReturnsTrue() {
        // given
        generator.setShouldGenerateOnStart(true);

        // when
        generator.onStart(testContext);

        // then
        assertTrue(generator.isGenerateOnStartCalled());
    }

    @Test
    @DisplayName("测试开始事件 - 当shouldGenerateOnStart返回false时不应调用generateOnStart")
    void onStart_ShouldNotCallGenerateOnStart_WhenShouldGenerateOnStartReturnsFalse() {
        // given
        generator.setShouldGenerateOnStart(false);

        // when
        generator.onStart(testContext);

        // then
        assertFalse(generator.isGenerateOnStartCalled());
    }

    @Test
    @DisplayName("测试停止事件 - 当shouldCommitOnStop返回true时应调用commitOnStop")
    void onStop_ShouldCallCommitOnStop_WhenShouldCommitOnStopReturnsTrue() {
        // given
        generator.setShouldCommitOnStop(true);

        // when
        generator.onStop(testContext);

        // then
        assertTrue(generator.isCommitOnStopCalled());
    }

    @Test
    @DisplayName("测试停止事件 - 当shouldCommitOnStop返回false时不应调用commitOnStop")
    void onStop_ShouldNotCallCommitOnStop_WhenShouldCommitOnStopReturnsFalse() {
        // given
        generator.setShouldCommitOnStop(false);

        // when
        generator.onStop(testContext);

        // then
        assertFalse(generator.isCommitOnStopCalled());
    }

    @Test
    @DisplayName("测试开始事件 - 当上下文类型不匹配时应抛出ClassCastException")
    void onStart_ShouldThrowClassCastException_WhenContextTypeMismatch() {
        assertThrows(ClassCastException.class, () -> generator.onStart(anotherContext));
    }

    @Test
    @DisplayName("测试停止事件 - 当上下文类型不匹配时应抛出ClassCastException")
    void onStop_ShouldThrowClassCastException_WhenContextTypeMismatch() {
        assertThrows(ClassCastException.class, () -> generator.onStop(anotherContext));
    }

    @Test
    @DisplayName("测试获取上下文类 - 应返回正确的类")
    void getContextClazz_ShouldReturnCorrectClass() {
        assertEquals(TestContext.class, generator.getContextClazz());
    }

    private static class TestContext extends Observation.Context {
    }

    private static class AnotherContext extends Observation.Context {
    }

    private static class TestObservationToJFRGenerator extends ObservationToJFRGenerator<TestContext> {
        private boolean shouldGenerateOnStart;
        private boolean shouldCommitOnStop;
        private boolean generateOnStartCalled;
        private boolean commitOnStopCalled;

        @Override
        public Class<TestContext> getContextClazz() {
            return TestContext.class;
        }

        @Override
        protected boolean shouldCommitOnStop(TestContext context) {
            return shouldCommitOnStop;
        }

        @Override
        protected boolean shouldGenerateOnStart(TestContext context) {
            return shouldGenerateOnStart;
        }

        @Override
        protected void commitOnStop(TestContext context) {
            commitOnStopCalled = true;
        }

        @Override
        protected void generateOnStart(TestContext context) {
            generateOnStartCalled = true;
        }

        public void setShouldGenerateOnStart(boolean shouldGenerateOnStart) {
            this.shouldGenerateOnStart = shouldGenerateOnStart;
        }

        public void setShouldCommitOnStop(boolean shouldCommitOnStop) {
            this.shouldCommitOnStop = shouldCommitOnStop;
        }

        public boolean isGenerateOnStartCalled() {
            return generateOnStartCalled;
        }

        public boolean isCommitOnStopCalled() {
            return commitOnStopCalled;
        }
    }
} 