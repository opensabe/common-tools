package io.github.opensabe.spring.cloud.parent.web.common.internalcall;

import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.web.client.RestTemplate;

public class InternalCallRestTemplateCustomizer implements RestTemplateCustomizer {

    private final InternalCallRestTemplateInterceptor interceptor;

    public InternalCallRestTemplateCustomizer(InternalCallRestTemplateInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        InternalCallClients.decorate(restTemplate, interceptor);
    }
}
