package io.github.opensabe.common.internalcall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * IPv4-only CIDR matcher. Rejects leading zeros in octets and IPv6.
 * Empty set matches nothing.
 */
public final class Ipv4CidrSet {

    private final List<Cidr> cidrs;

    private Ipv4CidrSet(List<Cidr> cidrs) {
        this.cidrs = Collections.unmodifiableList(cidrs);
    }

    /**
     * Parse CIDR list. Illegal entries throw IllegalArgumentException (fail-fast at startup).
     */
    public static Ipv4CidrSet parse(Collection<String> entries) {
        List<Cidr> list = new ArrayList<>();
        if (entries == null || entries.isEmpty()) {
            return new Ipv4CidrSet(list);
        }
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) {
                throw new IllegalArgumentException("trusted-cidrs entry must not be blank");
            }
            list.add(parseOne(entry.trim()));
        }
        return new Ipv4CidrSet(list);
    }

    public boolean contains(String ipv4) {
        if (ipv4 == null || ipv4.isBlank() || ipv4.indexOf(':') >= 0) {
            return false;
        }
        long addr;
        try {
            addr = parseIpv4ToLong(ipv4);
        } catch (IllegalArgumentException e) {
            return false;
        }
        for (Cidr cidr : cidrs) {
            if ((addr & cidr.mask) == cidr.network) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return cidrs.isEmpty();
    }

    static Cidr parseOne(String cidr) {
        int slash = cidr.indexOf('/');
        if (slash <= 0 || slash == cidr.length() - 1) {
            throw new IllegalArgumentException("Invalid CIDR (missing prefix): " + cidr);
        }
        String ipPart = cidr.substring(0, slash);
        String prefixPart = cidr.substring(slash + 1);
        if (ipPart.indexOf(':') >= 0) {
            throw new IllegalArgumentException("IPv6 CIDR is not supported: " + cidr);
        }
        int prefix;
        try {
            prefix = Integer.parseInt(prefixPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid CIDR prefix: " + cidr);
        }
        if (prefix < 0 || prefix > 32) {
            throw new IllegalArgumentException("CIDR prefix out of range 0-32: " + cidr);
        }
        long network = parseIpv4ToLong(ipPart);
        long mask = prefix == 0 ? 0L : (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
        if ((network & ~mask) != 0) {
            // allow host bits set; normalize to network
            network = network & mask;
        }
        return new Cidr(network, mask);
    }

    /**
     * Parse dotted IPv4. Rejects leading zeros (e.g. 010.0.0.1).
     */
    public static long parseIpv4ToLong(String ipv4) {
        if (ipv4 == null || ipv4.isBlank() || ipv4.indexOf(':') >= 0) {
            throw new IllegalArgumentException("Not an IPv4 address: " + ipv4);
        }
        String[] parts = ipv4.split("\\.", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Not an IPv4 address: " + ipv4);
        }
        long value = 0;
        for (String part : parts) {
            if (part.isEmpty()) {
                throw new IllegalArgumentException("Not an IPv4 address: " + ipv4);
            }
            if (part.length() > 1 && part.charAt(0) == '0') {
                throw new IllegalArgumentException("Leading zeros are not allowed in IPv4: " + ipv4);
            }
            int octet;
            try {
                octet = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Not an IPv4 address: " + ipv4);
            }
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("IPv4 octet out of range: " + ipv4);
            }
            value = (value << 8) | octet;
        }
        return value & 0xFFFFFFFFL;
    }

    /**
     * Classify peer address string without InetAddress.getByName (avoids IPv4-mapped folding).
     */
    public static PeerKind classifyPeer(String peerHost) {
        if (peerHost == null || peerHost.isBlank()) {
            return PeerKind.MISSING;
        }
        if (peerHost.indexOf(':') >= 0) {
            return PeerKind.IPV6_OR_MAPPED;
        }
        try {
            parseIpv4ToLong(peerHost);
            return PeerKind.IPV4;
        } catch (IllegalArgumentException e) {
            return PeerKind.INVALID;
        }
    }

    public enum PeerKind {
        IPV4,
        IPV6_OR_MAPPED,
        MISSING,
        INVALID
    }

    static final class Cidr {
        final long network;
        final long mask;

        Cidr(long network, long mask) {
            this.network = network;
            this.mask = mask;
        }
    }
}
