/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.opensabe.common.internalcall;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Resolves caller service name and stable non-loopback IPv4 for outbound signing.
 */
@Log4j2
public class CallerIdentityProvider {

    private final Environment environment;
    private volatile String cachedIp;

    public CallerIdentityProvider(Environment environment) {
        this.environment = environment;
    }

    public String getCallerService() {
        String name = environment.getProperty("spring.application.name");
        if (name == null || name.isBlank()) {
            return "unknown";
        }
        return name;
    }

    public String getCallerIp() {
        String cached = cachedIp;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (cachedIp != null) {
                return cachedIp;
            }
            String resolved = resolveIp();
            cachedIp = resolved;
            return resolved;
        }
    }

    private String resolveIp() {
        try {
            String fromNic = fromNetworkInterfaces();
            if (fromNic != null) {
                return fromNic;
            }
        } catch (Exception e) {
            log.debug("CallerIdentityProvider-resolveIp: network enumeration failed: {}", e.toString());
        }
        String podIp = environment.getProperty("POD_IP");
        if (podIp != null && !podIp.isBlank() && Ipv4CidrSet.classifyPeer(podIp) == Ipv4CidrSet.PeerKind.IPV4) {
            return podIp;
        }
        String springIp = environment.getProperty("spring.cloud.client.ip-address");
        if (springIp != null && !springIp.isBlank() && Ipv4CidrSet.classifyPeer(springIp) == Ipv4CidrSet.PeerKind.IPV4) {
            return springIp;
        }
        return "127.0.0.1";
    }

    private String fromNetworkInterfaces() throws Exception {
        String siteLocal = null;
        String anyNonLoopback = null;
        Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
        while (nics.hasMoreElements()) {
            NetworkInterface nic = nics.nextElement();
            if (!nic.isUp() || nic.isLoopback()) {
                continue;
            }
            Enumeration<InetAddress> addresses = nic.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (!(addr instanceof Inet4Address) || addr.isLoopbackAddress()) {
                    continue;
                }
                String host = addr.getHostAddress();
                if (host != null && host.indexOf(':') >= 0) {
                    continue;
                }
                log.debug("CallerIdentityProvider-fromNetworkInterfaces: nic={} ip={}", nic.getName(), host);
                if (addr.isSiteLocalAddress()) {
                    if (siteLocal == null) {
                        siteLocal = host;
                    }
                } else if (anyNonLoopback == null) {
                    anyNonLoopback = host;
                }
            }
        }
        if (siteLocal != null) {
            return siteLocal;
        }
        return anyNonLoopback;
    }
}
