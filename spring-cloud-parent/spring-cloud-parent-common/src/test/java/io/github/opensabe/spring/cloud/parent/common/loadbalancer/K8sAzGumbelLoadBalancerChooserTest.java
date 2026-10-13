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
 * 与 {@link AzBalanceUtilsTest} 一致：<strong>source</strong> 为调方（本服务）在 Discovery 中按 AZ 的实例数分布，
 * <strong>target</strong> 为下游候选按 AZ 的分布；本机 AZ 仅决定取矩阵的哪一行。不得用「source 全在一 AZ、target 全在另一 AZ」
 * 来代表跨 AZ——跨 AZ 指同一矩阵内源 AZ 与目标 AZ 不一致时的分配比例。
 * <p>
 * Mock Discovery / Eureka / 随机源；断言与失败消息使用英文，便于日志与 CI 检索。
 */
@DisplayName("K8sAzGumbelLoadBalancerChooser")
class K8sAzGumbelLoadBalancerChooserTest {

    /** 被调下游服务 id（仅测试数据） */
    private static final String SVC = "downstream";
    /** spring.application.name，用于 buildSourceAzMap */
    private static final String APP = "caller";
    /** 与 AzBalanceUtilsTest 场景命名一致：本机 Eureka 所在 AZ（矩阵行键） */
    private static final String AZ_1 = "az1";
    private static final String AZ_2 = "az2";
    private static final String AZ_3 = "az3";

    /**
     * 默认调方多 AZ 分布（与 {@link AzBalanceUtilsTest}「多可用区不平衡」同量级），用于非 Monte Carlo 用例，
     * 避免 source 地图退化成单 AZ 桶。
     */
    private static final Map<String, Integer> DEFAULT_CALLER_AZ_COUNTS = Map.of(AZ_1, 13, AZ_2, 7);

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
        ServiceInstance i1 = instance("1", "h1", 1, AZ_1);
        CircuitBreaker open = circuitBreaker(CircuitBreaker.State.OPEN, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(i1, open);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelfMultiAz(DEFAULT_CALLER_AZ_COUNTS), mockEurekaAz(AZ_1), () -> 0.5);
        assertTrue(chooser.choose(SVC, List.of(i1), new LoadBalancerRequestTraceContext(), cbMap, null, false, cache()).isEmpty());
    }

    @Test
    @DisplayName("过滤 OPEN 后仅在非 OPEN 实例中选，同 AZ 内命中 CLOSED")
    void chooseFiltersOpenAndPicksClosedInSameAz() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        ServiceInstance openI = instance("open", "bad", 1, AZ_1);
        ServiceInstance closedI = instance("ok", "good", 2, AZ_1);
        CircuitBreaker openCb = circuitBreaker(CircuitBreaker.State.OPEN, 0f);
        CircuitBreaker closedCb = circuitBreaker(CircuitBreaker.State.CLOSED, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(openI, openCb, closedI, closedCb);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelfMultiAz(DEFAULT_CALLER_AZ_COUNTS), mockEurekaAz(AZ_1), () -> 0.5);
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
        ServiceInstance i0 = instance("a", "h0", 10, AZ_1);
        ServiceInstance i1 = instance("b", "h1", 11, AZ_1);
        CircuitBreaker cb = circuitBreaker(CircuitBreaker.State.CLOSED, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(i0, cb, i1, cb);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(TracedCircuitBreakerRoundRobinLoadBalancer.LOAD_BALANCE_KEY, "sticky-uid");
        RequestData rd = new RequestData(HttpMethod.GET, URI.create("http://x"), new HttpHeaders(), null, attrs);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelfMultiAz(DEFAULT_CALLER_AZ_COUNTS), mockEurekaAz(AZ_1), () -> 0.5);
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
                instance("a", "h0", 10, AZ_1),
                instance("b", "h1", 11, AZ_1),
                instance("c", "h2", 12, AZ_1));
        CircuitBreaker cb = circuitBreaker(CircuitBreaker.State.CLOSED, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = new HashMap<>();
        for (ServiceInstance si : inst) {
            cbMap.put(si, cb);
        }

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(TracedCircuitBreakerRoundRobinLoadBalancer.ROUND_ROBIN_KEY, "7");
        RequestData rd = new RequestData(HttpMethod.GET, URI.create("http://x"), new HttpHeaders(), null, attrs);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelfMultiAz(DEFAULT_CALLER_AZ_COUNTS), mockEurekaAz(AZ_1), () -> 0.5);
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
        ServiceInstance i0 = instance("a", "h0", 10, AZ_1);
        ServiceInstance i1 = instance("b", "h1", 11, AZ_1);
        CircuitBreaker cb0 = circuitBreaker(CircuitBreaker.State.CLOSED, 0.5f);
        CircuitBreaker cb1 = circuitBreaker(CircuitBreaker.State.CLOSED, 0.01f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(i0, cb0, i1, cb1);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(TracedCircuitBreakerRoundRobinLoadBalancer.ROUND_ROBIN_KEY, "not-a-number");
        RequestData rd = new RequestData(HttpMethod.GET, URI.create("http://x"), new HttpHeaders(), null, attrs);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelfMultiAz(DEFAULT_CALLER_AZ_COUNTS), mockEurekaAz(AZ_1), () -> 0.5);
        Response<ServiceInstance> r = chooser.choose(SVC, List.of(i0, i1), new LoadBalancerRequestTraceContext(), cbMap, rd, true, cache()).orElseThrow();
        assertEquals("h1", r.getServer().getHost());
    }

    @Test
    @DisplayName("useClientAffinity=false：不解析亲和，按失败率选（对齐重试语义）")
    void retrySkipsAffinityAndUsesSort() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        ServiceInstance i0 = instance("a", "h0", 10, AZ_1);
        ServiceInstance i1 = instance("b", "h1", 11, AZ_1);
        CircuitBreaker cb0 = circuitBreaker(CircuitBreaker.State.CLOSED, 0.8f);
        CircuitBreaker cb1 = circuitBreaker(CircuitBreaker.State.CLOSED, 0.01f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(i0, cb0, i1, cb1);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(TracedCircuitBreakerRoundRobinLoadBalancer.LOAD_BALANCE_KEY, "uid");
        RequestData rd = new RequestData(HttpMethod.GET, URI.create("http://x"), new HttpHeaders(), null, attrs);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelfMultiAz(DEFAULT_CALLER_AZ_COUNTS), mockEurekaAz(AZ_1), () -> 0.5);
        Response<ServiceInstance> retryPick = chooser.choose(SVC, List.of(i0, i1), new LoadBalancerRequestTraceContext(), cbMap, rd, false, cache()).orElseThrow();
        assertEquals("h1", retryPick.getServer().getHost());
    }

    @Test
    @DisplayName("HALF_OPEN 与 CLOSED 排序同级后再比失败率，低失败率实例胜出")
    void halfOpenTreatedLikeClosedThenFailureRateWins() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        ServiceInstance iHo = instance("a", "h0", 10, AZ_1);
        ServiceInstance iCl = instance("b", "h1", 11, AZ_1);
        CircuitBreaker cbHo = circuitBreaker(CircuitBreaker.State.HALF_OPEN, 0.01f);
        CircuitBreaker cbCl = circuitBreaker(CircuitBreaker.State.CLOSED, 0.9f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(iHo, cbHo, iCl, cbCl);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelfMultiAz(DEFAULT_CALLER_AZ_COUNTS), mockEurekaAz(AZ_1), () -> 0.5);
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
            ServiceInstance si = instance("id-" + i, "host-" + i, 30000 + i, AZ_1);
            instances.add(si);
            cbMap.put(si, cb);
        }
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(TracedCircuitBreakerRoundRobinLoadBalancer.LOAD_BALANCE_KEY, "same-user-42");
        RequestData rd = new RequestData(HttpMethod.GET, URI.create("http://x"), new HttpHeaders(), null, attrs);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelfMultiAz(Map.of(AZ_1, 30, AZ_2, 20)), mockEurekaAz(AZ_1), () -> 0.5);
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
    @DisplayName("Monte Carlo：与 AzBalanceUtilsTest「多可用区不平衡」同型 source/target，Gumbel 选中某目标 AZ 比例与矩阵行一致")
    void monteCarloTargetAzShareMatchesAzBalanceWeights() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);

        // AzBalanceUtilsTest 场景4：源 az1:13, az2:7；目标 az1:5, az2:11, az3:3（本机 az1，矩阵取 row az1）
        Map<String, Integer> sourceAzCounts = Map.of(AZ_1, 13, AZ_2, 7);
        Map<String, Integer> targetAzCounts = Map.of(AZ_1, 5, AZ_2, 11, AZ_3, 3);

        List<ServiceInstance> selfCluster = selfInstancesFromAzCounts(sourceAzCounts, "caller-");
        List<ServiceInstance> eligible = downstreamInstancesFromAzCounts(targetAzCounts, "dst-");
        CircuitBreaker cb = circuitBreaker(CircuitBreaker.State.CLOSED, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = eligible.stream().collect(Collectors.toMap(si -> si, si -> cb));

        Map<String, List<Object>> rawSource = groupByAz(selfCluster);
        Map<String, List<Object>> rawTarget = groupByAz(eligible);
        @SuppressWarnings("rawtypes")
        Map<String, Map<String, Integer>> matrix = AzBalanceUtils.getLoadBalancingRatio(toRaw(rawSource), toRaw(rawTarget));
        Map<String, Integer> row = matrix.get(AZ_1);
        assertNotNull(row);

        Map<String, Integer> weights = new LinkedHashMap<>();
        int weightSum = 0;
        for (Map.Entry<String, Integer> e : row.entrySet()) {
            String targetAz = e.getKey();
            int w = e.getValue() == null ? 0 : Math.max(0, e.getValue());
            if (w > 0 && eligible.stream().anyMatch(si -> targetAz.equals(azOf(si)))) {
                weights.put(targetAz, w);
                weightSum += w;
            }
        }
        assertTrue(weightSum > 0, "expected positive usable weights, row=" + row);

        // 选矩阵中权重大于 0 的某一目标 AZ 做二项检验（优先 az3，纯跨区分量更直观）
        final String probeAz = firstPositiveWeightAz(weights, List.of(AZ_3, AZ_2, AZ_1));
        int wProbe = weights.get(probeAz);
        assertTrue(wProbe > 0, "probe AZ weight must be positive, weights=" + weights);
        final double pExpected = wProbe / (double) weightSum;

        Environment env = mockEnvApp();
        DiscoveryClient dc = mock(DiscoveryClient.class);
        when(dc.getInstances(APP)).thenReturn(selfCluster);
        EurekaInstanceConfigBean eureka = mockEurekaAz(AZ_1);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, env, dc, eureka, () -> ThreadLocalRandom.current().nextDouble());

        int trials = 15_000;
        Map<String, Long> countByAz = new HashMap<>();
        for (int i = 0; i < trials; i++) {
            Response<ServiceInstance> r = chooser.choose(SVC, eligible, new LoadBalancerRequestTraceContext(), cbMap, null, false, cache()).orElseThrow();
            String az = azOf(r.getServer());
            countByAz.merge(az, 1L, Long::sum);
        }
        long countProbe = countByAz.getOrDefault(probeAz, 0L);
        double pHat = countProbe / (double) trials;
        // 比例 pHat 的标准误 SE(p) = sqrt(p*(1-p)/n)，勿用计数标准差 sqrt(n*p*(1-p))
        double se = Math.sqrt(pExpected * (1 - pExpected) / trials);

        // 汇总日志：各 AZ 期望比例（来自矩阵行与 eligible 交集）、Monte Carlo 实际比例与次数
        System.out.println();
        System.out.println("=== monteCarloTargetAzShareMatchesAzBalanceWeights ===");
        System.out.println("localSourceAz=" + AZ_1 + ", trials=" + trials + ", weightSum=" + weightSum + ", usableWeights=" + weights);
        System.out.println("probeAz(for 5-sigma binomial)=" + probeAz);
        System.out.println("各可用区：expected_ratio=矩阵可用权重占比，actual_ratio=抽样占比，count=命中次数");
        for (String az : List.of(AZ_1, AZ_2, AZ_3)) {
            int w = weights.getOrDefault(az, 0);
            double expectedRatio = w / (double) weightSum;
            long cnt = countByAz.getOrDefault(az, 0L);
            double actualRatio = cnt / (double) trials;
            System.out.printf("  AZ %-4s  expected_ratio=%.6f  actual_ratio=%.6f  count=%d  (matrix_usable_weight=%d)%n",
                    az, expectedRatio, actualRatio, cnt, w);
        }
        System.out.println("====================================================");
        System.out.println();

        assertTrue(Math.abs(pHat - pExpected) < 5.0 * Math.max(se, 1e-6),
                () -> String.format("Monte Carlo %s share %.4f vs expected %.4f (se~%.6f) weights=%s", probeAz, pHat, pExpected, se, weights));
    }

    @Test
    @DisplayName("无亲和且指标相同：大量随机 shuffle 下流量分散到多个 host、无单点过热")
    void manyEqualInstancesSpreadsAcrossHostsWhenNoAffinity() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        int n = 40;
        List<ServiceInstance> eligible = new ArrayList<>(n);
        CircuitBreaker cb = circuitBreaker(CircuitBreaker.State.CLOSED, 0f);
        Map<ServiceInstance, CircuitBreaker> cbMap = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            ServiceInstance si = instance("id-" + i, "hn-" + i, 60000 + i, AZ_1);
            eligible.add(si);
            cbMap.put(si, cb);
        }
        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelfMultiAz(Map.of(AZ_1, 18, AZ_2, 12)), mockEurekaAz(AZ_1),
                () -> ThreadLocalRandom.current().nextDouble());

        Map<String, Long> hostCounts = new HashMap<>();
        int trials = 800;
        for (int i = 0; i < trials; i++) {
            Response<ServiceInstance> r = chooser.choose(SVC, eligible, new LoadBalancerRequestTraceContext(), cbMap, null, false, cache()).orElseThrow();
            hostCounts.merge(r.getServer().getHost(), 1L, Long::sum);
        }
        long distinctHosts = hostCounts.size();
        assertTrue(distinctHosts >= 3,
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
        ServiceInstance iBad = instance("a", "bad", 1, AZ_1);
        ServiceInstance iGood = instance("b", "good", 2, AZ_1);
        ServiceInstance iMid = instance("c", "mid", 3, AZ_1);
        CircuitBreaker cbBad = circuitBreaker(CircuitBreaker.State.CLOSED, 0.9f);
        CircuitBreaker cbGood = circuitBreaker(CircuitBreaker.State.CLOSED, 0.05f);
        CircuitBreaker cbMid = circuitBreaker(CircuitBreaker.State.CLOSED, 0.4f);
        Map<ServiceInstance, CircuitBreaker> cbMap = Map.of(iBad, cbBad, iGood, cbGood, iMid, cbMid);

        K8sAzGumbelLoadBalancerChooser chooser = newChooser(props, mockEnvApp(), mockDiscoverySelfMultiAz(DEFAULT_CALLER_AZ_COUNTS), mockEurekaAz(AZ_1), () -> 0.5);
        Response<ServiceInstance> r = chooser.choose(SVC, List.of(iBad, iGood, iMid), new LoadBalancerRequestTraceContext(),
                cbMap, null, false, cache()).orElseThrow();
        assertEquals("good", r.getServer().getHost());
        assertEquals(2, r.getServer().getPort());
    }

    /** 按优先级取第一个在 weights 中大于 0 的 AZ（供 lambda 捕获 final） */
    private static String firstPositiveWeightAz(Map<String, Integer> weights, List<String> preferenceOrder) {
        for (String az : preferenceOrder) {
            if (weights.getOrDefault(az, 0) > 0) {
                return az;
            }
        }
        throw new IllegalStateException("no AZ with positive weight: " + weights);
    }

    /** 读取实例 metadata 中的 K8S AZ，与生产常量一致 */
    private static String azOf(ServiceInstance si) {
        String az = si.getMetadata().get(EurekaInstanceConfigBeanAddNodeInfoCustomizer.K8S_AZ_INFO);
        return az == null || az.isBlank() ? EurekaInstanceConfigBeanAddNodeInfoCustomizer.DEFAULT_AZ_INFO : az;
    }

    /**
     * 调方（本服务）在 Discovery 中的实例列表：按 AZ 计数，语义与 {@link AzBalanceUtilsTest} 中 source 地图一致。
     */
    private static List<ServiceInstance> selfInstancesFromAzCounts(Map<String, Integer> azToCount, String idPrefix) {
        List<ServiceInstance> out = new ArrayList<>();
        int seq = 0;
        for (Map.Entry<String, Integer> e : new LinkedHashMap<>(azToCount).entrySet()) {
            String az = e.getKey();
            int n = e.getValue();
            for (int i = 0; i < n; i++) {
                out.add(instance(idPrefix + az + "-" + i, "caller-" + az + "-" + i, 2000 + seq, az));
                seq++;
            }
        }
        return out;
    }

    /** 下游候选按 AZ 计数，语义与 AzBalanceUtilsTest 中 target 地图一致 */
    private static List<ServiceInstance> downstreamInstancesFromAzCounts(Map<String, Integer> azToCount, String idPrefix) {
        List<ServiceInstance> list = new ArrayList<>();
        int port = 40_000;
        for (Map.Entry<String, Integer> e : new LinkedHashMap<>(azToCount).entrySet()) {
            String az = e.getKey();
            for (int i = 0; i < e.getValue(); i++) {
                list.add(instance(idPrefix + az + "-" + i, "dst-" + az + "-" + i, port++, az));
            }
        }
        return list;
    }

    /** Mock Discovery：本服务多 AZ 分布（buildSourceAzMap） */
    private static DiscoveryClient mockDiscoverySelfMultiAz(Map<String, Integer> azToCount) {
        DiscoveryClient dc = mock(DiscoveryClient.class);
        when(dc.getInstances(APP)).thenReturn(selfInstancesFromAzCounts(azToCount, "self-"));
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
