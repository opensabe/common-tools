package io.github.opensabe.common.internalcall;

import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.Locale;

/**
 * Determines whether an outbound host should receive an internal-call signature.
 */
public final class InternalHostMatcher {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final Collection<String> patterns;

    public InternalHostMatcher(Collection<String> patterns) {
        this.patterns = patterns;
    }

    /**
     * @param host        request host (may be null)
     * @param serviceName optional named-client serviceName for equality match; may be null
     */
    public boolean isInternalHost(String host, String serviceName) {
        if (host == null || host.isBlank()) {
            return false;
        }
        String lower = host.toLowerCase(Locale.ROOT);
        if (lower.indexOf(':') >= 0) {
            // IPv6 literal
            return false;
        }
        if (serviceName != null && !serviceName.isBlank()
                && lower.equals(serviceName.toLowerCase(Locale.ROOT))) {
            return true;
        }
        if (patterns != null) {
            for (String pattern : patterns) {
                if (pattern != null && antPathMatcher.match(pattern.toLowerCase(Locale.ROOT), lower)) {
                    return true;
                }
            }
        }
        // Single-label short service name (default http://{serviceName})
        if (lower.indexOf('.') < 0) {
            return true;
        }
        return isPrivateOrLoopbackIpv4(lower);
    }

    static boolean isPrivateOrLoopbackIpv4(String host) {
        Ipv4CidrSet.PeerKind kind = Ipv4CidrSet.classifyPeer(host);
        if (kind != Ipv4CidrSet.PeerKind.IPV4) {
            return false;
        }
        try {
            long addr = Ipv4CidrSet.parseIpv4ToLong(host);
            // 127.0.0.0/8
            if ((addr & 0xFF000000L) == 0x7F000000L) {
                return true;
            }
            // 10.0.0.0/8
            if ((addr & 0xFF000000L) == 0x0A000000L) {
                return true;
            }
            // 172.16.0.0/12
            if ((addr & 0xFFF00000L) == 0xAC100000L) {
                return true;
            }
            // 192.168.0.0/16
            if ((addr & 0xFFFF0000L) == 0xC0A80000L) {
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
