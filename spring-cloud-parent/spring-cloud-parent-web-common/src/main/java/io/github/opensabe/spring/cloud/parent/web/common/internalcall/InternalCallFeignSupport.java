package io.github.opensabe.spring.cloud.parent.web.common.internalcall;

import feign.Request;
import io.github.opensabe.common.internalcall.CallerIdentityProvider;
import io.github.opensabe.common.internalcall.InternalCallProperties;
import io.github.opensabe.common.internalcall.InternalCallRequestHeaderSupport;
import io.github.opensabe.common.internalcall.InternalCallSignedHeaders;
import io.github.opensabe.common.internalcall.InternalCallSigner;
import io.github.opensabe.common.internalcall.InternalHostMatcher;
import lombok.extern.log4j.Log4j2;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Signs Feign requests after load-balancer rewrite, without mutating RequestTemplate.
 */
@Log4j2
public class InternalCallFeignSupport {

    private final InternalCallProperties properties;
    private final InternalCallSigner signer;
    private final CallerIdentityProvider callerIdentityProvider;
    private final InternalHostMatcher hostMatcher;

    public InternalCallFeignSupport(
            InternalCallProperties properties,
            InternalCallSigner signer,
            CallerIdentityProvider callerIdentityProvider,
            InternalHostMatcher hostMatcher
    ) {
        this.properties = properties;
        this.signer = signer;
        this.callerIdentityProvider = callerIdentityProvider;
        this.hostMatcher = hostMatcher;
    }

    /**
     * @return original request when disabled; otherwise a new Request with mechanism headers stripped
     *         and optionally signed (internal hosts only). Never mutates request.requestTemplate().
     */
    public Request prepare(Request request) {
        if (!properties.isEnabled()
                || properties.getOutbound() == null
                || !properties.getOutbound().isEnabled()
                || !properties.getOutbound().isSignAllFeign()) {
            return request;
        }
        Map<String, Collection<String>> cleaned = stripCaseInsensitive(request.headers());
        try {
            String host = hostOf(request.url());
            boolean internal = hostMatcher.isInternalHost(host, null);
            if (internal && properties.hasSecretConfigured()) {
                Optional<InternalCallSignedHeaders> signed = signer.sign(
                        callerIdentityProvider.getCallerService(),
                        callerIdentityProvider.getCallerIp()
                );
                if (signed.isPresent()) {
                    applySigned(cleaned, signed.get());
                }
            }
            return Request.create(
                    request.httpMethod(),
                    request.url(),
                    cleaned,
                    request.body(),
                    request.charset(),
                    request.requestTemplate()
            );
        } catch (Throwable t) {
            log.warn("InternalCallFeignSupport-prepare: skip signing, still strip: {}", t.toString());
            try {
                return Request.create(
                        request.httpMethod(),
                        request.url(),
                        cleaned,
                        request.body(),
                        request.charset(),
                        request.requestTemplate()
                );
            } catch (Throwable stripError) {
                log.warn("InternalCallFeignSupport-prepare: strip rebuild failed: {}", stripError.toString());
                return request;
            }
        }
    }

    private Map<String, Collection<String>> stripCaseInsensitive(Map<String, Collection<String>> original) {
        Map<String, Collection<String>> cleaned = new LinkedHashMap<>();
        List<String> mechanism = InternalCallRequestHeaderSupport.mechanismHeaderNames(properties);
        for (Map.Entry<String, Collection<String>> e : original.entrySet()) {
            if (InternalCallRequestHeaderSupport.isMechanismHeader(e.getKey(), mechanism)) {
                continue;
            }
            cleaned.put(e.getKey(), e.getValue() == null ? List.of() : new ArrayList<>(e.getValue()));
        }
        return cleaned;
    }

    private void applySigned(Map<String, Collection<String>> headers, InternalCallSignedHeaders signed) {
        InternalCallProperties.Headers h = properties.getHeaders();
        headers.put(h.getCallerService(), List.of(signed.callerService()));
        headers.put(h.getCallerIp(), List.of(signed.callerIp()));
        headers.put(h.getTs(), List.of(signed.ts()));
        headers.put(h.getNonce(), List.of(signed.nonce()));
        headers.put(h.getSign(), List.of(signed.sign()));
        headers.put(h.getVer(), List.of(signed.ver()));
    }

    private static String hostOf(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return null;
        }
    }
}
