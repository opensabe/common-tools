package io.github.opensabe.common.internalcall;

/**
 * Default header names and protocol constants for Internal HTTP call authentication.
 */
public final class InternalCallHeaders {

    public static final String PROTOCOL_VERSION = "1";

    public static final String DEFAULT_CALLER_SERVICE = "X-Internal-Caller-Service";
    public static final String DEFAULT_CALLER_IP = "X-Internal-Caller-Ip";
    public static final String DEFAULT_TS = "X-Internal-Auth-Ts";
    public static final String DEFAULT_NONCE = "X-Internal-Auth-Nonce";
    public static final String DEFAULT_SIGN = "X-Internal-Auth-Sign";
    public static final String DEFAULT_VER = "X-Internal-Auth-Ver";
    public static final String DEFAULT_EDGE_TOKEN = "X-Edge-Token";

    public static final int MAX_SERVICE_LEN = 128;
    public static final int MAX_IP_LEN = 64;
    public static final int MAX_TS_LEN = 16;
    public static final int MAX_NONCE_LEN = 64;
    public static final int MAX_SIGN_LEN = 128;
    public static final int HMAC_RAW_BYTES = 32;
    public static final int NONCE_BYTES = 16;

    private InternalCallHeaders() {
    }
}
