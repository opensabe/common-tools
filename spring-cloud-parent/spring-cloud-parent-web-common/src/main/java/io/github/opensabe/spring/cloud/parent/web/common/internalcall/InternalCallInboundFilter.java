package io.github.opensabe.spring.cloud.parent.web.common.internalcall;

import io.github.opensabe.common.internalcall.InternalCallAuthAttributes;
import io.github.opensabe.common.internalcall.InternalCallFailureHandler;
import io.github.opensabe.common.internalcall.InternalCallFailureReason;
import io.github.opensabe.common.internalcall.InternalCallPathMatcher;
import io.github.opensabe.common.internalcall.InternalCallProperties;
import io.github.opensabe.common.internalcall.InternalCallRequestHeaderSupport;
import io.github.opensabe.common.internalcall.InternalCallVerifyResult;
import io.github.opensabe.common.internalcall.InternalCallVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Observe-only inbound hop verification for Servlet microservices.
 */
@Log4j2
public class InternalCallInboundFilter extends OncePerRequestFilter implements Ordered {

    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 50;

    private final InternalCallProperties properties;
    private final InternalCallVerifier verifier;
    private final InternalCallFailureHandler failureHandler;
    private final InternalCallPathMatcher pathMatcher;

    public InternalCallInboundFilter(
            InternalCallProperties properties,
            InternalCallVerifier verifier,
            InternalCallFailureHandler failureHandler,
            InternalCallPathMatcher pathMatcher
    ) {
        this.properties = properties;
        this.verifier = verifier;
        this.failureHandler = failureHandler;
        this.pathMatcher = pathMatcher;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterNestedErrorDispatch(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        continueAfterProcessed(request, response, filterChain);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (!properties.isEnabled()
                    || properties.getInbound() == null
                    || !properties.getInbound().isEnabled()) {
                filterChain.doFilter(request, response);
                return;
            }
            if (Boolean.TRUE.equals(request.getAttribute(InternalCallAuthAttributes.PROCESSED))
                    || Boolean.TRUE.equals(request.getAttribute(InternalCallAuthAttributes.VERIFIED))) {
                continueAfterProcessed(request, response, filterChain);
                return;
            }
            String rawPath = request.getRequestURI();
            String contextPath = request.getContextPath();
            if (pathMatcher.isExcluded(rawPath, contextPath)) {
                filterChain.doFilter(request, response);
                return;
            }
            if (!properties.hasSecretConfigured()) {
                request.setAttribute(InternalCallAuthAttributes.PROCESSED, Boolean.TRUE);
                failureHandler.report(
                        InternalCallFailureReason.NO_SECRET_CONFIGURED,
                        peerIp(request),
                        safePath(rawPath, contextPath),
                        null
                );
                filterChain.doFilter(new HeaderHidingRequest(request, mechanismNames()), response);
                return;
            }

            InternalCallProperties.Headers h = properties.getHeaders();
            String callerService = request.getHeader(h.getCallerService());
            String callerIp = request.getHeader(h.getCallerIp());
            String ts = request.getHeader(h.getTs());
            String nonce = request.getHeader(h.getNonce());
            String sign = request.getHeader(h.getSign());
            String ver = request.getHeader(h.getVer());
            String peer = peerIp(request);

            InternalCallVerifyResult result = verifier.verify(callerService, callerIp, ts, nonce, sign, ver, peer);
            request.setAttribute(InternalCallAuthAttributes.PROCESSED, Boolean.TRUE);
            if (result.success()) {
                request.setAttribute(InternalCallAuthAttributes.VERIFIED, Boolean.TRUE);
                request.setAttribute(InternalCallAuthAttributes.CALLER_SERVICE, result.callerService());
                request.setAttribute(InternalCallAuthAttributes.CALLER_IP, result.callerIp());
                filterChain.doFilter(request, response);
                return;
            }
            failureHandler.report(
                    result.reason(),
                    peer,
                    safePath(rawPath, contextPath),
                    result.callerService() != null ? result.callerService() : callerService
            );
            filterChain.doFilter(new HeaderHidingRequest(request, mechanismNames()), response);
        } catch (Throwable t) {
            log.warn("InternalCallInboundFilter-doFilterInternal: INTERNAL_ERROR: {}", t.toString());
            try {
                request.setAttribute(InternalCallAuthAttributes.PROCESSED, Boolean.TRUE);
                failureHandler.report(
                        InternalCallFailureReason.INTERNAL_ERROR,
                        peerIp(request),
                        request.getRequestURI(),
                        null
                );
            } catch (Throwable ignored) {
                // swallow
            }
            filterChain.doFilter(new HeaderHidingRequest(request, mechanismNames()), response);
        }
    }

    private void continueAfterProcessed(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (Boolean.TRUE.equals(request.getAttribute(InternalCallAuthAttributes.VERIFIED))) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(new HeaderHidingRequest(request, mechanismNames()), response);
    }

    private List<String> mechanismNames() {
        return InternalCallRequestHeaderSupport.mechanismHeaderNames(properties);
    }

    private String safePath(String rawPath, String contextPath) {
        String normalized = pathMatcher.normalize(rawPath, contextPath);
        return normalized == null ? rawPath : normalized;
    }

    static String peerIp(HttpServletRequest request) {
        try {
            String remote = request.getRemoteAddr();
            if (remote == null || remote.isBlank()) {
                return null;
            }
            return remote;
        } catch (Exception e) {
            return null;
        }
    }

    static final class HeaderHidingRequest extends HttpServletRequestWrapper {
        private final List<String> hidden;

        HeaderHidingRequest(HttpServletRequest request, List<String> hidden) {
            super(request);
            this.hidden = hidden;
        }

        private boolean hidden(String name) {
            return InternalCallRequestHeaderSupport.isMechanismHeader(name, hidden);
        }

        @Override
        public String getHeader(String name) {
            if (hidden(name)) {
                return null;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (hidden(name)) {
                return Collections.emptyEnumeration();
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            names.removeIf(this::hidden);
            return Collections.enumeration(names);
        }
    }
}
