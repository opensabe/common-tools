package io.github.opensabe.common.auto;

import io.github.opensabe.common.config.JacksonCustomizedConfiguration;
import io.github.opensabe.common.config.MicroMeterCustomizedConfiguration;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration(before = ObservationAutoConfiguration.class)
@Import({
        MicroMeterCustomizedConfiguration.class,
})
public class MicroMeterCustomizedAutoConfiguration {
}
