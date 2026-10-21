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

package io.github.opensabe.spring.cloud.parent.gateway.filter;

import io.github.opensabe.common.internalcall.CallerIdentityProvider;
import io.github.opensabe.common.internalcall.InternalCallPathMatcher;
import io.github.opensabe.common.internalcall.InternalCallProperties;
import io.github.opensabe.common.internalcall.InternalCallRequestHeaderSupport;
import io.github.opensabe.common.internalcall.InternalCallSignedHeaders;
import io.github.opensabe.common.internalcall.InternalCallSigner;
import io.github.opensabe.common.internalcall.InternalHostMatcher;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Strip forged mechanism headers and sign outbound hop to internal targets.
 * Order is LOWEST_PRECEDENCE - 2 so it runs after retry/LB and before WebsocketRoutingFilter (MAX-1).
 */
@Log4j2
@Component
public class InternalCallOutboundGatewayFilter extends AbstractTracedFilter {

    public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 2;

    private final InternalCallProperties properties;
    private final InternalCallSigner signer;
    private final CallerIdentityProvider callerIdentityProvider;
    private final InternalHostMatcher hostMatcher;
    private final InternalCallPathMatcher pathMatcher;

    @Autowired
    public InternalCallOutboundGatewayFilter(
            InternalCallProperties properties,
            InternalCallSigner signer,
            CallerIdentityProvider callerIdentityProvider,
            InternalHostMatcher hostMatcher,
            InternalCallPathMatcher pathMatcher
    ) {
        this.properties = properties;
        this.signer = signer;
        this.callerIdentityProvider = callerIdentityProvider;
        this.hostMatcher = hostMatcher;
        this.pathMatcher = pathMatcher;
    }

    @Override
    protected Mono<Void> traced(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!properties.isEnabled()
                || properties.getOutbound() == null
                || !properties.getOutbound().isEnabled()) {
            return chain.filter(exchange);
        }
        try {
            String path = exchange.getRequest().getPath().value();
            boolean excluded = pathMatcher.isExcluded(path, null);
            boolean shouldSign = !excluded && shouldSign(exchange);
            InternalCallSignedHeaders signed = null;
            if (shouldSign && properties.hasSecretConfigured()) {
                Optional<InternalCallSignedHeaders> opt = signer.sign(
                        callerIdentityProvider.getCallerService(),
                        callerIdentityProvider.getCallerIp()
                );
                if (opt.isPresent()) {
                    signed = opt.get();
                }
            }
            return chain.filter(mutateStripAndMaybeSign(exchange, signed));
        } catch (Throwable t) {
            log.warn("InternalCallOutboundGatewayFilter-traced: skip signing, still strip: {}", t.toString());
            try {
                return chain.filter(mutateStripAndMaybeSign(exchange, null));
            } catch (Throwable stripError) {
                log.warn("InternalCallOutboundGatewayFilter-traced: strip also failed: {}", stripError.toString());
                return chain.filter(exchange);
            }
        }
    }

    private ServerWebExchange mutateStripAndMaybeSign(ServerWebExchange exchange, InternalCallSignedHeaders signed) {
        List<String> mechanism = InternalCallRequestHeaderSupport.mechanismHeaderNames(properties);
        return exchange.mutate()
                .request(builder -> builder.headers(httpHeaders -> {
                    for (String name : mechanism) {
                        httpHeaders.remove(name);
                    }
                    if (signed != null) {
                        InternalCallProperties.Headers h = properties.getHeaders();
                        httpHeaders.set(h.getCallerService(), signed.callerService());
                        httpHeaders.set(h.getCallerIp(), signed.callerIp());
                        httpHeaders.set(h.getTs(), signed.ts());
                        httpHeaders.set(h.getNonce(), signed.nonce());
                        httpHeaders.set(h.getSign(), signed.sign());
                        httpHeaders.set(h.getVer(), signed.ver());
                    }
                }))
                .build();
    }

    /**
     * Post-LB {@code GATEWAY_REQUEST_URL_ATTR} is already instance http(s). Detect lb via
     * {@code GATEWAY_SCHEME_PREFIX_ATTR} or the original route URI scheme.
     */
    boolean shouldSign(ServerWebExchange exchange) {
        String schemePrefix = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR);
        if (schemePrefix != null && schemePrefix.equalsIgnoreCase("lb")) {
            return true;
        }
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route != null && route.getUri() != null) {
            String routeScheme = route.getUri().getScheme();
            if (routeScheme != null && routeScheme.equalsIgnoreCase("forward")) {
                return false;
            }
            if (routeScheme != null && routeScheme.equalsIgnoreCase("lb")) {
                return true;
            }
        }
        URI target = resolveTarget(exchange);
        return shouldSignTarget(target);
    }

    static URI resolveTarget(ServerWebExchange exchange) {
        URI requestUrl = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        if (requestUrl != null) {
            return requestUrl;
        }
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route != null) {
            return route.getUri();
        }
        return null;
    }

    boolean shouldSignTarget(URI target) {
        if (target == null) {
            return false;
        }
        String scheme = target.getScheme();
        if (scheme != null && scheme.equalsIgnoreCase("forward")) {
            return false;
        }
        if (scheme != null && scheme.equalsIgnoreCase("lb")) {
            return true;
        }
        String host = target.getHost();
        return hostMatcher.isInternalHost(host, null);
    }

    @Override
    protected int ordered() {
        return ORDER;
    }
}
