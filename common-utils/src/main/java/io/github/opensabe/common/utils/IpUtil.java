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
package io.github.opensabe.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

@Log4j2
public class IpUtil {

    public static InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface ifc = nics.nextElement();
                if (!ifc.isUp())
                    continue;
                log.trace((Object) ("Testing interface: " + ifc.getDisplayName()));
                if (ifc.getIndex() < lowest || result == null) {
                    lowest = ifc.getIndex();
                } else if (result != null)
                    continue;
                Enumeration<InetAddress> addrs = ifc.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress address = addrs.nextElement();
                    if (!(address instanceof Inet4Address) || address.isLoopbackAddress())
                        continue;
                    log.trace((Object) ("Found non-loopback interface: " + ifc.getDisplayName()));
                    result = address;
                }
            }
        } catch (IOException ex) {
            log.error((Object) "Cannot get first non-loopback address", (Throwable) ex);
        }
        if (result != null) {
            return result;
        }
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.warn((Object) "Unable to retrieve localhost");
            return null;
        }
    }

    public static String getIpFromHeader(HttpServletRequest request) {
        try {
            String ipField = request.getHeader("x-forwarded-for");
            if (ipField == null || ipField.length() == 0 || "unknown".equalsIgnoreCase(ipField))
                ipField = request.getHeader("Proxy-Client-IP");
            if (ipField == null || ipField.length() == 0 || "unknown".equalsIgnoreCase(ipField))
                ipField = request.getHeader("WL-Proxy-Client-IP");
            if (ipField == null || ipField.length() == 0 || "unknown".equalsIgnoreCase(ipField))
                ipField = request.getHeader("HTTP_CLIENT_IP");
            if (ipField == null || ipField.length() == 0 || "unknown".equalsIgnoreCase(ipField))
                ipField = request.getHeader("HTTP_X_FORWARDED_FOR");
            if (ipField == null || ipField.length() == 0 || "unknown".equalsIgnoreCase(ipField))
                ipField = request.getHeader("X-Real-IP");
            if (ipField == null || ipField.length() == 0 || "unknown".equalsIgnoreCase(ipField))
                ipField = request.getRemoteAddr();

            // remove 0.0.0.0
            String[] split = getIpFromIpString(ipField);

            //可能放在开头，可能放在最后
            String first = split[0];
            if (!internalIp(first)) {
                return first;
            }
            String last = split[split.length - 1];
            if (!internalIp(last)) {
                return last;
            }
            return first;
        } catch (Throwable e) {
            log.error("getting user ip from header error: {}", e.getMessage(), e);
            return "0.0.0.0";
        }
    }

    public static String getIpFromHeader(HttpHeaders headers) {
        List<String> strings = headers.get("x-forwarded-for");
        if (CollectionUtils.isEmpty(strings)) {
            strings = headers.get("Proxy-Client-IP");
        }
        if (CollectionUtils.isEmpty(strings)) {
            strings = headers.get("WL-Proxy-Client-IP");
        }
        if (CollectionUtils.isEmpty(strings)) {
            strings = headers.get("HTTP_CLIENT_IP");
        }
        if (CollectionUtils.isEmpty(strings)) {
            strings = headers.get("HTTP_X_FORWARDED_FOR");
        }
        if (CollectionUtils.isEmpty(strings)) {
            strings = headers.get("X-Real-IP");
        }
        if (CollectionUtils.isNotEmpty(strings)) {
            String ipsHeader = strings.get(0);
            String[] ips = getIpFromIpString(ipsHeader);
            if (ips.length == 0) {
                return "";
            }

            String first = ips[0];
            if (!internalIp(first)) {
                return first;
            }
            String last = ips[ips.length - 1];
            if (!internalIp(last)) {
                return last;
            }
        }
        return "";
    }

    private static String[] getIpFromIpString(String ips) {
        // remove 0.0.0.0
        return Stream.of(ips.split("\\,"))
                .map(String::trim)
                .filter(ip -> !"0.0.0.0".equals(ip))
                .toArray(String[]::new);
    }

    private static boolean internalIp(String ip) {
        if (ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("192.168.") || ip.equals("0.0.0.0"))
            return true;
        if (ip.startsWith("172.")) {
            String[] ipSegs = ip.split("\\.");
            int ipSeg2 = Integer.parseInt(ipSegs[1]);
            if (ipSeg2 >= 16 && ipSeg2 <= 31)
                return true;
        }
        return false;
    }

//    private static String trueIp(String ip) {
//        if (ip.startsWith("52.80") || ip.startsWith("52.81"))
//            return "35.156.137.1";
//        return ip;
//    }
}
