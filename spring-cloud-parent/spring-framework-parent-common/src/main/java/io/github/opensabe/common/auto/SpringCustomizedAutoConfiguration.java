package io.github.opensabe.common.auto;

import io.github.opensabe.common.config.JacksonCustomizedConfiguration;
import io.github.opensabe.common.config.MicroMeterCustomizedConfiguration;
import io.github.opensabe.common.config.SpringCommonUtilConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        SpringCommonUtilConfiguration.class,
        JacksonCustomizedConfiguration.class,
})
public class SpringCustomizedAutoConfiguration {
}
