package io.github.opensabe.common.auto;

import io.github.opensabe.common.config.SwaggerDatetimeResolveConfiguration;
import io.swagger.v3.core.jackson.ModelResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * @author heng.ma
 */
@Import(SwaggerDatetimeResolveConfiguration.class)
@AutoConfiguration
@ConditionalOnClass(ModelResolver.class)
public class SpringdocAutoConfiguration {
}
