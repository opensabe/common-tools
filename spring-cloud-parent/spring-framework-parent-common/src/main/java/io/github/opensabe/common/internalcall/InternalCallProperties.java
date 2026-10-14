package io.github.opensabe.common.internalcall;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for Internal HTTP call authentication (observe &amp; alert).
 * Default {@code enabled=false} keeps upgrades no-op.
 */
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "opensabe.internal-call")
public class InternalCallProperties {

    /**
     * Master switch. Default false for compatibility.
     */
    private boolean enabled = false;

    /**
     * Shared secret used when {@link Secrets#current} is blank.
     */
    private String sharedSecret = "";

    private Secrets secrets = new Secrets();

    /**
     * Allowed clock skew in milliseconds (default 5 minutes).
     */
    private long skewMs = 300_000L;

    /**
     * Failure handling: WARN (log) or ALARM (AlarmUtil.fatal). Never changes HTTP response by default.
     */
    private OnFailure onFailure = OnFailure.WARN;

    private List<String> excludePathPatterns = defaultExcludes();

    private Headers headers = new Headers();

    private Outbound outbound = new Outbound();

    private Inbound inbound = new Inbound();

    private Edge edge = new Edge();

    private Failure failure = new Failure();

    public enum OnFailure {
        WARN,
        ALARM
    }

    @Data
    @NoArgsConstructor
    public static class Secrets {
        private String current = "";
        private String previous = "";
    }

    @Data
    @NoArgsConstructor
    public static class Headers {
        private String callerService = InternalCallHeaders.DEFAULT_CALLER_SERVICE;
        private String callerIp = InternalCallHeaders.DEFAULT_CALLER_IP;
        private String ts = InternalCallHeaders.DEFAULT_TS;
        private String nonce = InternalCallHeaders.DEFAULT_NONCE;
        private String sign = InternalCallHeaders.DEFAULT_SIGN;
        private String ver = InternalCallHeaders.DEFAULT_VER;
        private String edgeToken = InternalCallHeaders.DEFAULT_EDGE_TOKEN;
    }

    @Data
    @NoArgsConstructor
    public static class Outbound {
        private boolean enabled = true;
        /**
         * When true, Feign signs internal hosts (still gated by host matcher).
         */
        private boolean signAllFeign = true;
        /**
         * Ant patterns matched against lower-cased host (default cluster DNS).
         */
        private List<String> internalHostPatterns = new ArrayList<>(List.of("*.svc.cluster.local"));
    }

    @Data
    @NoArgsConstructor
    public static class Inbound {
        private boolean enabled = true;
        /**
         * When true, callerIp header must equal TCP peer. Default false (sidecar / proxy safe).
         */
        private boolean bindPeerIp = false;
    }

    @Data
    @NoArgsConstructor
    public static class Edge {
        /**
         * Edge trust checks (Gateway only).
         */
        private boolean enabled = true;
        /**
         * Trusted IPv4 CIDRs for TCP peer. Empty list means all peers are UNTRUSTED_EDGE.
         * Illegal entries fail application startup.
         */
        private List<String> trustedCidrs = new ArrayList<>(List.of(
                "10.0.0.0/8",
                "172.16.0.0/12",
                "192.168.0.0/16"
        ));
        /**
         * Optional edge token. Blank skips token validation.
         */
        private String edgeToken = "";
        private OnFailure onFailure = OnFailure.WARN;
    }

    @Data
    @NoArgsConstructor
    public static class Failure {
        /**
         * Dedup window for WARN/ALARM fingerprint.
         */
        private int alarmDedupSeconds = 60;
    }

    private static List<String> defaultExcludes() {
        List<String> list = new ArrayList<>();
        list.add("/actuator/**");
        list.add("/health");
        list.add("/favicon.ico");
        return list;
    }

    /**
     * Effective current secret: secrets.current, else sharedSecret. Blank means not configured.
     */
    public String resolveCurrentSecret() {
        if (secrets != null && secrets.getCurrent() != null && !secrets.getCurrent().isBlank()) {
            return secrets.getCurrent();
        }
        if (sharedSecret != null && !sharedSecret.isBlank()) {
            return sharedSecret;
        }
        return "";
    }

    /**
     * Effective previous secret for rotation verify. Blank when unset.
     */
    public String resolvePreviousSecret() {
        if (secrets != null && secrets.getPrevious() != null && !secrets.getPrevious().isBlank()) {
            return secrets.getPrevious();
        }
        return "";
    }

    public boolean hasSecretConfigured() {
        return !resolveCurrentSecret().isBlank() || !resolvePreviousSecret().isBlank();
    }
}
