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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.github.opensabe.spring.cloud.parent.common.eureka.EurekaInstanceConfigBeanAddNodeInfoCustomizer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;

/**
 * {@link K8sAzGumbelLoadBalancerChooser} 单元测试。
 * <p>
 * 通过 Mock Discovery / Eureka / 随机源构造矩阵与候选实例；断言与失败消息使用英文，便于日志与 CI 检索。
 */
@DisplayName("K8sAzGumbelLoadBalancerChooser")
class K8sAzGumbelLoadBalancerChooserTest {

    /** 被调下游服务 id（仅测试数据） */
    private static final String SVC = "downstream";
    /** spring.application.name，用于 buildSourceAzMap */
    private static final String APP = "caller";
    /** 与 mock 本机 Eureka AZ 一致，便于 AzBalanceUtils 出有效行 */
    private static final String AZ_SRC = "zone-src";
    /** 对端 AZ，用于跨 AZ 比例与 Monte Carlo */
    private static final String AZ_PEER = "zone-peer";

    @Test
    @DisplayName("enabled=false 时立即返回 empty，由外层 LB 走老逻辑")
    void chooseWhenDisabledReturnsEmpty() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(false);
        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mock(Environment.class), null, null, () -> 0.5);
        assertTrue(chooser.choose(SVC, List.of(instance("1", "h1", 1, "az-a")), new LoadBalancerRequestTraceContext(),
                Map.of(), null, true, cache()).isEmpty());
    }

    @Test
    @DisplayName("候选全部 OPEN 时返回 empty，交由 Traced 老路径")
    void chooseWhenAllOpenReturnsEmpty() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        ServiceInstance i1 = instance("1", "h1", 1, AZ_SRC);
        CircuitBreaker open = circuitBreaker(CircuitBreaker.State.OPEN, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(i1, open);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelf(AZ_SRC, 2), mockEurekaAz(AZ_SRC), () -> 0.5);
        assertTrue(chooser.choose(SVC, List.of(i1), new LoadBalancerRequestTraceContext(), cbMap, null, false, cache()).isEmpty());
    }

    @Test
    @DisplayName("过滤 OPEN 后仅在非 OPEN 实例中选，同 AZ 内命中 CLOSED")
    void chooseFiltersOpenAndPicksClosedInSameAz() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        ServiceInstance openI = instance("open", "bad", 1, AZ_SRC);
        ServiceInstance closedI = instance("ok", "good", 2, AZ_SRC);
        CircuitBreaker openCb = circuitBreaker(CircuitBreaker.State.OPEN, 0f);
        CircuitBreaker closedCb = circuitBreaker(CircuitBreaker.State.CLOSED, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(openI, openCb, closedI, closedCb);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelf(AZ_SRC, 2), mockEurekaAz(AZ_SRC), () -> 0.5);
        Response<ServiceInstance> r = chooser.choose(SVC, List.of(openI, closedI), new LoadBalancerRequestTraceContext(),
                cbMap, null, false, cache()).orElseThrow();
        assertEquals("good", r.getServer().getHost());
        assertEquals(2, r.getServer().getPort());
    }

    @Test
    @DisplayName("LOAD_BALANCE_KEY：多次 choose 在 shuffle 下仍命中同一 host（稳定排序后取模）")
    void chooseWithLoadBalanceKeyStableAcrossCalls() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        ServiceInstance i0 = instance("a", "h0", 10, AZ_SRC);
        ServiceInstance i1 = instance("b", "h1", 11, AZ_SRC);
        CircuitBreaker cb = circuitBreaker(CircuitBreaker.State.CLOSED, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(i0, cb, i1, cb);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(TracedCircuitBreakerRoundRobinLoadBalancer.LOAD_BALANCE_KEY, "sticky-uid");
        RequestData rd = new RequestData(HttpMethod.GET, URI.create("http://x"), new HttpHeaders(), null, attrs);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelf(AZ_SRC, 1), mockEurekaAz(AZ_SRC), () -> 0.5);
        String firstHost = null;
        for (int i = 0; i < 200; i++) {
            Response<ServiceInstance> r = chooser.choose(SVC, List.of(i0, i1), new LoadBalancerRequestTraceContext(),
                    cbMap, rd, true, cache()).orElseThrow();
            if (firstHost == null) {
                firstHost = r.getServer().getHost();
            } else {
                assertEquals(firstHost, r.getServer().getHost(), "affinity host must be stable across shuffles");
            }
        }
        assertTrue(List.of("h0", "h1").contains(firstHost));
    }

    @Test
    @DisplayName("ROUND_ROBIN_KEY：与稳定排序列表下标一致，多次调用不变")
    void chooseWithRoundRobinKeyStableAcrossCalls() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        List<ServiceInstance> inst = List.of(
                instance("a", "h0", 10, AZ_SRC),
                instance("b", "h1", 11, AZ_SRC),
                instance("c", "h2", 12, AZ_SRC));
        CircuitBreaker cb = circuitBreaker(CircuitBreaker.State.CLOSED, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = new HashMap<>();
        for (ServiceInstance si : inst) {
            cbMap.put(si, cb);
        }

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(TracedCircuitBreakerRoundRobinLoadBalancer.ROUND_ROBIN_KEY, "7");
        RequestData rd = new RequestData(HttpMethod.GET, URI.create("http://x"), new HttpHeaders(), null, attrs);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelf(AZ_SRC, 1), mockEurekaAz(AZ_SRC), () -> 0.5);
        int mod = Math.abs(Integer.parseInt("7") % 3);
        String expectedHost = inst.stream()
                .sorted(Comparator.comparing(ServiceInstance::getHost))
                .collect(Collectors.toList())
                .get(mod)
                .getHost();
        for (int i = 0; i < 150; i++) {
            Response<ServiceInstance> r = chooser.choose(SVC, inst, new LoadBalancerRequestTraceContext(), cbMap, rd, true, cache()).orElseThrow();
            assertEquals(expectedHost, r.getServer().getHost());
        }
    }

    @Test
    @DisplayName("ROUND_ROBIN_KEY 非法：亲和失败，回退同 AZ 内按失败率排序")
    void invalidRoundRobinKeyFallsBackToSort() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        ServiceInstance i0 = instance("a", "h0", 10, AZ_SRC);
        ServiceInstance i1 = instance("b", "h1", 11, AZ_SRC);
        CircuitBreaker cb0 = circuitBreaker(CircuitBreaker.State.CLOSED, 0.5f);
        CircuitBreaker cb1 = circuitBreaker(CircuitBreaker.State.CLOSED, 0.01f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(i0, cb0, i1, cb1);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(TracedCircuitBreakerRoundRobinLoadBalancer.ROUND_ROBIN_KEY, "not-a-number");
        RequestData rd = new RequestData(HttpMethod.GET, URI.create("http://x"), new HttpHeaders(), null, attrs);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelf(AZ_SRC, 1), mockEurekaAz(AZ_SRC), () -> 0.5);
        Response<ServiceInstance> r = chooser.choose(SVC, List.of(i0, i1), new LoadBalancerRequestTraceContext(), cbMap, rd, true, cache()).orElseThrow();
        assertEquals("h1", r.getServer().getHost());
    }

    @Test
    @DisplayName("useClientAffinity=false：不解析亲和，按失败率选（对齐重试语义）")
    void retrySkipsAffinityAndUsesSort() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        ServiceInstance i0 = instance("a", "h0", 10, AZ_SRC);
        ServiceInstance i1 = instance("b", "h1", 11, AZ_SRC);
        CircuitBreaker cb0 = circuitBreaker(CircuitBreaker.State.CLOSED, 0.8f);
        CircuitBreaker cb1 = circuitBreaker(CircuitBreaker.State.CLOSED, 0.01f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(i0, cb0, i1, cb1);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(TracedCircuitBreakerRoundRobinLoadBalancer.LOAD_BALANCE_KEY, "uid");
        RequestData rd = new RequestData(HttpMethod.GET, URI.create("http://x"), new HttpHeaders(), null, attrs);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelf(AZ_SRC, 1), mockEurekaAz(AZ_SRC), () -> 0.5);
        Response<ServiceInstance> retryPick = chooser.choose(SVC, List.of(i0, i1), new LoadBalancerRequestTraceContext(), cbMap, rd, false, cache()).orElseThrow();
        assertEquals("h1", retryPick.getServer().getHost());
    }

    @Test
    @DisplayName("HALF_OPEN 与 CLOSED 排序同级后再比失败率，低失败率实例胜出")
    void halfOpenTreatedLikeClosedThenFailureRateWins() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        ServiceInstance iHo = instance("a", "h0", 10, AZ_SRC);
        ServiceInstance iCl = instance("b", "h1", 11, AZ_SRC);
        CircuitBreaker cbHo = circuitBreaker(CircuitBreaker.State.HALF_OPEN, 0.01f);
        CircuitBreaker cbCl = circuitBreaker(CircuitBreaker.State.CLOSED, 0.9f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(iHo, cbHo, iCl, cbCl);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelf(AZ_SRC, 1), mockEurekaAz(AZ_SRC), () -> 0.5);
        Response<ServiceInstance> r = chooser.choose(SVC, List.of(iHo, iCl), new LoadBalancerRequestTraceContext(), cbMap, null, false, cache()).orElseThrow();
        assertEquals("h0", r.getServer().getHost());
    }

    @Test
    @DisplayName("大规模同 AZ 实例 + LOAD_BALANCE_KEY：亲和目标 host 在多轮 choose 中不变")
    void affinityStableWithManyInstancesInOneAz() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        int n = 800;
        List<ServiceInstance> instances = new ArrayList<>(n);
        CircuitBreaker cb = circuitBreaker(CircuitBreaker.State.CLOSED, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            ServiceInstance si = instance("id-" + i, "host-" + i, 30000 + i, AZ_SRC);
            instances.add(si);
            cbMap.put(si, cb);
        }
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(TracedCircuitBreakerRoundRobinLoadBalancer.LOAD_BALANCE_KEY, "same-user-42");
        RequestData rd = new RequestData(HttpMethod.GET, URI.create("http://x"), new HttpHeaders(), null, attrs);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelf(AZ_SRC, 50), mockEurekaAz(AZ_SRC), () -> 0.5);
        String host = null;
        for (int t = 0; t < 120; t++) {
            Response<ServiceInstance> r = chooser.choose(SVC, instances, new LoadBalancerRequestTraceContext(), cbMap, rd, true, cache()).orElseThrow();
            assertNotNull(r.getServer());
            if (host == null) {
                host = r.getServer().getHost();
            } else {
                assertEquals(host, r.getServer().getHost());
            }
        }
    }

    @Test
    @DisplayName("Monte Carlo：跨 AZ 选中比例与 AzBalanceUtils 行权重理论值在 5σ 内一致")
    void monteCarloTargetAzShareMatchesAzBalanceWeights() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);

        int nLocal = 45;
        int nPeer = 55;
        List<ServiceInstance> eligible = new ArrayList<>(nLocal + nPeer);
        for (int i = 0; i < nLocal; i++) {
            eligible.add(instance("L" + i, "east-" + i, 40000 + i, AZ_SRC));
        }
        for (int i = 0; i < nPeer; i++) {
            eligible.add(instance("P" + i, "west-" + i, 50000 + i, AZ_PEER));
        }
        CircuitBreaker cb = circuitBreaker(CircuitBreaker.State.CLOSED, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = eligible.stream().collect(Collectors.toMap(si -> si, si -> cb));

        Map<String, List<Object>> rawSource = groupByAz(mockDiscoverySelfInstances(AZ_SRC, 60));
        Map<String, List<Object>> rawTarget = groupByAz(eligible);
        @SuppressWarnings("rawtypes")
        Map<String, Map<String, Integer>> matrix = AzBalanceUtils.getLoadBalancingRatio(toRaw(rawSource), toRaw(rawTarget));
        Map<String, Integer> row = matrix.get(AZ_SRC);
        assertNotNull(row);

        double wPeer = 0;
        double wLocal = 0;
        for (Map.Entry<String, Integer> e : row.entrySet()) {
            String taz = e.getKey();
            int w = e.getValue() == null ? 0 : Math.max(0, e.getValue());
            if (w <= 0) {
                continue;
            }
            boolean has = eligible.stream().anyMatch(si -> taz.equals(azOf(si)));
            if (!has) {
                continue;
            }
            if (AZ_PEER.equals(taz)) {
                wPeer += w;
            }
            if (AZ_SRC.equals(taz)) {
                wLocal += w;
            }
        }
        double sumW = wPeer + wLocal;
        assertTrue(sumW > 0, "expected positive weights for Monte Carlo");
        double pPeer = wPeer / sumW;

        Environment env = mockEnvApp();
        DiscoveryClient dc = mock(DiscoveryClient.class);
        when(dc.getInstances(APP)).thenReturn(mockDiscoverySelfInstances(AZ_SRC, 60));
        EurekaInstanceConfigBean eureka = mockEurekaAz(AZ_SRC);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, env, dc, eureka, () -> ThreadLocalRandom.current().nextDouble());

        int trials = 12_000;
        long countPeer = 0;
        for (int i = 0; i < trials; i++) {
            Response<ServiceInstance> r = chooser.choose(SVC, eligible, new LoadBalancerRequestTraceContext(), cbMap, null, false, cache()).orElseThrow();
            if (AZ_PEER.equals(azOf(r.getServer()))) {
                countPeer++;
            }
        }
        double pHat = countPeer / (double) trials;
        double sigma = Math.sqrt(trials * pPeer * (1 - pPeer));
        assertTrue(Math.abs(pHat - pPeer) < 5.0 * Math.max(sigma, 1e-6),
                () -> String.format("Monte Carlo peer AZ share %.4f vs expected %.4f (sigma~%.4f)", pHat, pPeer, sigma));
    }

    @Test
    @DisplayName("无亲和且指标相同：大量随机 shuffle 下流量分散到多个 host、无单点过热")
    void manyEqualInstancesSpreadsAcrossHostsWhenNoAffinity() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        int n = 400;
        List<ServiceInstance> eligible = new ArrayList<>(n);
        CircuitBreaker cb = circuitBreaker(CircuitBreaker.State.CLOSED, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            ServiceInstance si = instance("id-" + i, "hn-" + i, 60000 + i, AZ_SRC);
            eligible.add(si);
            cbMap.put(si, cb);
        }
        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelf(AZ_SRC, 30), mockEurekaAz(AZ_SRC),
                () -> ThreadLocalRandom.current().nextDouble());

        Map<String, Long> hostCounts = new HashMap<>();
        int trials = 8000;
        for (int i = 0; i < trials; i++) {
            Response<ServiceInstance> r = chooser.choose(SVC, eligible, new LoadBalancerRequestTraceContext(), cbMap, null, false, cache()).orElseThrow();
            hostCounts.merge(r.getServer().getHost(), 1L, Long::sum);
        }
        long distinctHosts = hostCounts.size();
        assertTrue(distinctHosts >= 30,
                "expected spread across many hosts when stats tie, got " + distinctHosts + " distinct hosts over " + trials + " trials");
        long maxOnOneHost = hostCounts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        assertTrue(maxOnOneHost < trials * 0.06,
                "no single host should absorb most traffic: max count " + maxOnOneHost);
    }

    @Test
    @DisplayName("无亲和：同 AZ 三实例按失败率排序，选中最低失败率")
    void chooseWithoutAffinityPrefersLowerFailureRateInSameAz() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        ServiceInstance iBad = instance("a", "bad", 1, AZ_SRC);
        ServiceInstance iGood = instance("b", "good", 2, AZ_SRC);
        ServiceInstance iMid = instance("c", "mid", 3, AZ_SRC);
        CircuitBreaker cbBad = circuitBreaker(CircuitBreaker.State.CLOSED, 0.9f);
        CircuitBreaker cbGood = circuitBreaker(CircuitBreaker.State.CLOSED, 0.05f);
        CircuitBreaker cbMid = circuitBreaker(CircuitBreaker.State.CLOSED, 0.4f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(iBad, cbBad, iGood, cbGood, iMid, cbMid);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelf(AZ_SRC, 1), mockEurekaAz(AZ_SRC), () -> 0.5);
        Response<ServiceInstance> r = chooser.choose(SVC, List.of(iBad, iGood, iMid), new LoadBalancerRequestTraceContext(),
                cbMap, null, false, cache()).orElseThrow();
        assertEquals("good", r.getServer().getHost());
        assertEquals(2, r.getServer().getPort());
    }

    /** 读取实例 metadata 中的 K8S AZ，与生产常量一致 */
    private static String azOf(ServiceInstance si) {
        String az = si.getMetadata().get(EurekaInstanceConfigBeanAddNodeInfoCustomizer.K8S_AZ_INFO);
        return az == null || az.isBlank() ? EurekaInstanceConfigBeanAddNodeInfoCustomizer.DEFAULT_AZ_INFO : az;
    }

    /** 构造本服务在 Discovery 中的实例列表，仅用于 source AZ 矩阵计数 */
    private static List<ServiceInstance> mockDiscoverySelfInstances(String az, int count) {
        List<ServiceInstance> self = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            self.add(instance("self-" + i, "local-" + i, 2000 + i, az));
        }
        return self;
    }

    /** Mock DiscoveryClient#getInstances(APP) 返回指定 AZ、数量的本集群实例 */
    private static DiscoveryClient mockDiscoverySelf(String az, int count) {
        DiscoveryClient dc = mock(DiscoveryClient.class);
        when(dc.getInstances(APP)).thenReturn(mockDiscoverySelfInstances(az, count));
        return dc;
    }

    /** spring.application.name = {@link #APP}，供 chooser 拉取本服务注册信息 */
    private static Environment mockEnvApp() {
        Environment env = mock(Environment.class);
        when(env.getProperty("spring.application.name")).thenReturn(APP);
        return env;
    }

    /** Mock 本机 Eureka metadata 中的 K8S AZ */
    private static EurekaInstanceConfigBean mockEurekaAz(String az) {
        EurekaInstanceConfigBean eureka = mock(EurekaInstanceConfigBean.class);
        when(eureka.getMetadataMap()).thenReturn(Map.of(EurekaInstanceConfigBeanAddNodeInfoCustomizer.K8S_AZ_INFO, az));
        return eureka;
    }

    /** 转为 AzBalanceUtils 所需的「按 AZ 分组占位列表」结构 */
    private static Map<String, List<Object>> groupByAz(List<ServiceInstance> instances) {
        Map<String, List<Object>> map = new LinkedHashMap<>();
        for (ServiceInstance si : instances) {
            map.computeIfAbsent(azOf(si), k -> new ArrayList<>()).add(new Object());
        }
        return map;
    }

    /** 将分组结果转为 AzBalanceUtils 原始 Map 签名（测试侧避免泛型告警） */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Map<String, List> toRaw(Map<String, List<Object>> typed) {
        Map raw = new LinkedHashMap();
        for (Map.Entry<String, List<Object>> e : typed.entrySet()) {
            raw.put(e.getKey(), e.getValue());
        }
        return raw;
    }

    /** 构造被测 chooser；随机源可注入以实现确定性或 Monte Carlo */
    @SuppressWarnings("unchecked")
    private static K8sAzGumbelLoadBalancerChooser newChooser(
            LoadBalancerK8sAzBalanceProperties props,
            Environment env,
            DiscoveryClient dc,
            EurekaInstanceConfigBean eureka,
            Supplier<Double> random) {
        ObjectProvider<DiscoveryClient> dcp = mock(ObjectProvider.class);
        when(dcp.getIfAvailable()).thenReturn(dc);
        ObjectProvider<EurekaInstanceConfigBean> ecp = mock(ObjectProvider.class);
        when(ecp.getIfAvailable()).thenReturn(eureka);
        return new K8sAzGumbelLoadBalancerChooser(props, env, dcp, ecp, random);
    }

    /** 下游候选实例，metadata 含 K8S AZ */
    private static ServiceInstance instance(String id, String host, int port, String az) {
        return new DefaultServiceInstance(id, SVC, host, port, false,
                Map.of(EurekaInstanceConfigBeanAddNodeInfoCustomizer.K8S_AZ_INFO, az));
    }

    /** Mock 熔断器状态与指标（失败率、缓冲调用数） */
    private static CircuitBreaker circuitBreaker(CircuitBreaker.State state, float failureRate) {
        CircuitBreaker cb = mock(CircuitBreaker.class);
        when(cb.getState()).thenReturn(state);
        CircuitBreaker.Metrics metrics = mock(CircuitBreaker.Metrics.class);
        when(metrics.getFailureRate()).thenReturn(failureRate);
        when(metrics.getNumberOfBufferedCalls()).thenReturn(0);
        when(cb.getMetrics()).thenReturn(metrics);
        return cb;
    }

    /** 与生产一致的 loadBalancedCount 缓存形态（测试中多为空操作） */
    private static LoadingCache<String, AtomicLong> cache() {
        return Caffeine.newBuilder().build(k -> new AtomicLong(0));
    }
}
