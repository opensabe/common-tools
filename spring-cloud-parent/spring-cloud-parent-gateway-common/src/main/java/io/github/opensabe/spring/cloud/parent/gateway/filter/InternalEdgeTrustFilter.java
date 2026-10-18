package io.github.opensabe.spring.cloud.parent.gateway.filter;

import io.github.opensabe.common.internalcall.InternalCallFailureHandler;
import io.github.opensabe.common.internalcall.InternalCallFailureReason;
import io.github.opensabe.common.internalcall.InternalCallPathMatcher;
import io.github.opensabe.common.internalcall.InternalCallProperties;
import io.github.opensabe.common.internalcall.InternalCallVerifier;
import io.github.opensabe.common.internalcall.Ipv4CidrSet;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Observe-only edge trust: TCP peer must be in trusted IPv4 CIDRs; optional edge-token.
 * Peer is the Netty channel remote address when available (survives NATIVE forwarded-headers).
 */
@Log4j2
@Component
public class InternalEdgeTrustFilter extends AbstractTracedFilter {

    public static final int ORDER = TraceIdFilter.ORDER + 2;

    private final InternalCallProperties properties;
    private final Ipv4CidrSet trustedCidrs;
    private final InternalCallVerifier verifier;
    private final InternalCallFailureHandler failureHandler;
    private final InternalCallPathMatcher pathMatcher;

    @Autowired
    public InternalEdgeTrustFilter(
            InternalCallProperties properties,
            Ipv4CidrSet trustedCidrs,
            InternalCallVerifier verifier,
            InternalCallFailureHandler failureHandler,
            InternalCallPathMatcher pathMatcher
    ) {
        this.properties = properties;
        this.trustedCidrs = trustedCidrs;
        this.verifier = verifier;
        this.failureHandler = failureHandler;
        this.pathMatcher = pathMatcher;
    }

    @Override
    protected Mono<Void> traced(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            if (!properties.isEnabled()
                    || properties.getEdge() == null
                    || !properties.getEdge().isEnabled()) {
                return chain.filter(exchange);
            }
            String path = exchange.getRequest().getPath().value();
            if (pathMatcher.isExcluded(path, null)) {
                return chain.filter(exchange);
            }
            String peer = peerHost(exchange.getRequest());
            if (peer == null) {
                failureHandler.report(
                        InternalCallFailureReason.UNTRUSTED_EDGE,
                        "",
                        path,
                        null,
                        properties.getEdge().getOnFailure()
                );
                return chain.filter(exchange);
            }
            Ipv4CidrSet.PeerKind kind = Ipv4CidrSet.classifyPeer(peer);
            if (kind == Ipv4CidrSet.PeerKind.IPV6_OR_MAPPED) {
                failureHandler.report(
                        InternalCallFailureReason.UNSUPPORTED_PEER_IP,
                        peer,
                        path,
                        null,
                        properties.getEdge().getOnFailure()
                );
                return chain.filter(exchange);
            }
            if (kind != Ipv4CidrSet.PeerKind.IPV4 || !trustedCidrs.contains(peer)) {
                failureHandler.report(
                        InternalCallFailureReason.UNTRUSTED_EDGE,
                        peer,
                        path,
                        null,
                        properties.getEdge().getOnFailure()
                );
                return chain.filter(exchange);
            }
            String configuredToken = properties.getEdge().getEdgeToken();
            if (configuredToken != null && !configuredToken.isBlank()) {
                String provided = exchange.getRequest().getHeaders()
                        .getFirst(properties.getHeaders().getEdgeToken());
                if (!verifier.verifyEdgeToken(configuredToken, provided)) {
                    failureHandler.report(
                            InternalCallFailureReason.BAD_EDGE_TOKEN,
                            peer,
                            path,
                            null,
                            properties.getEdge().getOnFailure()
                    );
                }
            }
        } catch (Throwable t) {
            log.warn("InternalEdgeTrustFilter-traced: INTERNAL_ERROR: {}", t.toString());
            try {
                failureHandler.report(
                        InternalCallFailureReason.INTERNAL_ERROR,
                        "",
                        exchange.getRequest().getPath().value(),
                        null,
                        properties.getEdge() == null ? null : properties.getEdge().getOnFailure()
                );
            } catch (Throwable ignored) {
                // swallow
            }
        }
        return chain.filter(exchange);
    }

    static String peerHost(ServerHttpRequest request) {
        InetSocketAddress remote = tcpPeerAddress(request);
        if (remote == null) {
            return null;
        }
        InetAddress address = remote.getAddress();
        if (address == null) {
            return null;
        }
        return address.getHostAddress();
    }

    /**
     * Prefer Reactor Netty {@code connectionRemoteAddress()} (true TCP peer).
     * Falls back to {@link ServerHttpRequest#getRemoteAddress()} for mocks / non-Netty.
     */
    static InetSocketAddress tcpPeerAddress(ServerHttpRequest request) {
        try {
            Object nativeRequest = ServerHttpRequestDecorator.getNativeRequest(request);
            if (nativeRequest instanceof HttpServerRequest http) {
                SocketAddress peer = http.connectionRemoteAddress();
                if (peer instanceof InetSocketAddress inet) {
                    return inet;
                }
            }
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            // MockServerHttpRequest: "No running server, no native request"
        }
        return request.getRemoteAddress();
    }

    @Override
    protected int ordered() {
        return ORDER;
    }
}
