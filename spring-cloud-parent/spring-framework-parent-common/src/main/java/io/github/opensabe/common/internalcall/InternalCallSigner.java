package io.github.opensabe.common.internalcall;

import lombok.extern.log4j.Log4j2;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Issues HMAC-SHA256 signatures for outbound internal HTTP calls.
 */
@Log4j2
public class InternalCallSigner {

    private static final String HMAC_ALG = "HmacSHA256";

    private final InternalCallProperties properties;
    private final Clock clock;
    private final SecureRandom secureRandom;
    private final ThreadLocal<Mac> macThreadLocal = ThreadLocal.withInitial(() -> {
        try {
            return Mac.getInstance(HMAC_ALG);
        } catch (Exception e) {
            throw new IllegalStateException("HmacSHA256 not available", e);
        }
    });

    public InternalCallSigner(InternalCallProperties properties, Clock clock) {
        this(properties, clock, new SecureRandom());
    }

    public InternalCallSigner(InternalCallProperties properties, Clock clock, SecureRandom secureRandom) {
        this.properties = properties;
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    /**
     * Sign with a fixed ts/nonce (for tests / sample vectors).
     */
    public Optional<InternalCallSignedHeaders> sign(String callerService, String callerIp, long tsMillis, String nonceHex) {
        String secret = properties.resolveCurrentSecret();
        if (secret == null || secret.isBlank()) {
            return Optional.empty();
        }
        if (!isValidField(callerService, InternalCallHeaders.MAX_SERVICE_LEN)
                || !isValidField(callerIp, InternalCallHeaders.MAX_IP_LEN)
                || !isValidField(nonceHex, InternalCallHeaders.MAX_NONCE_LEN)) {
            log.warn("InternalCallSigner-sign: invalid field length or illegal character, skip signing");
            return Optional.empty();
        }
        String ts = Long.toString(tsMillis);
        if (ts.length() > InternalCallHeaders.MAX_TS_LEN) {
            log.warn("InternalCallSigner-sign: timestamp too long, skip signing");
            return Optional.empty();
        }
        String canonical = buildCanonical(callerService, callerIp, ts, nonceHex);
        try {
            byte[] mac = hmac(secret, canonical);
            String sign = Base64.getUrlEncoder().withoutPadding().encodeToString(mac);
            return Optional.of(new InternalCallSignedHeaders(
                    callerService,
                    callerIp,
                    ts,
                    nonceHex,
                    sign,
                    InternalCallHeaders.PROTOCOL_VERSION
            ));
        } catch (Exception e) {
            log.warn("InternalCallSigner-sign: INTERNAL_ERROR while signing: {}", e.toString());
            return Optional.empty();
        }
    }

    public Optional<InternalCallSignedHeaders> sign(String callerService, String callerIp) {
        long ts = clock.millis();
        byte[] nonceBytes = new byte[InternalCallHeaders.NONCE_BYTES];
        secureRandom.nextBytes(nonceBytes);
        String nonce = HexFormat.of().formatHex(nonceBytes);
        return sign(callerService, callerIp, ts, nonce);
    }

    /**
     * Compute raw HMAC for a secret (also used by verifier).
     */
    byte[] hmac(String secret, String canonical) throws Exception {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Empty key");
        }
        Mac mac = macThreadLocal.get();
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
        return mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
    }

    public static String buildCanonical(String callerService, String callerIp, String ts, String nonce) {
        return InternalCallHeaders.PROTOCOL_VERSION + '|'
                + callerService + '|'
                + callerIp + '|'
                + ts + '|'
                + nonce;
    }

    static boolean isValidField(String value, int maxLen) {
        if (value == null || value.isEmpty() || value.length() > maxLen) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '|' || c < 0x20 || c == 0x7F) {
                return false;
            }
        }
        return true;
    }
}
