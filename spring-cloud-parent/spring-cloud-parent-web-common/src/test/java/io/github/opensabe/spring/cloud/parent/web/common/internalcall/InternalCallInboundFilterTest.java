package io.github.opensabe.spring.cloud.parent.web.common.internalcall;

import io.github.opensabe.common.internalcall.InternalCallAuthAttributes;
import io.github.opensabe.common.internalcall.InternalCallFailureHandler;
import io.github.opensabe.common.internalcall.InternalCallPathMatcher;
import io.github.opensabe.common.internalcall.InternalCallProperties;
import io.github.opensabe.common.internalcall.InternalCallSigner;
import io.github.opensabe.common.internalcall.InternalCallVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalCallInboundFilterTest {

    @Test
    @DisplayName("验签失败放行且下游看不到签名头")
    void failureHidesHeaders() throws Exception {
        InternalCallProperties props = enabledProps("secret");
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000L), ZoneOffset.UTC);
        InternalCallSigner signer = new InternalCallSigner(props, clock);
        InternalCallVerifier verifier = new InternalCallVerifier(props, signer, clock);
        InternalCallInboundFilter filter = new InternalCallInboundFilter(
                props, verifier, new InternalCallFailureHandler(props),
                new InternalCallPathMatcher(props.getExcludePathPatterns())
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/order");
        request.addHeader(props.getHeaders().getCallerService(), "gw");
        request.addHeader(props.getHeaders().getCallerIp(), "10.0.0.1");
        request.addHeader(props.getHeaders().getTs(), "1700000000000");
        request.addHeader(props.getHeaders().getNonce(), "abc");
        request.addHeader(props.getHeaders().getSign(), "not-a-valid-sign");
        request.addHeader(props.getHeaders().getVer(), "1");
        request.setRemoteAddr("10.0.0.2");

        AtomicReference<String> seenSign = new AtomicReference<>("unset");
        filter.doFilter(request, new MockHttpServletResponse(), (req, resp) -> {
            seenSign.set(((jakarta.servlet.http.HttpServletRequest) req).getHeader(props.getHeaders().getSign()));
        });
        assertNull(seenSign.get());
    }

    @Test
    @DisplayName("验签成功保留头并写入 attribute；二次进入跳过验签")
    void successSetsAttributeAndSkipsSecondPass() throws Exception {
        InternalCallProperties props = enabledProps("secret");
        long ts = 1_700_000_000_000L;
        Clock clock = Clock.fixed(Instant.ofEpochMilli(ts), ZoneOffset.UTC);
        InternalCallSigner signer = new InternalCallSigner(props, clock);
        var signed = signer.sign("gw", "10.0.0.1", ts, "0123456789abcdef0123456789abcdef").orElseThrow();
        CountingVerifier counting = new CountingVerifier(props, signer, clock);
        InternalCallInboundFilter filter = new InternalCallInboundFilter(
                props, counting, new InternalCallFailureHandler(props),
                new InternalCallPathMatcher(List.of())
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/order");
        request.setRemoteAddr("10.0.0.1");
        request.addHeader(props.getHeaders().getCallerService(), signed.callerService());
        request.addHeader(props.getHeaders().getCallerIp(), signed.callerIp());
        request.addHeader(props.getHeaders().getTs(), signed.ts());
        request.addHeader(props.getHeaders().getNonce(), signed.nonce());
        request.addHeader(props.getHeaders().getSign(), signed.sign());
        request.addHeader(props.getHeaders().getVer(), signed.ver());

        filter.doFilter(request, new MockHttpServletResponse(), (req, resp) -> {
        });
        assertTrue(Boolean.TRUE.equals(request.getAttribute(InternalCallAuthAttributes.VERIFIED)));
        assertEquals(1, counting.calls.get());

        filter.doFilter(request, new MockHttpServletResponse(), (req, resp) -> {
        });
        assertEquals(1, counting.calls.get());
    }

    @Test
    @DisplayName("无 secret 时仍剥头并标记 processed，二次进入不再报警")
    void noSecretHidesHeadersAndSkipsSecondPass() throws Exception {
        InternalCallProperties props = new InternalCallProperties();
        props.setEnabled(true);
        props.setSharedSecret("");
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000L), ZoneOffset.UTC);
        CountingVerifier counting = new CountingVerifier(props, new InternalCallSigner(props, clock), clock);
        InternalCallInboundFilter filter = new InternalCallInboundFilter(
                props, counting, new InternalCallFailureHandler(props),
                new InternalCallPathMatcher(List.of())
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/order");
        request.addHeader(props.getHeaders().getSign(), "forged");
        request.setRemoteAddr("10.0.0.1");

        AtomicReference<String> seen = new AtomicReference<>("unset");
        filter.doFilter(request, new MockHttpServletResponse(), (req, resp) -> {
            seen.set(((jakarta.servlet.http.HttpServletRequest) req).getHeader(props.getHeaders().getSign()));
        });
        assertNull(seen.get());
        assertEquals(0, counting.calls.get());

        filter.doFilter(request, new MockHttpServletResponse(), (req, resp) -> {
            seen.set(((jakarta.servlet.http.HttpServletRequest) req).getHeader(props.getHeaders().getSign()));
        });
        assertNull(seen.get());
        assertEquals(0, counting.calls.get());
    }

    @Test
    @DisplayName("验签失败后二次进入不再 HMAC")
    void failureSecondPassSkipsVerify() throws Exception {
        InternalCallProperties props = enabledProps("secret");
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000L), ZoneOffset.UTC);
        CountingVerifier counting = new CountingVerifier(props, new InternalCallSigner(props, clock), clock);
        InternalCallInboundFilter filter = new InternalCallInboundFilter(
                props, counting, new InternalCallFailureHandler(props),
                new InternalCallPathMatcher(List.of())
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/order");
        request.addHeader(props.getHeaders().getSign(), "bad");
        request.addHeader(props.getHeaders().getCallerService(), "gw");
        request.addHeader(props.getHeaders().getCallerIp(), "10.0.0.1");
        request.addHeader(props.getHeaders().getTs(), "1700000000000");
        request.addHeader(props.getHeaders().getNonce(), "abc");
        request.addHeader(props.getHeaders().getVer(), "1");
        request.setRemoteAddr("10.0.0.1");

        filter.doFilter(request, new MockHttpServletResponse(), (req, resp) -> {
        });
        assertEquals(1, counting.calls.get());
        filter.doFilter(request, new MockHttpServletResponse(), (req, resp) -> {
        });
        assertEquals(1, counting.calls.get());
    }

    @Test
    @DisplayName("decorate RestTemplate 幂等")
    void decorateIdempotent() {
        InternalCallProperties props = enabledProps("secret");
        Clock clock = Clock.systemUTC();
        InternalCallRestTemplateInterceptor interceptor = new InternalCallRestTemplateInterceptor(
                props,
                new InternalCallSigner(props, clock),
                new io.github.opensabe.common.internalcall.CallerIdentityProvider(
                        new org.springframework.mock.env.MockEnvironment().withProperty("spring.application.name", "svc")
                ),
                new io.github.opensabe.common.internalcall.InternalHostMatcher(List.of("*.svc.cluster.local"))
        );
        org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
        InternalCallClients.decorate(rt, interceptor);
        InternalCallClients.decorate(rt, interceptor);
        assertEquals(1, rt.getInterceptors().size());
    }

    private static InternalCallProperties enabledProps(String secret) {
        InternalCallProperties props = new InternalCallProperties();
        props.setEnabled(true);
        props.setSharedSecret(secret);
        return props;
    }

    static class CountingVerifier extends InternalCallVerifier {
        final AtomicInteger calls = new AtomicInteger();

        CountingVerifier(InternalCallProperties properties, InternalCallSigner signer, Clock clock) {
            super(properties, signer, clock);
        }

        @Override
        public io.github.opensabe.common.internalcall.InternalCallVerifyResult verify(
                String callerService, String callerIp, String ts, String nonce, String sign, String ver, String peerIp
        ) {
            calls.incrementAndGet();
            return super.verify(callerService, callerIp, ts, nonce, sign, ver, peerIp);
        }
    }
}
