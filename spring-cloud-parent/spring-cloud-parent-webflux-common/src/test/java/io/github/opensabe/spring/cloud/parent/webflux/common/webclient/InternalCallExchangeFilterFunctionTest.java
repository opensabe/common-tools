package io.github.opensabe.spring.cloud.parent.webflux.common.webclient;

import io.github.opensabe.common.internalcall.CallerIdentityProvider;
import io.github.opensabe.common.internalcall.InternalCallProperties;
import io.github.opensabe.common.internalcall.InternalCallSigner;
import io.github.opensabe.common.internalcall.InternalHostMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InternalCallExchangeFilterFunctionTest {

    @Test
    @DisplayName("Mono 被订阅两次得到两个 nonce，host 仍是短服务名")
    void deferResignes() {
        InternalCallProperties props = new InternalCallProperties();
        props.setEnabled(true);
        props.setSharedSecret("secret");
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000L), ZoneOffset.UTC);
        InternalCallExchangeFilterFunction filter = new InternalCallExchangeFilterFunction(
                props,
                new InternalCallSigner(props, clock),
                new CallerIdentityProvider(new MockEnvironment().withProperty("spring.application.name", "svc-a")),
                new InternalHostMatcher(List.of("*.svc.cluster.local")),
                "order-service"
        );

        ClientRequest request = ClientRequest.create(
                org.springframework.http.HttpMethod.GET,
                URI.create("http://order-service/api")
        ).build();

        List<String> nonces = new ArrayList<>();
        List<String> hosts = new ArrayList<>();
        ExchangeFunction next = req -> {
            nonces.add(req.headers().getFirst(props.getHeaders().getNonce()));
            hosts.add(req.url().getHost());
            return Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK).build());
        };

        Mono<ClientResponse> mono = filter.filter(request, next);
        mono.block();
        mono.block();

        assertEquals(2, nonces.size());
        assertNotNull(nonces.get(0));
        assertNotEquals(nonces.get(0), nonces.get(1));
        assertEquals("order-service", hosts.get(0));
        assertEquals("order-service", hosts.get(1));
    }
}
