package io.github.opensabe.spring.framework.parent.common.test.observation;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = UnifiedObservationFactoryTest.Main.class)
public class UnifiedObservationFactoryTest {
    @SpringBootApplication(scanBasePackages = "io.github.opensabe.spring.framework.parent.common.test.observation")
    public static class Main {
    }

    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    @Test
    public void testGetObservationRegistry() {
        ObservationRegistry observationRegistry = unifiedObservationFactory.getObservationRegistry();
        assertNotNull(observationRegistry);
    }

    @Test
    public void testStartObservation() {
        ObservationRegistry observationRegistry = unifiedObservationFactory.getObservationRegistry();
        Observation observation = Observation.start("testObservation", observationRegistry);
        assertNotNull(observation);
    }
} 