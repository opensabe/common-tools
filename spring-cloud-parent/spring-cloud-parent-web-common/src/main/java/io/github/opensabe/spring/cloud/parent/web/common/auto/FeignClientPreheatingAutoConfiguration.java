package io.github.opensabe.spring.cloud.parent.web.common.auto;

import io.github.opensabe.spring.cloud.parent.web.common.config.FeignClientPreheatingConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(FeignClientPreheatingConfiguration.class)
public class FeignClientPreheatingAutoConfiguration {
}
