package io.github.opensabe.common.internalcall;

/**
 * Result of inbound / edge verification.
 */
public record InternalCallVerifyResult(
        boolean success,
        InternalCallFailureReason reason,
        String callerService,
        String callerIp
) {
    public static InternalCallVerifyResult ok(String callerService, String callerIp) {
        return new InternalCallVerifyResult(true, null, callerService, callerIp);
    }

    public static InternalCallVerifyResult fail(InternalCallFailureReason reason) {
        return new InternalCallVerifyResult(false, reason, null, null);
    }

    public static InternalCallVerifyResult fail(InternalCallFailureReason reason, String callerService, String callerIp) {
        return new InternalCallVerifyResult(false, reason, callerService, callerIp);
    }
}
