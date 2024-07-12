package io.github.opensabe.spring.cloud.starter.third.client.auto;

import io.github.opensabe.spring.cloud.starter.third.client.conf.ThirdPartyWebClientConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(ThirdPartyWebClientConfiguration.class)
@Configuration(proxyBeanMethods = false)
public class ThirdPartyWebClientAutoConfiguration {
}
