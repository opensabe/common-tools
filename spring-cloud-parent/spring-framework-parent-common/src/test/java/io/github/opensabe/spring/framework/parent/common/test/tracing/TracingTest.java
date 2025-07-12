package io.github.opensabe.spring.framework.parent.common.test.tracing;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static io.micrometer.observation.tck.ObservationRegistryAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
@SpringBootTest(
        classes = TracingTest.Main.class
)
@DisplayName("链路追踪测试")
public class TracingTest {
    @SpringBootApplication
    public static class Main {
    }

    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    @Test
    @DisplayName("测试链路追踪功能 - 验证父子观察者和MDC上下文")
    public void testTracing() {
        var logger = LoggerFactory.getLogger("test");
        ObservationRegistry observationRegistry = unifiedObservationFactory.getObservationRegistry();
        var parent = Observation.start("parent", observationRegistry);
        parent.scoped(() -> {
            logger.info("parent");
            //获取当前 Observation
            Observation current = unifiedObservationFactory.getCurrentObservation();
            assertTrue(current == parent);
            //验证日志 Context 中有放入对应的 key
            String parentLoggerTraceId = MDC.get("traceId");
            String parentLoggerSpanId = MDC.get("spanId");
            //验证从当前 Observation 获取的 TracingContext 中的 Span 与日志 Context 中的一致
            Assertions.assertEquals(parentLoggerTraceId, UnifiedObservationFactory.getTraceContext(current).traceId());
            Assertions.assertEquals(parentLoggerSpanId, UnifiedObservationFactory.getTraceContext(current).spanId());
            //判断 Observation
            assertThat(observationRegistry)
                    .hasRemainingCurrentObservationSameAs(parent);
            var child = Observation.start("child", observationRegistry);
            child.scoped(() -> {
                logger.info("child");
                //验证日志 Context 中有放入对应的 key
                String childLoggerTraceId = MDC.get("traceId");
                String childLoggerSpanId = MDC.get("spanId");
                //判断 Observation
                assertThat(observationRegistry)
                        .hasRemainingCurrentObservationSameAs(child)
                        .doesNotHaveRemainingCurrentObservationSameAs(parent);
                //获取当前 Observation
                Observation currentChild = unifiedObservationFactory.getCurrentObservation();
                //验证从当前 Observation 获取的 TracingContext 中的 Span 与日志 Context 中的一致
                Assertions.assertEquals(childLoggerTraceId, UnifiedObservationFactory.getTraceContext(currentChild).traceId());
                Assertions.assertEquals(childLoggerSpanId, UnifiedObservationFactory.getTraceContext(currentChild).spanId());
                Assertions.assertEquals(parentLoggerTraceId, childLoggerTraceId);
                Assertions.assertNotNull(parentLoggerTraceId);
                Assertions.assertNotNull(parentLoggerSpanId);
                Assertions.assertNotNull(childLoggerTraceId);
                Assertions.assertNotNull(childLoggerSpanId);
            });
        });
    }
}
