package io.github.opensabe.spring.cloud.starter.third.client.webclient;

import io.github.opensabe.spring.cloud.starter.third.client.conf.ThirdPartyWebClientDefaultConfiguration;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.web.reactive.function.client.WebClient;

public class ThirdPartyWebClientNamedContextFactory extends NamedContextFactory<ThirdPartyWebClientSpecification> {
    public static final String NAMESPACE = "third-party.webclient";
    public static final String PROPERTY_NAME = NAMESPACE + ".name";

    public ThirdPartyWebClientNamedContextFactory() {
        super(ThirdPartyWebClientDefaultConfiguration.class, NAMESPACE, PROPERTY_NAME);
    }

    /**
     * 获取 WebClient
     *
     * @param name
     * @return
     */
    public WebClient getWebClient(String name) {
        return getInstance(name, WebClient.class);
    }
}
