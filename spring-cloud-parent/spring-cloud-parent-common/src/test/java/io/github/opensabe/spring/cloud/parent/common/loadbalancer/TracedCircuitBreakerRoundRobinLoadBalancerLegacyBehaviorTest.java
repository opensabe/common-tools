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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.common.eureka.EurekaInstanceConfigBeanAddNodeInfoCustomizer;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

/**
 * 验证在未走 K8S AZ 选择路径时，{@link TracedCircuitBreakerRoundRobinLoadBalancer} 与改造前一致。
 * <p>
 * 覆盖：六参构造（K8S 依赖为 null）、八参但 {@code enabled=false}、八参且 chooser 返回 empty、
 * 八参传入 null props/null chooser 与六参等价。通过同包可见的 {@code selectServiceInstanceForTest} 驱动选路。
 */
@DisplayName("TracedCircuitBreakerRoundRobinLoadBalancer 遗留行为")
class TracedCircuitBreakerRoundRobinLoadBalancerLegacyBehaviorTest {

    /** 被测负载均衡器 serviceId，仅用于构造 DefaultServiceInstance */
    private static final String SVC = "legacy-svc";

    @Test
    @DisplayName("六参构造：首次选择对全列表按 LOAD_BALANCE_KEY 取模（与旧版一致）")
    void sixArgConstructor_nullK8sBeans_firstPickUsesLoadBalanceKeyOnFullList() {
        TracedCircuitBreakerRoundRobinLoadBalancer lb = newBalancerWithUniformCircuitBreaker();
        ServiceInstance i0 = instance("a", "h0", 80);
        ServiceInstance i1 = instance("b", "h1", 81);
        Object key = "uid-stable";
        RequestDataContext ctx = requestContextWithLoadBalanceKey(key);

        int expectedIndex = Math.abs(key.hashCode() % 2);
        ServiceInstance expected = expectedIndex == 0 ? i0 : i1;

        Response<ServiceInstance> r = lb.selectServiceInstanceForTest(List.of(i0, i1), ctx);
        assertEquals(expected.getHost(), r.getServer().getHost());
        assertEquals(expected.getPort(), r.getServer().getPort());
    }

    @Test
    @DisplayName("六参构造：重试跳过亲和；未调用实例优先（本数据下为低失败率 h1）")
    void sixArgConstructor_retrySkipsAffinityAndSortsByFailureRate() {
        CircuitBreakerExtractor extractor = mock(CircuitBreakerExtractor.class);
        CircuitBreakerRegistry registry = mock(CircuitBreakerRegistry.class);
        // 先构造 CB，避免在 when().thenReturn(...) 求值时嵌套调用 when 触发 UnfinishedStubbing
        CircuitBreaker cbHigh = circuitBreakerWithFailureRate(0.95f);
        CircuitBreaker cbLow = circuitBreakerWithFailureRate(0.02f);
        when(extractor.getCircuitBreaker(eq(registry), any(), eq("h0"), eq(80))).thenReturn(cbHigh);
        when(extractor.getCircuitBreaker(eq(registry), any(), eq("h1"), eq(81))).thenReturn(cbLow);

        TracedCircuitBreakerRoundRobinLoadBalancer lb = new TracedCircuitBreakerRoundRobinLoadBalancer(
                mock(ServiceInstanceListSupplier.class),
                mock(RoundRobinLoadBalancer.class),
                SVC,
                extractor,
                registry,
                mock(UnifiedObservationFactory.class));

        ServiceInstance i0 = instance("a", "h0", 80);
        ServiceInstance i1 = instance("b", "h1", 81);
        List<ServiceInstance> pair = List.of(i0, i1);
        // 首次必须命中 h0（高失败率）：重试路径优先未调用实例，再比失败率，否则会稳定选到另一侧
        Object key = loadBalanceKeyHashingToListIndex(pair, 0);
        RequestDataContext ctx = requestContextWithLoadBalanceKey(key);

        Response<ServiceInstance> first = lb.selectServiceInstanceForTest(pair, ctx);
        assertEquals("h0", first.getServer().getHost());
        Response<ServiceInstance> second = lb.selectServiceInstanceForTest(pair, ctx);
        assertEquals("h1", second.getServer().getHost());
        assertEquals(81, second.getServer().getPort());
    }

    @Test
    @DisplayName("K8S 开关关闭：不调用 chooser，首次选择与六参 legacy 相同")
    void k8sPropertiesDisabled_chooserNeverInvoked_firstPickSameAsLegacy() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(false);
        K8sAzGumbelLoadBalancerChooser chooser = mock(K8sAzGumbelLoadBalancerChooser.class);

        TracedCircuitBreakerRoundRobinLoadBalancer lb = newBalancerEightArg(props, chooser);
        ServiceInstance i0 = instance("a", "h0", 80);
        ServiceInstance i1 = instance("b", "h1", 81);
        Object key = "k";
        RequestDataContext ctx = requestContextWithLoadBalanceKey(key);

        int expectedIndex = Math.abs(key.hashCode() % 2);
        ServiceInstance expected = expectedIndex == 0 ? i0 : i1;
        Response<ServiceInstance> r = lb.selectServiceInstanceForTest(List.of(i0, i1), ctx);

        assertEquals(expected.getHost(), r.getServer().getHost());
        verify(chooser, never()).choose(anyString(), any(), any(), any(), any(), anyBoolean(), any());
    }

    @Test
    @DisplayName("K8S 开启但 chooser 返回 empty：回退 legacy 按 LOAD_BALANCE_KEY 取模")
    void k8sEnabledButChooserReturnsEmpty_delegatesToLegacyAffinity() {
        LoadBalancerK8sAzBalanceProperties props = new LoadBalancerK8sAzBalanceProperties();
        props.setEnabled(true);
        K8sAzGumbelLoadBalancerChooser chooser = mock(K8sAzGumbelLoadBalancerChooser.class);
        when(chooser.choose(anyString(), any(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(Optional.empty());

        TracedCircuitBreakerRoundRobinLoadBalancer lb = newBalancerEightArg(props, chooser);
        ServiceInstance i0 = instance("a", "h0", 80);
        ServiceInstance i1 = instance("b", "h1", 81);
        Object key = "z";
        RequestDataContext ctx = requestContextWithLoadBalanceKey(key);
        int expectedIndex = Math.abs(key.hashCode() % 2);
        ServiceInstance expected = expectedIndex == 0 ? i0 : i1;

        Response<ServiceInstance> r = lb.selectServiceInstanceForTest(List.of(i0, i1), ctx);
        assertEquals(expected.getHost(), r.getServer().getHost());
    }

    @Test
    @DisplayName("八参构造传入 null props 与 null chooser：与六参构造选路结果一致")
    void eightArgWithNullK8sBehavesSameAsSixArg() {
        CircuitBreakerExtractor extractor = mock(CircuitBreakerExtractor.class);
        CircuitBreakerRegistry registry = mock(CircuitBreakerRegistry.class);
        CircuitBreaker cb = circuitBreakerWithFailureRate(0f);
        when(extractor.getCircuitBreaker(eq(registry), any(), anyString(), anyInt())).thenReturn(cb);

        TracedCircuitBreakerRoundRobinLoadBalancer six = new TracedCircuitBreakerRoundRobinLoadBalancer(
                mock(ServiceInstanceListSupplier.class),
                mock(RoundRobinLoadBalancer.class),
                SVC,
                extractor,
                registry,
                mock(UnifiedObservationFactory.class));
        TracedCircuitBreakerRoundRobinLoadBalancer eight = new TracedCircuitBreakerRoundRobinLoadBalancer(
                mock(ServiceInstanceListSupplier.class),
                mock(RoundRobinLoadBalancer.class),
                SVC,
                extractor,
                registry,
                mock(UnifiedObservationFactory.class),
                null,
                null);

        ServiceInstance i0 = instance("a", "h0", 80);
        ServiceInstance i1 = instance("b", "h1", 81);
        RequestDataContext ctxSix = requestContextWithLoadBalanceKey("same");
        RequestDataContext ctxEight = requestContextWithLoadBalanceKey("same");

        Response<ServiceInstance> r6 = six.selectServiceInstanceForTest(List.of(i0, i1), ctxSix);
        Response<ServiceInstance> r8 = eight.selectServiceInstanceForTest(List.of(i0, i1), ctxEight);
        assertSame(r6.getServer().getHost(), r8.getServer().getHost());
        assertEquals(r6.getServer().getPort(), r8.getServer().getPort());
    }

    /** 熔断指标相同，便于隔离「选路」与「熔断排序」 */
    private static TracedCircuitBreakerRoundRobinLoadBalancer newBalancerWithUniformCircuitBreaker() {
        CircuitBreakerExtractor extractor = mock(CircuitBreakerExtractor.class);
        CircuitBreakerRegistry registry = mock(CircuitBreakerRegistry.class);
        CircuitBreaker cb = circuitBreakerWithFailureRate(0f);
        when(extractor.getCircuitBreaker(eq(registry), any(), anyString(), anyInt())).thenReturn(cb);
        return new TracedCircuitBreakerRoundRobinLoadBalancer(
                mock(ServiceInstanceListSupplier.class),
                mock(RoundRobinLoadBalancer.class),
                SVC,
                extractor,
                registry,
                mock(UnifiedObservationFactory.class));
    }

    /** 八参构造：注入 K8S 配置与 chooser mock，用于验证开关与委托 */
    private static TracedCircuitBreakerRoundRobinLoadBalancer newBalancerEightArg(
            LoadBalancerK8sAzBalanceProperties props,
            K8sAzGumbelLoadBalancerChooser chooser) {
        CircuitBreakerExtractor extractor = mock(CircuitBreakerExtractor.class);
        CircuitBreakerRegistry registry = mock(CircuitBreakerRegistry.class);
        CircuitBreaker cb = circuitBreakerWithFailureRate(0f);
        when(extractor.getCircuitBreaker(eq(registry), any(), anyString(), anyInt())).thenReturn(cb);
        return new TracedCircuitBreakerRoundRobinLoadBalancer(
                mock(ServiceInstanceListSupplier.class),
                mock(RoundRobinLoadBalancer.class),
                SVC,
                extractor,
                registry,
                mock(UnifiedObservationFactory.class),
                props,
                chooser);
    }

    /** CLOSED 状态 + 指定失败率，供排序分支使用 */
    private static CircuitBreaker circuitBreakerWithFailureRate(float failureRate) {
        CircuitBreaker cb = mock(CircuitBreaker.class);
        when(cb.getState()).thenReturn(CircuitBreaker.State.CLOSED);
        CircuitBreaker.Metrics metrics = mock(CircuitBreaker.Metrics.class);
        when(metrics.getFailureRate()).thenReturn(failureRate);
        when(metrics.getNumberOfBufferedCalls()).thenReturn(0);
        when(cb.getMetrics()).thenReturn(metrics);
        return cb;
    }

    /** 带固定 AZ metadata 的候选实例（满足排序里 node 元数据读取路径） */
    private static ServiceInstance instance(String id, String host, int port) {
        return new DefaultServiceInstance(id, SVC, host, port, false,
                Map.of(EurekaInstanceConfigBeanAddNodeInfoCustomizer.K8S_AZ_INFO, "az-x"));
    }

    /** 构造带 LOAD_BALANCE_KEY 的 RequestDataContext，与线上 choose 入参一致 */
    private static RequestDataContext requestContextWithLoadBalanceKey(Object key) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(TracedCircuitBreakerRoundRobinLoadBalancer.LOAD_BALANCE_KEY, key);
        RequestData rd = new RequestData(HttpMethod.GET, URI.create("http://test"), new HttpHeaders(), null, attrs);
        return new RequestDataContext(rd, null);
    }

    /**
     * 与 {@link TracedCircuitBreakerRoundRobinLoadBalancer} 中 LOAD_BALANCE_KEY 亲和一致：
     * {@code Math.abs(key.hashCode() % list.size())} 对应列表下标。
     */
    private static Object loadBalanceKeyHashingToListIndex(List<ServiceInstance> ordered, int indexInList) {
        int n = ordered.size();
        for (int i = 0; i < 100_000; i++) {
            Object k = "lb-retry-" + i;
            if (Math.abs(k.hashCode() % n) == indexInList) {
                return k;
            }
        }
        throw new IllegalStateException("no key for index " + indexInList + " size " + n);
    }
}
