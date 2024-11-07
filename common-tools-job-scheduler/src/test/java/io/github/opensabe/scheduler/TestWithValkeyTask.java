package io.github.opensabe.scheduler;

import io.github.opensabe.common.testcontainers.integration.SingleValkeyIntegrationTest;
import io.github.opensabe.scheduler.server.SchedulerServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

@JfrEventTest
@AutoConfigureObservability
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
public class TestWithValkeyTask {

    @SpringBootApplication
    public static class App {

    }
    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleValkeyIntegrationTest.setProperties(registry);
    }

    @Autowired
    private SchedulerServer schedulerServer;
    @Autowired
    private TempleTask templeTask;

    @Test
    public void testContainer() throws InterruptedException {
        schedulerServer.getJobs().keySet().forEach(System.out::println);
        int count = 0;
        while (!templeTask.run && count < 10) {
            System.out.println("not yet");
            TimeUnit.SECONDS.sleep(1);
            count++;
        }
    }
}
