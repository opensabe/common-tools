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
package io.github.opensabe.scheduler;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.opensabe.common.testcontainers.integration.SingleValkeyIntegrationTest;
import io.github.opensabe.scheduler.server.SchedulerServer;

/**
 * 调度器集成测试（Valkey Testcontainers）。
 */
@JfrEventTest
@ExtendWith({
        SpringExtension.class,
        SingleValkeyIntegrationTest.class,
})
@SpringBootTest(properties = {
        "scheduler.job.expired-time=6000",
        "scheduler.job.enable=true",
        "eureka.client.enabled=false"
},
        classes = TestWithValkeyTask.App.class)
@DisplayName("调度器集成测试（Valkey）")
public class TestWithValkeyTask {

    @Autowired
    private SchedulerServer schedulerServer;
    @Autowired
    private TempleTask templeTask;

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleValkeyIntegrationTest.setProperties(registry);
    }

    /**
     * 启动调度容器后，定时任务应在超时前至少执行一次。
     */
    @Test
    @DisplayName("调度容器启动后定时任务可执行")
    public void testContainer() throws InterruptedException {
        schedulerServer.getJobs().keySet().forEach(System.out::println);
        int count = 0;
        while (!templeTask.run && count < 10) {
            System.out.println("not yet");
            TimeUnit.SECONDS.sleep(1);
            count++;
        }
        Assertions.assertTrue(templeTask.run, "TempleTask should have executed");
    }

    @SpringBootApplication
    public static class App {

    }
}
