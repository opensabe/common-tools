/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.opensabe.spring.cloud.parent.webflux.common.webclient;

import io.github.opensabe.common.internalcall.CallerIdentityProvider;
import io.github.opensabe.common.internalcall.InternalCallProperties;
import io.github.opensabe.common.internalcall.InternalCallRequestHeaderSupport;
import io.github.opensabe.common.internalcall.InternalCallSignedHeaders;
import io.github.opensabe.common.internalcall.InternalCallSigner;
import io.github.opensabe.common.internalcall.InternalHostMatcher;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * Signs internal WebClient requests. Must be registered after retry and before LB,
 * returning {@code Mono.defer} so each retry subscription re-signs.
 */
@Log4j2
public class InternalCallExchangeFilterFunction implements ExchangeFilterFunction {

    private final InternalCallProperties properties;
    private final InternalCallSigner signer;
    private final CallerIdentityProvider callerIdentityProvider;
    private final InternalHostMatcher hostMatcher;
    private final String serviceName;

    public InternalCallExchangeFilterFunction(
            InternalCallProperties properties,
            InternalCallSigner signer,
            CallerIdentityProvider callerIdentityProvider,
            InternalHostMatcher hostMatcher,
            String serviceName
    ) {
        this.properties = properties;
        this.signer = signer;
        this.callerIdentityProvider = callerIdentityProvider;
        this.hostMatcher = hostMatcher;
        this.serviceName = serviceName;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        if (!properties.isEnabled()
                || properties.getOutbound() == null
                || !properties.getOutbound().isEnabled()) {
            return next.exchange(request);
        }
        return Mono.defer(() -> next.exchange(prepare(request)));
    }

    ClientRequest prepare(ClientRequest request) {
        ClientRequest.Builder builder = ClientRequest.from(request);
        List<String> mechanism = InternalCallRequestHeaderSupport.mechanismHeaderNames(properties);
        for (String name : mechanism) {
            builder.headers(headers -> headers.remove(name));
        }
        try {
            String host = request.url().getHost();
            if (hostMatcher.isInternalHost(host, serviceName) && properties.hasSecretConfigured()) {
                Optional<InternalCallSignedHeaders> signed = signer.sign(
                        callerIdentityProvider.getCallerService(),
                        callerIdentityProvider.getCallerIp()
                );
                if (signed.isPresent()) {
                    InternalCallSignedHeaders s = signed.get();
                    InternalCallProperties.Headers h = properties.getHeaders();
                    builder.headers(headers -> {
                        headers.set(h.getCallerService(), s.callerService());
                        headers.set(h.getCallerIp(), s.callerIp());
                        headers.set(h.getTs(), s.ts());
                        headers.set(h.getNonce(), s.nonce());
                        headers.set(h.getSign(), s.sign());
                        headers.set(h.getVer(), s.ver());
                    });
                }
            }
            return builder.build();
        } catch (Throwable t) {
            log.warn("InternalCallExchangeFilterFunction-prepare: skip signing, still strip: {}", t.toString());
            return builder.build();
        }
    }
}
