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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class IpUtil {

    public static final String DEFAULT_IP = "0.0.0.0";

    private static final InetAddressValidator VALIDATOR = InetAddressValidator.getInstance();

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
    
    private static boolean isValidIp(String ip) {
        if (
                StringUtils.isBlank(ip) 
                || StringUtils.isEmpty(ip) 
                || StringUtils.equalsIgnoreCase("unknown", ip)
                || StringUtils.equalsIgnoreCase("null", ip)
        ) {
            return false;
        }
        return true;
    }

    public static String getIpFromHeader(HttpServletRequest request) {
        try {
            // 按优先级顺序收集所有可能的IP源
            List<String> ipSources = collectIpSources(request);
            
            // 从所有IP源中查找第一个非内网IP
            String result = findFirstNonInternalIp(ipSources);
            
            return result != null ? result : DEFAULT_IP;
        } catch (Throwable e) {
            log.error("getting user ip from header error: {}", e.getMessage(), e);
            return DEFAULT_IP;
        }
    }

    public static String getIpFromHeader(HttpHeaders headers) {
        try {
            // 按优先级顺序收集所有可能的IP源
            List<String> ipSources = collectIpSources(headers);
            
            // 从所有IP源中查找第一个非内网IP
            String result = findFirstNonInternalIp(ipSources);
            
            return result;
        } catch (Throwable e) {
            log.error("getting user ip from headers error: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 从HttpServletRequest收集所有可能的IP源
     */
    private static List<String> collectIpSources(HttpServletRequest request) {
        List<String> ipSources = new ArrayList<>();
        
        // 按优先级顺序添加IP源
        addIpSource(ipSources, request.getHeader("x-forwarded-for"));
        addIpSource(ipSources, request.getHeader("Proxy-Client-IP"));
        addIpSource(ipSources, request.getHeader("WL-Proxy-Client-IP"));
        addIpSource(ipSources, request.getHeader("HTTP_CLIENT_IP"));
        addIpSource(ipSources, request.getHeader("HTTP_X_FORWARDED_FOR"));
        addIpSource(ipSources, request.getHeader("X-Real-IP"));
        addIpSource(ipSources, request.getRemoteAddr());
        
        return ipSources;
    }

    /**
     * 从HttpHeaders收集所有可能的IP源
     */
    private static List<String> collectIpSources(HttpHeaders headers) {
        List<String> ipSources = new ArrayList<>();
        // 按优先级顺序添加IP源
        Optional.ofNullable(headers.get("x-forwarded-for")).ifPresent(h ->
                h.forEach(ip -> addIpSource(ipSources, ip))
        );
        Optional.ofNullable(headers.get("Proxy-Client-IP")).ifPresent(h ->
                h.forEach(ip -> addIpSource(ipSources, ip))
        );
        Optional.ofNullable(headers.get("WL-Proxy-Client-IP")).ifPresent(h ->
                h.forEach(ip -> addIpSource(ipSources, ip))
        );
        Optional.ofNullable(headers.get("HTTP_CLIENT_IP")).ifPresent(h ->
                h.forEach(ip -> addIpSource(ipSources, ip))
        );
        Optional.ofNullable(headers.get("HTTP_X_FORWARDED_FOR")).ifPresent(h ->
                h.forEach(ip -> addIpSource(ipSources, ip))
        );
        Optional.ofNullable(headers.get("X-Real-IP")).ifPresent(h ->
                h.forEach(ip -> addIpSource(ipSources, ip))
        );
        return ipSources;
    }

    /**
     * 添加IP源到列表中，如果IP有效的话
     */
    private static void addIpSource(List<String> ipSources, String ip) {
        // 处理可能包含多个IP的字符串
        String[] ips = parseIpString(ip);
        for (String singleIp : ips) {
            if (isValidIp(singleIp)) {
                ipSources.add(singleIp);
            }
        }
    }

    /**
     * 解析IP字符串，支持逗号分隔的多个IP
     */
    private static String[] parseIpString(String ips) {
        if (!isValidIp(ips)) {
            return new String[0];
        }
        
        return Stream.of(ips.split("\\,"))
                .map(String::trim)
                .filter(ip -> isValidIp(ip) && !DEFAULT_IP.equals(ip))
                .toArray(String[]::new);
    }

    /**
     * 从IP源列表中查找第一个非内网IP
     */
    private static String findFirstNonInternalIp(List<String> ipSources) {
        if (CollectionUtils.isEmpty(ipSources)) {
            return DEFAULT_IP;
        }
        
        // 按顺序查找第一个非内网IP
        for (String ip : ipSources) {
            if (!invalidOrInternalIp(ip)) {
                return ip;
            }
        }
        
        // 如果所有IP都是内网IP，返回 0.0.0.0
        return DEFAULT_IP;
    }

    private static boolean invalidOrInternalIp(String ip) {
        if (!VALIDATOR.isValidInet4Address(ip)) {
            return true;
        }
        if (ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("192.168.") || DEFAULT_IP.equals(ip)) {
            return true;
        }
        if (ip.startsWith("172.")) {
            String[] ipSegs = ip.split("\\.");
            int ipSeg2 = Integer.parseInt(ipSegs[1]);
            if (ipSeg2 >= 16 && ipSeg2 <= 31)
                return true;
        }
        return false;
    }
}
