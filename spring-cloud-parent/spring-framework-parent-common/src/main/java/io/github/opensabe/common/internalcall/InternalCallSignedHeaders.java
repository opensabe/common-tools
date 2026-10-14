package io.github.opensabe.common.internalcall;

/**
 * Values to set on an outbound request after signing.
 */
public record InternalCallSignedHeaders(
        String callerService,
        String callerIp,
        String ts,
        String nonce,
        String sign,
        String ver
) {
}
