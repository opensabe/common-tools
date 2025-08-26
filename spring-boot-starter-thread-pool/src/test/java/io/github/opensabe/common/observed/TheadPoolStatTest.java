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
package io.github.opensabe.common.observed;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.scheduler.ThreadPoolStatScheduler;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import jdk.jfr.consumer.RecordedEvent;
import lombok.extern.log4j.Log4j2;

import static org.junit.Assert.assertTrue;

@Log4j2
@JfrEventTest
@ActiveProfiles("jfr")
@AutoConfigureObservability
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
})
//JFR 测试最好在本地做
@Disabled
@DisplayName("线程池统计测试")
public class TheadPoolStatTest {

    public JfrEvents jfrEvents = new JfrEvents();
    @Autowired
    ThreadPoolFactory threadPoolFactory;
    @Autowired
    ThreadPoolStatScheduler scheduler;
    @Autowired
    UnifiedObservationFactory unifiedObservationFactory;

    @Test
    @DisplayName("测试线程池统计功能 - 验证JFR事件记录")
    public void testNormal() {
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool("threadPoolStat", 2);

        Observation currentOrCreateEmptyObservation1 = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        currentOrCreateEmptyObservation1.scoped(() -> {
            for (int i = 0; i < 1000; i++) {
                executorService.submit(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(10L);
                    } catch (InterruptedException e) {
                    }
                });
            }
        });

        scheduler.recordStat();
        //等待事件全部采集到
        jfrEvents.awaitEvents();

        List<RecordedEvent> events = jfrEvents.events()
                .filter(recordedEvent ->
                        recordedEvent.getEventType().getName().equals("io.github.opensabe.common.executor.jfr.ThreadPoolStatJFREvent")
                ).collect(Collectors.toList());
        log.info(events);
        assertTrue(!events.isEmpty());
    }

    @SpringBootApplication
    static class MockConfig {
    }

}
