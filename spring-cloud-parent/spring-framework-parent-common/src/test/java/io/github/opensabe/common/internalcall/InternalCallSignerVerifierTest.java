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

package io.github.opensabe.common.internalcall;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalCallSignerVerifierTest {

    private static final String SECRET = "test-secret-vector-001";
    private static final String SERVICE = "order-service";
    private static final String IP = "10.0.1.5";
    private static final long TS = 1_700_000_000_000L;
    private static final String NONCE = "0123456789abcdef0123456789abcdef";

    @Test
    @DisplayName("样例向量：固定 secret/ts/nonce 得到固定 Base64URL 签名")
    void sampleVector() throws Exception {
        String canonical = InternalCallSigner.buildCanonical(SERVICE, IP, Long.toString(TS), NONCE);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String expected = Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));

        InternalCallProperties props = propsWithSecret(SECRET);
        Clock clock = Clock.fixed(Instant.ofEpochMilli(TS), ZoneOffset.UTC);
        InternalCallSigner signer = new InternalCallSigner(props, clock);
        Optional<InternalCallSignedHeaders> signed = signer.sign(SERVICE, IP, TS, NONCE);
        assertTrue(signed.isPresent());
        assertEquals(expected, signed.get().sign());
        assertFalse(signed.get().sign().contains("="));
        assertFalse(signed.get().sign().contains("+"));
        assertFalse(signed.get().sign().contains("/"));

        InternalCallVerifier verifier = new InternalCallVerifier(props, signer, clock);
        InternalCallVerifyResult result = verifier.verify(
                SERVICE, IP, Long.toString(TS), NONCE, signed.get().sign(), "1", IP
        );
        assertTrue(result.success());
    }

    @Test
    @DisplayName("错 secret 验签失败")
    void wrongSecret() {
        InternalCallProperties props = propsWithSecret(SECRET);
        Clock clock = Clock.fixed(Instant.ofEpochMilli(TS), ZoneOffset.UTC);
        InternalCallSigner signer = new InternalCallSigner(props, clock);
        InternalCallSignedHeaders signed = signer.sign(SERVICE, IP, TS, NONCE).orElseThrow();

        InternalCallProperties bad = propsWithSecret("other-secret");
        InternalCallVerifier verifier = new InternalCallVerifier(bad, new InternalCallSigner(bad, clock), clock);
        assertEquals(InternalCallFailureReason.BAD_SIGNATURE,
                verifier.verify(SERVICE, IP, Long.toString(TS), NONCE, signed.sign(), "1", IP).reason());
    }

    @Test
    @DisplayName("previous secret 轮换仍能验过")
    void previousSecretRotation() {
        InternalCallProperties signProps = propsWithSecret("new-secret");
        Clock clock = Clock.fixed(Instant.ofEpochMilli(TS), ZoneOffset.UTC);
        InternalCallSignedHeaders signed = new InternalCallSigner(signProps, clock)
                .sign(SERVICE, IP, TS, NONCE).orElseThrow();

        InternalCallProperties verifyProps = new InternalCallProperties();
        verifyProps.getSecrets().setCurrent("brand-new");
        verifyProps.getSecrets().setPrevious("new-secret");
        InternalCallVerifier verifier = new InternalCallVerifier(
                verifyProps, new InternalCallSigner(verifyProps, clock), clock);
        assertTrue(verifier.verify(SERVICE, IP, Long.toString(TS), NONCE, signed.sign(), "1", IP).success());
    }

    @Test
    @DisplayName("过期 ts 返回 EXPIRED")
    void expired() {
        InternalCallProperties props = propsWithSecret(SECRET);
        Clock signClock = Clock.fixed(Instant.ofEpochMilli(TS), ZoneOffset.UTC);
        InternalCallSignedHeaders signed = new InternalCallSigner(props, signClock)
                .sign(SERVICE, IP, TS, NONCE).orElseThrow();
        Clock late = Clock.fixed(Instant.ofEpochMilli(TS + props.getSkewMs() + 1), ZoneOffset.UTC);
        InternalCallVerifier verifier = new InternalCallVerifier(props, new InternalCallSigner(props, late), late);
        assertEquals(InternalCallFailureReason.EXPIRED,
                verifier.verify(SERVICE, IP, Long.toString(TS), NONCE, signed.sign(), "1", IP).reason());
    }

    @Test
    @DisplayName("拒绝带填充的 Base64 签名")
    void rejectPadding() {
        InternalCallProperties props = propsWithSecret(SECRET);
        Clock clock = Clock.fixed(Instant.ofEpochMilli(TS), ZoneOffset.UTC);
        InternalCallVerifier verifier = new InternalCallVerifier(props, new InternalCallSigner(props, clock), clock);
        assertEquals(InternalCallFailureReason.BAD_SIGNATURE,
                verifier.verify(SERVICE, IP, Long.toString(TS), NONCE, "abc=", "1", IP).reason());
    }

    @Test
    @DisplayName("空 secret 不加签且不抛")
    void emptySecretNoSign() {
        InternalCallProperties props = new InternalCallProperties();
        props.setSharedSecret("");
        Clock clock = Clock.fixed(Instant.ofEpochMilli(TS), ZoneOffset.UTC);
        assertTrue(new InternalCallSigner(props, clock).sign(SERVICE, IP).isEmpty());
        InternalCallVerifier verifier = new InternalCallVerifier(props, new InternalCallSigner(props, clock), clock);
        assertEquals(InternalCallFailureReason.NO_SECRET_CONFIGURED,
                verifier.verify(SERVICE, IP, Long.toString(TS), NONCE, "x", "1", IP).reason());
    }

    @Test
    @DisplayName("双 secret 都做常数时间比较")
    void bothSecretsCompared() throws Exception {
        CountingSigner counting = new CountingSigner(propsWithSecrets("current-key", "previous-key"),
                Clock.fixed(Instant.ofEpochMilli(TS), ZoneOffset.UTC));
        InternalCallSignedHeaders signed = counting.sign(SERVICE, IP, TS, NONCE).orElseThrow();
        counting.hmacCalls.set(0);
        InternalCallVerifier verifier = new InternalCallVerifier(
                propsWithSecrets("current-key", "previous-key"), counting,
                Clock.fixed(Instant.ofEpochMilli(TS), ZoneOffset.UTC));
        assertTrue(verifier.verify(SERVICE, IP, Long.toString(TS), NONCE, signed.sign(), "1", IP).success());
        assertEquals(2, counting.hmacCalls.get());
    }

    @Test
    @DisplayName("解码长度不是 32 字节失败")
    void wrongMacLength() {
        InternalCallProperties props = propsWithSecret(SECRET);
        Clock clock = Clock.fixed(Instant.ofEpochMilli(TS), ZoneOffset.UTC);
        InternalCallVerifier verifier = new InternalCallVerifier(props, new InternalCallSigner(props, clock), clock);
        String shortSign = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[16]);
        assertEquals(InternalCallFailureReason.BAD_SIGNATURE,
                verifier.verify(SERVICE, IP, Long.toString(TS), NONCE, shortSign, "1", IP).reason());
    }

    @Test
    @DisplayName("缺头 MISSING_HEADER")
    void missingHeader() {
        InternalCallProperties props = propsWithSecret(SECRET);
        Clock clock = Clock.fixed(Instant.ofEpochMilli(TS), ZoneOffset.UTC);
        InternalCallVerifier verifier = new InternalCallVerifier(props, new InternalCallSigner(props, clock), clock);
        assertEquals(InternalCallFailureReason.MISSING_HEADER,
                verifier.verify(null, IP, Long.toString(TS), NONCE, "x", "1", IP).reason());
    }

    @Test
    @DisplayName("不支持的版本")
    void unsupportedVersion() {
        InternalCallProperties props = propsWithSecret(SECRET);
        Clock clock = Clock.fixed(Instant.ofEpochMilli(TS), ZoneOffset.UTC);
        InternalCallVerifier verifier = new InternalCallVerifier(props, new InternalCallSigner(props, clock), clock);
        assertEquals(InternalCallFailureReason.UNSUPPORTED_VERSION,
                verifier.verify(SERVICE, IP, Long.toString(TS), NONCE, "x", "99", IP).reason());
    }

    @Test
    @DisplayName("bindPeerIp 时 peer 不一致返回 PEER_IP_MISMATCH")
    void bindPeerIpMismatch() {
        InternalCallProperties props = propsWithSecret(SECRET);
        props.getInbound().setBindPeerIp(true);
        Clock clock = Clock.fixed(Instant.ofEpochMilli(TS), ZoneOffset.UTC);
        InternalCallSigner signer = new InternalCallSigner(props, clock);
        InternalCallSignedHeaders signed = signer.sign(SERVICE, IP, TS, NONCE).orElseThrow();
        InternalCallVerifier verifier = new InternalCallVerifier(props, signer, clock);
        assertEquals(InternalCallFailureReason.PEER_IP_MISMATCH,
                verifier.verify(SERVICE, IP, Long.toString(TS), NONCE, signed.sign(), "1", "10.9.9.9").reason());
    }

    private static InternalCallProperties propsWithSecret(String secret) {
        InternalCallProperties props = new InternalCallProperties();
        props.setSharedSecret(secret);
        return props;
    }

    private static InternalCallProperties propsWithSecrets(String current, String previous) {
        InternalCallProperties props = new InternalCallProperties();
        props.getSecrets().setCurrent(current);
        props.getSecrets().setPrevious(previous);
        return props;
    }

    static class CountingSigner extends InternalCallSigner {
        final AtomicInteger hmacCalls = new AtomicInteger();

        CountingSigner(InternalCallProperties properties, Clock clock) {
            super(properties, clock);
        }

        @Override
        byte[] hmac(String secret, String canonical) throws Exception {
            hmacCalls.incrementAndGet();
            return super.hmac(secret, canonical);
        }
    }
}
