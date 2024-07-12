package io.github.opensabe.scheduler;

import io.github.opensabe.scheduler.server.SchedulerServer;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.TimeUnit;

@JfrEventTest
@AutoConfigureObservability
@SpringBootTest(properties = {
        "spring.main.allow-circular-references=true",
        "scheduler.job.expired-time=6000",
        "scheduler.job.enable=true",
        "eureka.client.enabled=false"
},
        classes = TestTask.App.class)
public class TestTask {

    @ClassRule
    @SuppressWarnings({"deprecation", "resource"})
    public static GenericContainer<?> redis = new FixedHostPortGenericContainer<>("redis")
            .withFixedExposedPort(6379, 6379)
            .withExposedPorts(6379)
            .withCommand("redis-server");

    @SpringBootApplication
    public static class App {

    }

    @BeforeAll
    static void setup() {
        redis.start();
    }

    @AfterAll
    static void destroy() {
        redis.stop();
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
