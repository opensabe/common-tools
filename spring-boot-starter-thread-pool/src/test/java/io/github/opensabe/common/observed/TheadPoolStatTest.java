package io.github.opensabe.common.observed;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.scheduler.ThreadPoolStatScheduler;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import jdk.jfr.consumer.RecordedEvent;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Log4j2
@JfrEventTest
@ActiveProfiles("jfr")
@AutoConfigureObservability
@SpringBootTest(properties = {"eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"}
)
public class TheadPoolStatTest {

    @SpringBootApplication
    static class MockConfig {
    }

    @Autowired
    ThreadPoolFactory threadPoolFactory;
    @Autowired
    ThreadPoolStatScheduler scheduler;
    @Autowired
    UnifiedObservationFactory unifiedObservationFactory;

    public JfrEvents jfrEvents = new JfrEvents();

    @Test
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

}
