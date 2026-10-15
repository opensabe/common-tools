package io.github.opensabe.common.internalcall;

/**
 * Request / exchange attribute keys written after a successful inbound verify.
 * Business code must not clear the verified marker.
 */
public final class InternalCallAuthAttributes {

    public static final String VERIFIED = "opensabe.internal-call.verified";
    /**
     * Set after the first inbound attempt (success or failure) so async/ERROR re-entry
     * does not re-HMAC or re-alarm.
     */
    public static final String PROCESSED = "opensabe.internal-call.processed";
    public static final String CALLER_SERVICE = "opensabe.internal-call.caller-service";
    public static final String CALLER_IP = "opensabe.internal-call.caller-ip";

    private InternalCallAuthAttributes() {
    }
}
