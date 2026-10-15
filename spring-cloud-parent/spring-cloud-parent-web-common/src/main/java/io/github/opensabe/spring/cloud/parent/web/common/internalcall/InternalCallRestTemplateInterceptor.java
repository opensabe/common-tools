package io.github.opensabe.spring.cloud.parent.web.common.internalcall;

import io.github.opensabe.common.internalcall.CallerIdentityProvider;
import io.github.opensabe.common.internalcall.InternalCallProperties;
import io.github.opensabe.common.internalcall.InternalCallRequestHeaderSupport;
import io.github.opensabe.common.internalcall.InternalCallSignedHeaders;
import io.github.opensabe.common.internalcall.InternalCallSigner;
import io.github.opensabe.common.internalcall.InternalHostMatcher;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Log4j2
public class InternalCallRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final InternalCallProperties properties;
    private final InternalCallSigner signer;
    private final CallerIdentityProvider callerIdentityProvider;
    private final InternalHostMatcher hostMatcher;

    public InternalCallRestTemplateInterceptor(
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

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (!properties.isEnabled()
                || properties.getOutbound() == null
                || !properties.getOutbound().isEnabled()) {
            return execution.execute(request, body);
        }
        try {
            List<String> mechanism = InternalCallRequestHeaderSupport.mechanismHeaderNames(properties);
            for (String name : mechanism) {
                request.getHeaders().remove(name);
            }
            String host = request.getURI().getHost();
            if (hostMatcher.isInternalHost(host, null) && properties.hasSecretConfigured()) {
                Optional<InternalCallSignedHeaders> signed = signer.sign(
                        callerIdentityProvider.getCallerService(),
                        callerIdentityProvider.getCallerIp()
                );
                if (signed.isPresent()) {
                    InternalCallProperties.Headers h = properties.getHeaders();
                    InternalCallSignedHeaders s = signed.get();
                    request.getHeaders().set(h.getCallerService(), s.callerService());
                    request.getHeaders().set(h.getCallerIp(), s.callerIp());
                    request.getHeaders().set(h.getTs(), s.ts());
                    request.getHeaders().set(h.getNonce(), s.nonce());
                    request.getHeaders().set(h.getSign(), s.sign());
                    request.getHeaders().set(h.getVer(), s.ver());
                }
            }
        } catch (Throwable t) {
            log.warn("InternalCallRestTemplateInterceptor-intercept: skip signing: {}", t.toString());
        }
        return execution.execute(request, body);
    }
}
