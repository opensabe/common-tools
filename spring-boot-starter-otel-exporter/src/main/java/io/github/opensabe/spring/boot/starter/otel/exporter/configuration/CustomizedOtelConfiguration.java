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
package io.github.opensabe.spring.boot.starter.otel.exporter.configuration;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.autoconfigure.tracing.TracingProperties;
import org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpGrpcSpanExporterBuilderCustomizer;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.List;

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
    @Conditional(CustomizedOtelEnabledCondition.class)
    public OtlpGrpcSpanExporterBuilderCustomizer customizedOtelTracingExporterBuilderCustomizer() {
        String appenv = System.getenv(APP_ENV);
        String tracingEndpoint = System.getenv(TRACING_ENDPOINT);
        log.info("init OtlpGrpcSpanExporter with tracingEndpoint: {}, app_env: {}", tracingEndpoint, appenv);
        return builder -> builder.setEndpoint(tracingEndpoint)
                //这个是运维约定的header，用于区分不同的环境
                //APPENV是环境变量，运维在k8s的pod中设置了这个环境变量，我们在上报的时候带上这个header用于区分
                .addHeader("X-Scope-OrgID", appenv);
    }

    @Bean
    @Primary
    @Conditional(CustomizedOtelEnabledCondition.class)
    Sampler customizedOtelSampler(Environment environment, TracingProperties properties) {
        String tracingSampleRatio = environment.getProperty(TRACING_SAMPLE_RATIO);
        double ratio;
        if (StringUtils.isBlank(tracingSampleRatio)) {
            ratio = properties.getSampling().getProbability();
        }else {
            //测试环境支持全部drop的
            ratio = Double.parseDouble(tracingSampleRatio);
            if (ratio <= 0) {
                return new Sampler() {
                    @Override
                    public SamplingResult shouldSample(Context parentContext, String traceId, String name, SpanKind spanKind, Attributes attributes, List<LinkData> parentLinks) {
                        return SamplingResult.drop();
                    }

                    @Override
                    public String getDescription() {
                        return "NoopSampler";
                    }
                };
            }
        }

        Sampler rootSampler = Sampler.traceIdRatioBased(ratio);
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

