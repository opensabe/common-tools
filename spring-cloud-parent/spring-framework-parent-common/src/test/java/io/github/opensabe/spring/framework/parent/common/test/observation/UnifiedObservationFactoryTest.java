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
package io.github.opensabe.spring.framework.parent.common.test.observation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = UnifiedObservationFactoryTest.Main.class)
@DisplayName("统一观察者工厂测试")
public class UnifiedObservationFactoryTest {
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    @Test
    @DisplayName("测试获取观察者注册表")
    public void testGetObservationRegistry() {
        ObservationRegistry observationRegistry = unifiedObservationFactory.getObservationRegistry();
        assertNotNull(observationRegistry);
    }

    @Test
    @DisplayName("测试启动观察者")
    public void testStartObservation() {
        ObservationRegistry observationRegistry = unifiedObservationFactory.getObservationRegistry();
        Observation observation = Observation.start("testObservation", observationRegistry);
        assertNotNull(observation);
    }

    @SpringBootApplication(scanBasePackages = "io.github.opensabe.spring.framework.parent.common.test.observation")
    public static class Main {
    }
} 