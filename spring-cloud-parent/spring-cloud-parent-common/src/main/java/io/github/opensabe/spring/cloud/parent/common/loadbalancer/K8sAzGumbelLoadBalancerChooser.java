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
package io.github.opensabe.spring.cloud.parent.common.loadbalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.cloud.netflix.eureka.EurekaServiceInstance;
import org.springframework.core.env.Environment;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;

import io.github.opensabe.spring.cloud.parent.common.eureka.EurekaInstanceConfigBeanAddNodeInfoCustomizer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * 在 {@link LoadBalancerK8sAzBalanceProperties#enabled} 为 true 时参与选路：
 * 先剔除 OPEN，再按 metadata {@link EurekaInstanceConfigBeanAddNodeInfoCustomizer#K8S_AZ_INFO} 调用 {@link AzBalanceUtils}，
 * Gumbel-max 得到目标 AZ，再在桶内走亲和键或原有排序键。
 */
@Log4j2
public class K8sAzGumbelLoadBalancerChooser {

    private final LoadBalancerK8sAzBalanceProperties properties;
    private final Environment environment;
    private final ObjectProvider<DiscoveryClient> discoveryClientProvider;
    private final ObjectProvider<EurekaInstanceConfigBean> eurekaInstanceConfigBeanProvider;
    private final Supplier<Double> randomUnit;

    public K8sAzGumbelLoadBalancerChooser(
            LoadBalancerK8sAzBalanceProperties properties,
            Environment environment,
            ObjectProvider<DiscoveryClient> discoveryClientProvider,
            ObjectProvider<EurekaInstanceConfigBean> eurekaInstanceConfigBeanProvider) {
        this(properties, environment, discoveryClientProvider, eurekaInstanceConfigBeanProvider,
                () -> ThreadLocalRandom.current().nextDouble());
    }

    K8sAzGumbelLoadBalancerChooser(
            LoadBalancerK8sAzBalanceProperties properties,
            Environment environment,
            ObjectProvider<DiscoveryClient> discoveryClientProvider,
            ObjectProvider<EurekaInstanceConfigBean> eurekaInstanceConfigBeanProvider,
            Supplier<Double> randomUnit) {
        this.properties = properties;
        this.environment = environment;
        this.discoveryClientProvider = discoveryClientProvider;
        this.eurekaInstanceConfigBeanProvider = eurekaInstanceConfigBeanProvider;
        this.randomUnit = randomUnit;
    }

    /**
     * @param useClientAffinity 首次为 true 时可解析亲和属性；重试时应为 false，与 {@link TracedCircuitBreakerRoundRobinLoadBalancer} 原语义一致
     */
    public Optional<Response<ServiceInstance>> choose(
            String serviceId,
            List<ServiceInstance> serviceInstances,
            LoadBalancerRequestTraceContext requestLoadBalancerContext,
            Map<ServiceInstance, CircuitBreaker> serviceInstanceCircuitBreakerMap,
            RequestData clientRequest,
            boolean useClientAffinity,
            LoadingCache<String, AtomicLong> loadBalancedCount) {
        if (!properties.isEnabled()) {
            return Optional.empty();
        }
        if (CollectionUtils.isEmpty(serviceInstances)) {
            log.info("K8sAzGumbelLoadBalancerChooser serviceId={} skip: empty instances", serviceId);
            return Optional.empty();
        }

        List<ServiceInstance> eligible = serviceInstances.stream()
                .distinct()
                .filter(si -> serviceInstanceCircuitBreakerMap.get(si) != null
                        && serviceInstanceCircuitBreakerMap.get(si).getState() != CircuitBreaker.State.OPEN)
                .collect(Collectors.toList());
        int openFiltered = serviceInstances.size() - eligible.size();
        log.info("K8sAzGumbelLoadBalancerChooser serviceId={} after OPEN filter: eligible={}, filteredOpen={}",
                serviceId, eligible.size(), openFiltered);
        if (eligible.isEmpty()) {
            log.info("K8sAzGumbelLoadBalancerChooser serviceId={} all instances OPEN, defer to legacy path", serviceId);
            return Optional.empty();
        }

        Collections.shuffle(eligible);
        if (eligible.stream().allMatch(si -> requestLoadBalancerContext.getCalledInstances()
                .contains(instanceKey(si)))) {
            log.info("K8sAzGumbelLoadBalancerChooser serviceId={} all eligible instances already called in trace, clearing called sets",
                    serviceId);
            requestLoadBalancerContext.getCalledInstances().clear();
            requestLoadBalancerContext.getCalledNodes().clear();
        }

        String localSourceAz = resolveLocalK8sAzInfo();
        Map<String, List<Object>> sourceAzMap = buildSourceAzMap(localSourceAz);
        Map<String, List<Object>> targetAzMap = buildTargetAzMap(eligible);

        @SuppressWarnings({ "rawtypes", "unchecked" })
        Map<String, Map<String, Integer>> ratioMatrix = AzBalanceUtils.getLoadBalancingRatio(toRawMap(sourceAzMap), toRawMap(targetAzMap));
        Map<String, Integer> row = ratioMatrix.get(localSourceAz);
        if (row == null || row.isEmpty()) {
            log.info("K8sAzGumbelLoadBalancerChooser serviceId={} no matrix row for sourceAz={}, keys={}, defer to legacy",
                    serviceId, localSourceAz, ratioMatrix.keySet());
            return Optional.empty();
        }

        Map<String, Integer> weights = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : row.entrySet()) {
            String targetAz = e.getKey();
            int w = e.getValue() == null ? 0 : Math.max(0, e.getValue());
            if (w > 0 && eligible.stream().anyMatch(si -> targetAz.equals(k8sAzOf(si)))) {
                weights.put(targetAz, w);
            }
        }
        int weightSum = weights.values().stream().mapToInt(Integer::intValue).sum();
        if (weightSum <= 0) {
            log.info("K8sAzGumbelLoadBalancerChooser serviceId={} zero usable weights after intersecting eligible instances, row={}, defer to legacy",
                    serviceId, row);
            return Optional.empty();
        }

        log.info("K8sAzGumbelLoadBalancerChooser serviceId={} sourceAz={} AzBalanceUtils row={} usableWeights={} sum={}",
                serviceId, localSourceAz, row, weights, weightSum);

        String tStar = pickTargetAzByGumbelMax(weights, serviceId);
        List<ServiceInstance> inAz = eligible.stream()
                .filter(si -> tStar.equals(k8sAzOf(si)))
                .collect(Collectors.toList());
        if (inAz.isEmpty()) {
            log.info("K8sAzGumbelLoadBalancerChooser serviceId={} T*={} has no eligible instances, defer to legacy", serviceId, tStar);
            return Optional.empty();
        }

        // 亲和键按「列表下标取模」计算；eligible 每次 shuffle，故在存在亲和键时必须先对 inAz 稳定排序，否则同一 key 会落到不同实例
        List<ServiceInstance> inAzForAffinity = inAz;
        if (useClientAffinity && clientRequest != null && hasClientAffinityKeys(clientRequest)) {
            inAzForAffinity = sortInstancesStableForAffinity(inAz);
            log.info("K8sAzGumbelLoadBalancerChooser serviceId={} stable-sorted {} instances in az={} for affinity", serviceId, inAzForAffinity.size(), tStar);
        }

        ServiceInstance picked;
        if (useClientAffinity && clientRequest != null) {
            picked = pickWithAffinityInAz(inAzForAffinity, clientRequest, serviceId);
            if (picked != null && isNewlyStartup(picked)) {
                log.info("K8sAzGumbelLoadBalancerChooser serviceId={} affinity picked newly startup instance {}, use legacy sort in AZ {}",
                        serviceId, picked.getInstanceId(), tStar);
                picked = null;
            }
        } else {
            picked = null;
        }
        if (picked == null) {
            picked = sortAndPickFirst(inAz, requestLoadBalancerContext, serviceInstanceCircuitBreakerMap, loadBalancedCount, serviceId);
        }
        log.info("K8sAzGumbelLoadBalancerChooser serviceId={} selected instance {}:{} az={}", serviceId,
                picked.getHost(), picked.getPort(), tStar);
        return Optional.of(new DefaultResponse(picked));
    }

    private String pickTargetAzByGumbelMax(Map<String, Integer> weights, String serviceId) {
        if (weights.size() == 1) {
            String only = weights.keySet().iterator().next();
            log.info("K8sAzGumbelLoadBalancerChooser serviceId={} single target AZ {}, skip Gumbel", serviceId, only);
            return only;
        }
        String bestAz = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Map.Entry<String, Integer> e : weights.entrySet()) {
            int w = e.getValue();
            double gumbel = -Math.log(-Math.log(clampUnit(randomUnit.get())));
            double score = Math.log(w) + gumbel;
            log.info("K8sAzGumbelLoadBalancerChooser serviceId={} Gumbel candidate az={} weight={} score={}", serviceId, e.getKey(), w, score);
            if (score > bestScore) {
                bestScore = score;
                bestAz = e.getKey();
            }
        }
        log.info("K8sAzGumbelLoadBalancerChooser serviceId={} Gumbel-max winner az={} score={}", serviceId, bestAz, bestScore);
        return bestAz;
    }

    private static double clampUnit(double u) {
        if (u <= 0.0) {
            return 1e-12;
        }
        if (u >= 1.0) {
            return 1.0 - 1e-12;
        }
        return u;
    }

    private static boolean hasClientAffinityKeys(RequestData clientRequest) {
        Map<String, Object> attributes = clientRequest.getAttributes();
        if (attributes == null) {
            return false;
        }
        return attributes.get(TracedCircuitBreakerRoundRobinLoadBalancer.LOAD_BALANCE_KEY) != null
                || attributes.get(TracedCircuitBreakerRoundRobinLoadBalancer.ROUND_ROBIN_KEY) != null;
    }

    /**
     * host、port、instanceId 稳定排序，保证同一亲和键与实例集合下模运算结果不变。
     */
    private static List<ServiceInstance> sortInstancesStableForAffinity(List<ServiceInstance> inAz) {
        return inAz.stream()
                .sorted(Comparator.comparing(ServiceInstance::getHost)
                        .thenComparingInt(ServiceInstance::getPort)
                        .thenComparing(si -> si.getInstanceId() == null ? "" : si.getInstanceId()))
                .collect(Collectors.toList());
    }

    private ServiceInstance pickWithAffinityInAz(List<ServiceInstance> inAz, RequestData clientRequest, String serviceId) {
        Map<String, Object> attributes = clientRequest.getAttributes();
        Object loadKey = attributes.get(TracedCircuitBreakerRoundRobinLoadBalancer.LOAD_BALANCE_KEY);
        if (loadKey != null) {
            int idx = Math.abs(loadKey.hashCode() % inAz.size());
            ServiceInstance si = inAz.get(idx);
            log.info("K8sAzGumbelLoadBalancerChooser serviceId={} LOAD_BALANCE_KEY affinity in az -> {}:{}", serviceId, si.getHost(), si.getPort());
            return si;
        }
        Object rrKey = attributes.get(TracedCircuitBreakerRoundRobinLoadBalancer.ROUND_ROBIN_KEY);
        if (rrKey != null) {
            try {
                int mod = Math.abs(Integer.parseInt(rrKey.toString()) % inAz.size());
                ServiceInstance si = inAz.get(mod);
                log.info("K8sAzGumbelLoadBalancerChooser serviceId={} ROUND_ROBIN_KEY affinity in az -> {}:{}", serviceId, si.getHost(), si.getPort());
                return si;
            } catch (NumberFormatException ex) {
                log.info("K8sAzGumbelLoadBalancerChooser serviceId={} invalid ROUND_ROBIN_KEY {}", serviceId, rrKey);
            }
        }
        return null;
    }

    private ServiceInstance sortAndPickFirst(
            List<ServiceInstance> inAz,
            LoadBalancerRequestTraceContext ctx,
            Map<ServiceInstance, CircuitBreaker> cbMap,
            LoadingCache<String, AtomicLong> loadBalancedCount,
            String serviceId) {
        Map<ServiceInstance, ServiceInstanceStat> statMap = inAz.stream().collect(Collectors.toMap(si -> si,
                si -> ServiceInstanceStat.builder()
                        .host(si.getHost())
                        .port(si.getPort())
                        .called(ctx.getCalledInstances().contains(instanceKey(si)) ? 1 : 0)
                        .calledNode(ctx.getCalledNodes().contains(si.getMetadata().get(EurekaInstanceConfigBeanAddNodeInfoCustomizer.K8S_NODE_INFO)) ? 1 : 0)
                        .state(cbMap.get(si).getState())
                        .failureRate(cbMap.get(si).getMetrics().getFailureRate())
                        .recentLoadBalancedCount(loadBalancedCount.get(si.getInstanceId()).get())
                        .numberOfBufferedCalls(cbMap.get(si).getMetrics().getNumberOfBufferedCalls())
                        .build()));
        List<ServiceInstance> sorted = inAz.stream()
                .sorted(Comparator
                        .<ServiceInstance>comparingInt(si -> {
                            CircuitBreaker.State state = statMap.get(si).getState();
                            if (state == CircuitBreaker.State.HALF_OPEN) {
                                state = CircuitBreaker.State.CLOSED;
                            }
                            return state.getOrder();
                        })
                        .thenComparingInt(si -> statMap.get(si).getCalled())
                        .thenComparingInt(si -> statMap.get(si).getCalledNode())
                        .thenComparingDouble(si -> {
                            float fr = statMap.get(si).getFailureRate();
                            return fr < 0.0 ? 0.0 : fr;
                        })
                        .thenComparingLong(si -> statMap.get(si).getRecentLoadBalancedCount())
                        .thenComparingLong(si -> statMap.get(si).getNumberOfBufferedCalls()))
                .collect(Collectors.toList());
        if (ctx.isDetailLog()) {
            for (ServiceInstance si : sorted) {
                log.info("K8sAzGumbelLoadBalancerChooser serviceId={} sorted {}:{} -> {}", serviceId, si.getHost(), si.getPort(), statMap.get(si));
            }
        } else {
            for (ServiceInstance si : sorted) {
                log.debug("K8sAzGumbelLoadBalancerChooser serviceId={} sorted {}:{} -> {}", serviceId, si.getHost(), si.getPort(), statMap.get(si));
            }
        }
        return sorted.get(0);
    }

    private String resolveLocalK8sAzInfo() {
        EurekaInstanceConfigBean bean = eurekaInstanceConfigBeanProvider.getIfAvailable();
        if (bean != null && bean.getMetadataMap() != null) {
            String az = bean.getMetadataMap().get(EurekaInstanceConfigBeanAddNodeInfoCustomizer.K8S_AZ_INFO);
            if (StringUtils.isNotBlank(az)) {
                return az;
            }
        }
        String envZone = System.getenv("ZONE");
        if (StringUtils.isNotBlank(envZone)) {
            return envZone;
        }
        return EurekaInstanceConfigBeanAddNodeInfoCustomizer.DEFAULT_AZ_INFO;
    }

    private Map<String, List<Object>> buildSourceAzMap(String localSourceAz) {
        String appName = environment.getProperty("spring.application.name");
        DiscoveryClient discoveryClient = discoveryClientProvider.getIfAvailable();
        List<ServiceInstance> self = (discoveryClient == null || StringUtils.isBlank(appName))
                ? List.of()
                : discoveryClient.getInstances(appName);
        if (self.isEmpty()) {
            log.info("K8sAzGumbelLoadBalancerChooser buildSourceAzMap: no discovery instances for appName={}, use single placeholder for az={}",
                    appName, localSourceAz);
            return Map.of(localSourceAz, Collections.singletonList(new Object()));
        }
        Map<String, List<Object>> map = new LinkedHashMap<>();
        for (ServiceInstance si : self) {
            map.computeIfAbsent(k8sAzOf(si), k -> new ArrayList<>()).add(new Object());
        }
        log.info("K8sAzGumbelLoadBalancerChooser buildSourceAzMap: appName={} azCounts={}", appName,
                map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size())));
        return map;
    }

    private static Map<String, List<Object>> buildTargetAzMap(List<ServiceInstance> eligible) {
        Map<String, List<Object>> map = new LinkedHashMap<>();
        for (ServiceInstance si : eligible) {
            map.computeIfAbsent(k8sAzOf(si), k -> new ArrayList<>()).add(new Object());
        }
        return map;
    }

    private static String k8sAzOf(ServiceInstance si) {
        String az = si.getMetadata().get(EurekaInstanceConfigBeanAddNodeInfoCustomizer.K8S_AZ_INFO);
        return StringUtils.isBlank(az) ? EurekaInstanceConfigBeanAddNodeInfoCustomizer.DEFAULT_AZ_INFO : az;
    }

    private static String instanceKey(ServiceInstance si) {
        return si.getHost() + ":" + si.getPort();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Map<String, List> toRawMap(Map<String, List<Object>> typed) {
        Map raw = new LinkedHashMap();
        for (Map.Entry<String, List<Object>> e : typed.entrySet()) {
            raw.put(e.getKey(), e.getValue());
        }
        return raw;
    }

    private static boolean isNewlyStartup(ServiceInstance serviceInstance) {
        if (serviceInstance instanceof EurekaServiceInstance) {
            EurekaServiceInstance eurekaServiceInstance = (EurekaServiceInstance) serviceInstance;
            InstanceInfo instanceInfo = eurekaServiceInstance.getInstanceInfo();
            if (instanceInfo != null) {
                LeaseInfo leaseInfo = instanceInfo.getLeaseInfo();
                if (leaseInfo != null) {
                    long serviceUpTimestamp = leaseInfo.getServiceUpTimestamp();
                    if (serviceUpTimestamp > 0 && System.currentTimeMillis() - serviceUpTimestamp < 5 * 60 * 1000) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ServiceInstanceStat {
        private String host;
        private int port;
        private int called;
        private int calledNode;
        private CircuitBreaker.State state;
        private float failureRate;
        private long recentLoadBalancedCount;
        private int numberOfBufferedCalls;
    }
}
