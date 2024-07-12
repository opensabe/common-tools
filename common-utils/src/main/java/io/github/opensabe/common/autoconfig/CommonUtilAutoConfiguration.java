package io.github.opensabe.common.autoconfig;

import io.github.opensabe.common.config.SpringCommonUtilConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        SpringCommonUtilConfiguration.class,
})
public class CommonUtilAutoConfiguration {
}
