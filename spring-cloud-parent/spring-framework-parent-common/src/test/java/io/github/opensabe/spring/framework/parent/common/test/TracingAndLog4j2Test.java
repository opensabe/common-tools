package io.github.opensabe.spring.framework.parent.common.test;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.micrometer.observation.tck.ObservationRegistryAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Log4j2
@SpringBootTest(classes = TracingAndLog4j2Test.Main.class)
@AutoConfigureObservability
public class TracingAndLog4j2Test {
    private static final String SECRET = "secretString";
    private static final String IDENTIFIER = "IDENTIFIER";

    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    private static final AtomicBoolean hasSecret = new AtomicBoolean(false);
    @SpringBootApplication(scanBasePackages = "io.github.opensabe.spring.framework.parent.common.test.secret")
    public static class Main {
        @Bean
        public TestSecretProvider testSecretProvider(GlobalSecretManager globalSecretManager) {
            return new TestSecretProvider(globalSecretManager);
        }
    }

    @Test
    public void testLog4j2() throws InterruptedException {
        log.info("{} test {} test {} test {}", IDENTIFIER, SECRET, SECRET + SECRET, SECRET + "xx");
        countDownLatch.await();
        //不能有 secret 字符串
        Assertions.assertFalse(hasSecret.get());
    }

    public static class TestSecretProvider extends SecretProvider {
        protected TestSecretProvider(GlobalSecretManager globalSecretManager) {
            super(globalSecretManager);
        }

        @Override
        protected String name() {
            return "testSecretProvider";
        }

        @Override
        protected long reloadTimeInterval() {
            return 1;
        }

        @Override
        protected TimeUnit reloadTimeIntervalUnit() {
            return TimeUnit.DAYS;
        }

        @Override
        protected Map<String, Set<String>> reload() {
            return Map.of(
                    "testSecretProviderKey", Set.of(SECRET)
            );
        }
    }

    @Plugin(name = "CustomAppender", category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
    public static class CustomAppender extends AbstractAppender {

        protected CustomAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
            super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
        }

        @PluginFactory
        public static CustomAppender createAppender(
                @PluginAttribute("name") String name,
                @PluginElement("Filter") Filter filter,
                @PluginElement("Layout") Layout<? extends Serializable> layout,
                @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
            return new CustomAppender(name, filter, layout, ignoreExceptions);
        }

        @Override
        public void append(LogEvent event) {
            String formattedMessage = event.getMessage().getFormattedMessage();
            System.out.println(formattedMessage);
            if (formattedMessage.contains(SECRET)) {
                hasSecret.set(true);
            }
            if (formattedMessage.contains(IDENTIFIER)) {
                countDownLatch.countDown();
            }
        }
    }

    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    @Test
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
