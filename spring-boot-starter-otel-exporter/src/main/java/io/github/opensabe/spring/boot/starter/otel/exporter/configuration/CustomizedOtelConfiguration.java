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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.micrometer.tracing.autoconfigure.TracingProperties;
import org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.otlp.OtlpGrpcSpanExporterBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import lombok.extern.log4j.Log4j2;

/**
 * 基于运维约定环境变量的 OpenTelemetry OTLP 导出与采样定制。
 * <p>
 * 当 {@code APPENV}、{@code TRACING_ENDPOINT}、{@code TRACING_SAMPLE_RATIO} 均存在时生效，
 * 覆盖 OTLP endpoint 并设置 {@code X-Scope-OrgID} 头以区分环境。
 * </p>
 *
 * @see org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryAutoConfiguration
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class CustomizedOtelConfiguration {

    /**
     * 应用环境标识环境变量名（K8s Pod 注入）。
     */
    private static final String APP_ENV = "APPENV";

    /**
     * OTLP 追踪上报端点环境变量名。
     */
    private static final String TRACING_ENDPOINT = "TRACING_ENDPOINT";

    /**
     * 追踪采样比例环境变量名；{@code <= 0} 时启用全 drop 采样器。
     */
    private static final String TRACING_SAMPLE_RATIO = "TRACING_SAMPLE_RATIO";

    /**
     * 定制 OTLP gRPC Span Exporter：设置 endpoint 与 {@code X-Scope-OrgID} 头。
     *
     * @return {@link OtlpGrpcSpanExporterBuilderCustomizer}
     */
    @Bean
    @Conditional(CustomizedOtelEnabledCondition.class)
    public OtlpGrpcSpanExporterBuilderCustomizer customizedOtelTracingExporterBuilderCustomizer() {
        String appenv = System.getenv(APP_ENV);
        String tracingEndpoint = System.getenv(TRACING_ENDPOINT);
        log.info("init OtlpGrpcSpanExporter with tracingEndpoint: {}, app_env: {}", tracingEndpoint, appenv);
        return builder -> builder.setEndpoint(tracingEndpoint)
                .addHeader("X-Scope-OrgID", appenv);
    }

    /**
     * 注册主 {@link Sampler}：优先读取 {@code TRACING_SAMPLE_RATIO}，否则回退 Boot {@link TracingProperties}。
     * 比例 {@code <= 0} 时返回恒 drop 的 noop 采样器（便于测试环境关闭追踪）。
     *
     * @param environment  Spring 环境
     * @param properties   Boot tracing 属性
     * @return parent-based 采样器或全 drop 采样器
     */
    @Bean
    @Primary
    @Conditional(CustomizedOtelEnabledCondition.class)
    Sampler customizedOtelSampler(Environment environment, TracingProperties properties) {
        String tracingSampleRatio = environment.getProperty(TRACING_SAMPLE_RATIO);
        double ratio;
        if (StringUtils.isBlank(tracingSampleRatio)) {
            ratio = properties.getSampling().getProbability();
        } else {
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

    /**
     * 判断是否启用本配置的 Spring {@link Condition}。
     * 要求 {@code APPENV}、{@code TRACING_ENDPOINT}、{@code TRACING_SAMPLE_RATIO} 三个环境变量均非 null。
     */
    @Log4j2
    public static class CustomizedOtelEnabledCondition implements Condition {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String appenv = System.getenv(APP_ENV);
            String tracingEndpoint = System.getenv(TRACING_ENDPOINT);
            String tracingSampleRatio = System.getenv(TRACING_SAMPLE_RATIO);
            log.info(
                    "CustomizedOtelEnabledCondition: appenv: {}, tracingEndpoint: {}, tracingSampleRatio: {}",
                    appenv, tracingEndpoint, tracingSampleRatio
            );
            return appenv != null && tracingEndpoint != null && tracingSampleRatio != null;
        }
    }
}
