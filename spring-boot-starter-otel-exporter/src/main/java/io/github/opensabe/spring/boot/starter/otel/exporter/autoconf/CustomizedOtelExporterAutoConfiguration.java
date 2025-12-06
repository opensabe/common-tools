package io.github.opensabe.spring.boot.starter.otel.exporter.autoconf;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import io.github.opensabe.spring.boot.starter.otel.exporter.configuration.CustomizedOtelConfiguration;

@Import(CustomizedOtelConfiguration.class)
@AutoConfiguration
public class CustomizedOtelExporterAutoConfiguration {
}

