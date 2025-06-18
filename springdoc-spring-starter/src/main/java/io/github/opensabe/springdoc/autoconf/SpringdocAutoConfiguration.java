package io.github.opensabe.springdoc.autoconf;

import io.github.opensabe.springdoc.config.CloudConfig;
import io.github.opensabe.springdoc.config.FrameworkConfig;
import io.github.opensabe.springdoc.config.GenerateConfig;
import io.github.opensabe.springdoc.responses.page.PageModelConverter;
import io.swagger.v3.core.converter.ModelConverter;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * @author heng.ma
 */
@Import({GenerateConfig.class, FrameworkConfig.class, CloudConfig.class})
@ConditionalOnBean(SpringDocConfiguration.class)
@ConditionalOnClass(ModelConverter.class)
@AutoConfiguration(before = SpringDocConfiguration.class)
public class SpringdocAutoConfiguration {
    static {
        PageModelConverter.config();
    }
}
