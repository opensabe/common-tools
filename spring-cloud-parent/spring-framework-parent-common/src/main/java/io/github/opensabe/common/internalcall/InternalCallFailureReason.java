package io.github.opensabe.common.internalcall;

/**
 * Failure reason codes for Internal HTTP call authentication (observe &amp; alert).
 * English codes for logs and alarm aggregation.
 */
public enum InternalCallFailureReason {
    MISSING_HEADER,
    BAD_TIMESTAMP,
    BAD_SIGNATURE,
    EXPIRED,
    UNSUPPORTED_VERSION,
    UNTRUSTED_EDGE,
    BAD_EDGE_TOKEN,
    PEER_IP_MISMATCH,
    UNSUPPORTED_PEER_IP,
    NO_SECRET_CONFIGURED,
    INTERNAL_ERROR
}
