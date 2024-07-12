package io.github.opensabe.spring.cloud.parent.web.common.auto;

import io.github.opensabe.spring.cloud.parent.web.common.config.CommonOpenFeignConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CommonOpenFeignConfiguration.class})
//@EnableFeignClients(value = "io.github.opensabe", defaultConfiguration = DefaultOpenFeignConfiguration.class)
public class OpenFeignAutoConfiguration {


}
