package io.github.opensabe.spring.boot.starter.otel.exporter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import lombok.extern.log4j.Log4j2;

/**
 * @see org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryAutoConfiguration
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class CustomizedOtelConfiguration {

    private static final String APP_ENV = "APPENV";
    private static final String TRACING_ENDPOINT = "TRACING_ENDPOINT";
    private static final String TRACING_SAMPLE_RATIO = "TRACING_SAMPLE_RATIO";


    @Bean
    @Primary
    @Conditional(CustomizedOtelEnabledCondition.class)
    public OtlpGrpcSpanExporter customizedOtelTracingExporter() {
        String appenv = System.getenv(APP_ENV);
        String tracingEndpoint = System.getenv(TRACING_ENDPOINT);
        log.info("init OtlpGrpcSpanExporter with tracingEndpoint: {}, app_env: {}", tracingEndpoint, appenv);
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint(tracingEndpoint)
                //这个是运维约定的header，用于区分不同的环境
                //APPENV是环境变量，运维在k8s的pod中设置了这个环境变量，我们在上报的时候带上这个header用于区分
                .addHeader("X-Scope-OrgID", appenv)
                .build();
    }

    @Bean
    @Primary
    @Conditional(CustomizedOtelEnabledCondition.class)
    Sampler customizedOtelSampler(Environment environment) {
        String tracingSampleRatio = environment.getProperty(TRACING_SAMPLE_RATIO);
        Sampler rootSampler = Sampler.traceIdRatioBased(Double.parseDouble(tracingSampleRatio));
        log.info("init Sampler with tracingSampleRatio: {}", tracingSampleRatio);
        return Sampler.parentBased(rootSampler);
    }


    @Log4j2
    public static class CustomizedOtelEnabledCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String appenv = System.getenv(APP_ENV);
            String tracingEndpoint = System.getenv(TRACING_ENDPOINT);
            String tracingSampleRatio = System.getenv(TRACING_SAMPLE_RATIO);
            log.info(
                    "CustomizedOtelEnabledCondition: appenv: {}, tracingEndpoint: {}, tracingSampleRatio: {}",
                    appenv, tracingEndpoint, tracingSampleRatio
            );
            //必须要有这三个属性才会满足条件
            return appenv != null && tracingEndpoint != null && tracingSampleRatio != null;
        }
    }
}

