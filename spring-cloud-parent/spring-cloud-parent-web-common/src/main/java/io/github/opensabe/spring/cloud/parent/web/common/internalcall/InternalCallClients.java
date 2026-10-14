package io.github.opensabe.spring.cloud.parent.web.common.internalcall;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Manual RestTemplate decoration for {@code new RestTemplate()} instances.
 * Idempotent: adds at most one {@link InternalCallRestTemplateInterceptor}.
 */
public final class InternalCallClients {

    private InternalCallClients() {
    }

    public static RestTemplate decorate(RestTemplate restTemplate, InternalCallRestTemplateInterceptor interceptor) {
        if (restTemplate == null || interceptor == null) {
            return restTemplate;
        }
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        for (ClientHttpRequestInterceptor existing : interceptors) {
            if (existing instanceof InternalCallRestTemplateInterceptor) {
                return restTemplate;
            }
        }
        interceptors.add(interceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}
