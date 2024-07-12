package io.github.opensabe.spring.cloud.parent.webflux.common.webclient.resilience4j;

import io.github.opensabe.spring.cloud.parent.webflux.common.config.WebClientConfigurationProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoOperator;
import reactor.core.publisher.Operators;

import static io.github.resilience4j.circuitbreaker.CallNotPermittedException.createCallNotPermittedException;

public class ClientResponseMonoCircuitBreaker extends MonoOperator<ClientResponse, ClientResponse> {
    private final CircuitBreaker circuitBreaker;
    private final ServiceInstance serviceInstance;
    private final WebClientConfigurationProperties.WebClientProperties webClientProperties;

    ClientResponseMonoCircuitBreaker(Mono<? extends ClientResponse> source, CircuitBreaker circuitBreaker, ServiceInstance serviceInstance, WebClientConfigurationProperties.WebClientProperties webClientProperties) {
        super(source);
        this.circuitBreaker = circuitBreaker;
        this.serviceInstance = serviceInstance;
        this.webClientProperties = webClientProperties;
    }

    @Override
    public void subscribe(CoreSubscriber<? super ClientResponse> actual) {
        if (circuitBreaker.tryAcquirePermission()) {
            source.subscribe(new ClientResponseCircuitBreakerSubscriber(circuitBreaker, actual, serviceInstance, true, webClientProperties));
        } else {
            Operators.error(actual, createCallNotPermittedException(circuitBreaker));
        }
    }
}
