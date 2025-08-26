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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.micrometer.observation.Observation;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("JFR观察者处理器测试")
class JFRObservationHandlerTest {

    @Mock
    private ObservationToJFRGenerator<TestContext> generator1;

    @Mock
    private ObservationToJFRGenerator<TestContext> generator2;

    @Mock
    private ObservationToJFRGenerator<AnotherContext> generator3;

    private JFRObservationHandler<TestContext> handler;
    private TestContext testContext;
    private AnotherContext anotherContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(generator1.getContextClazz()).thenReturn(TestContext.class);
        when(generator2.getContextClazz()).thenReturn(TestContext.class);
        when(generator3.getContextClazz()).thenReturn(AnotherContext.class);

        List<ObservationToJFRGenerator<TestContext>> generators = Arrays.asList(generator1, generator2);
        handler = new JFRObservationHandler<>(generators);

        testContext = new TestContext();
        anotherContext = new AnotherContext();
    }

    @Test
    @DisplayName("测试开始事件 - 应调用所有匹配的生成器")
    void onStart_ShouldCallAllMatchingGenerators() {
        // when
        handler.onStart(testContext);

        // then
        verify(generator1).onStart(testContext);
        verify(generator2).onStart(testContext);
        verify(generator3, never()).onStart(any());
    }

    @Test
    @DisplayName("测试开始事件异常处理 - 应处理生成器异常")
    void onStart_ShouldHandleGeneratorException() {
        // given
        doThrow(new RuntimeException("Test exception")).when(generator1).onStart(testContext);

        // when
        handler.onStart(testContext);

        // then
        verify(generator1).onStart(testContext);
        verify(generator2).onStart(testContext);
    }

    @Test
    @DisplayName("测试停止事件 - 应调用所有匹配的生成器")
    void onStop_ShouldCallAllMatchingGenerators() {
        // when
        handler.onStop(testContext);

        // then
        verify(generator1).onStop(testContext);
        verify(generator2).onStop(testContext);
        verify(generator3, never()).onStop(any());
    }

    @Test
    @DisplayName("测试停止事件异常处理 - 应处理生成器异常")
    void onStop_ShouldHandleGeneratorException() {
        // given
        doThrow(new RuntimeException("Test exception")).when(generator1).onStop(testContext);

        // when
        handler.onStop(testContext);

        // then
        verify(generator1).onStop(testContext);
        verify(generator2).onStop(testContext);
    }

    @Test
    @DisplayName("测试上下文支持 - 应始终返回true")
    void supportsContext_ShouldAlwaysReturnTrue() {
        assert handler.supportsContext(testContext);
        assert handler.supportsContext(anotherContext);
    }

    @Test
    @DisplayName("测试构造函数 - 无生成器时应初始化空映射")
    void constructor_ShouldInitializeEmptyMap_WhenNoGenerators() {
        // when
        JFRObservationHandler<TestContext> emptyHandler = new JFRObservationHandler<>(Collections.emptyList());

        // then
        emptyHandler.onStart(testContext);
        emptyHandler.onStop(testContext);
        // No exceptions should be thrown
    }

    private static class TestContext extends Observation.Context {
    }

    private static class AnotherContext extends Observation.Context {
    }
} 