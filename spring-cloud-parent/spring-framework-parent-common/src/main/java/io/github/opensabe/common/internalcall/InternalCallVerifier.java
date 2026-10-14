package io.github.opensabe.common.internalcall;

import lombok.extern.log4j.Log4j2;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;

/**
 * Verifies inbound hop signatures (observe-only; does not change HTTP status).
 */
@Log4j2
public class InternalCallVerifier {

    private final InternalCallProperties properties;
    private final InternalCallSigner signer;
    private final Clock clock;

    public InternalCallVerifier(InternalCallProperties properties, InternalCallSigner signer, Clock clock) {
        this.properties = properties;
        this.signer = signer;
        this.clock = clock;
    }

    public InternalCallVerifyResult verify(
            String callerService,
            String callerIp,
            String ts,
            String nonce,
            String sign,
            String ver,
            String peerIp
    ) {
        try {
            if (!properties.hasSecretConfigured()) {
                return InternalCallVerifyResult.fail(InternalCallFailureReason.NO_SECRET_CONFIGURED);
            }
            if (isBlank(callerService) || isBlank(callerIp) || isBlank(ts) || isBlank(nonce) || isBlank(sign) || isBlank(ver)) {
                return InternalCallVerifyResult.fail(InternalCallFailureReason.MISSING_HEADER);
            }
            if (!InternalCallHeaders.PROTOCOL_VERSION.equals(ver)) {
                return InternalCallVerifyResult.fail(InternalCallFailureReason.UNSUPPORTED_VERSION, callerService, callerIp);
            }
            if (!InternalCallSigner.isValidField(callerService, InternalCallHeaders.MAX_SERVICE_LEN)
                    || !InternalCallSigner.isValidField(callerIp, InternalCallHeaders.MAX_IP_LEN)
                    || !InternalCallSigner.isValidField(nonce, InternalCallHeaders.MAX_NONCE_LEN)
                    || sign.length() > InternalCallHeaders.MAX_SIGN_LEN) {
                return InternalCallVerifyResult.fail(InternalCallFailureReason.BAD_SIGNATURE, callerService, callerIp);
            }
            long tsMillis;
            try {
                tsMillis = Long.parseLong(ts);
            } catch (NumberFormatException e) {
                return InternalCallVerifyResult.fail(InternalCallFailureReason.BAD_TIMESTAMP, callerService, callerIp);
            }
            long now = clock.millis();
            long skew = Math.abs(now - tsMillis);
            if (skew > properties.getSkewMs()) {
                return InternalCallVerifyResult.fail(InternalCallFailureReason.EXPIRED, callerService, callerIp);
            }
            if (sign.indexOf('=') >= 0 || sign.indexOf('+') >= 0 || sign.indexOf('/') >= 0) {
                return InternalCallVerifyResult.fail(InternalCallFailureReason.BAD_SIGNATURE, callerService, callerIp);
            }
            byte[] provided;
            try {
                provided = Base64.getUrlDecoder().decode(sign);
            } catch (IllegalArgumentException e) {
                return InternalCallVerifyResult.fail(InternalCallFailureReason.BAD_SIGNATURE, callerService, callerIp);
            }
            if (provided.length != InternalCallHeaders.HMAC_RAW_BYTES) {
                return InternalCallVerifyResult.fail(InternalCallFailureReason.BAD_SIGNATURE, callerService, callerIp);
            }

            String canonical = InternalCallSigner.buildCanonical(callerService, callerIp, ts, nonce);
            boolean match = false;
            String current = properties.resolveCurrentSecret();
            String previous = properties.resolvePreviousSecret();
            // Always evaluate both when present to avoid leaking which key matched via early return.
            if (!current.isBlank()) {
                byte[] expected = signer.hmac(current, canonical);
                match |= MessageDigest.isEqual(expected, provided);
            }
            if (!previous.isBlank()) {
                byte[] expected = signer.hmac(previous, canonical);
                match |= MessageDigest.isEqual(expected, provided);
            }
            if (!match) {
                return InternalCallVerifyResult.fail(InternalCallFailureReason.BAD_SIGNATURE, callerService, callerIp);
            }

            if (properties.getInbound() != null && properties.getInbound().isBindPeerIp()) {
                if (peerIp == null || !peerIp.equals(callerIp)) {
                    return InternalCallVerifyResult.fail(InternalCallFailureReason.PEER_IP_MISMATCH, callerService, callerIp);
                }
            }
            return InternalCallVerifyResult.ok(callerService, callerIp);
        } catch (Throwable t) {
            log.warn("InternalCallVerifier-verify: INTERNAL_ERROR: {}", t.toString());
            return InternalCallVerifyResult.fail(InternalCallFailureReason.INTERNAL_ERROR, callerService, callerIp);
        }
    }

    public boolean verifyEdgeToken(String configuredToken, String provided) {
        if (configuredToken == null || configuredToken.isBlank()) {
            return true;
        }
        if (provided == null) {
            return false;
        }
        byte[] a = configuredToken.getBytes(StandardCharsets.UTF_8);
        byte[] b = provided.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
