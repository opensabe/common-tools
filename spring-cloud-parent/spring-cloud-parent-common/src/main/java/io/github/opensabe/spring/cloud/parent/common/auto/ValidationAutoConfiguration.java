package io.github.opensabe.spring.cloud.parent.common.auto;

import io.github.opensabe.spring.cloud.parent.common.validation.ExtendValidatorConfigure;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(ExtendValidatorConfigure.class)
@AutoConfigureBefore(org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration.class)
public class ValidationAutoConfiguration {
}
