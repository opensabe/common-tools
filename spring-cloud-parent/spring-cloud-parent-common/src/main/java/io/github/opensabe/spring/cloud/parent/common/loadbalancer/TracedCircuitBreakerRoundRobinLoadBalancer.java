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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.netflix.eureka.EurekaServiceInstance;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.utils.AlarmUtil;
import io.github.opensabe.spring.cloud.parent.common.eureka.EurekaInstanceConfigBeanAddNodeInfoCustomizer;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.observation.Observation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

//一定必须是实现ReactorServiceInstanceLoadBalancer
//而不是ReactorLoadBalancer<ServiceInstance>
//因为注册的时候是ReactorServiceInstanceLoadBalancer
@Log4j2
@Setter
@Getter
@NoArgsConstructor//仅仅为了单元测试
public class TracedCircuitBreakerRoundRobinLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    public static final String LOAD_BALANCE_KEY = "TracedCircuitBreakerRoundRobinLoadBalancer-load-balance-key";
    public static final String ROUND_ROBIN_KEY = "TracedCircuitBreakerRoundRobinLoadBalancer-round-robin-key";
    public static final String OBSERVATION_KEY = "TracedCircuitBreakerRoundRobinLoadBalancer-observation-key";
    private static final Map<String, AffinityLoadBalancer> LOAD_BALANCE_KEY_MAP = Map.of(
            LOAD_BALANCE_KEY, (serviceInstances, o) -> {
                //必须先取余之后取绝对值，否则对于负最大值的绝对值还会返回负最大值
                int abs = Math.abs(o.hashCode() % serviceInstances.size());
                ServiceInstance serviceInstance = serviceInstances.get(abs);
                log.info("found LOAD_BALANCE_KEY: {}, to service instance: {}:{}", o, serviceInstance.getHost(), serviceInstance.getPort());
                return serviceInstance;
            },
            ROUND_ROBIN_KEY, (serviceInstances, o) -> {
                //必须先取余之后取绝对值，否则对于负最大值的绝对值还会返回负最大值
                int mod = Math.abs(Integer.parseInt(o.toString()) % serviceInstances.size());
                ServiceInstance serviceInstance = serviceInstances.get(mod);
                log.info("found ROUND_ROBIN_KEY: {}, to service instance: {}:{}", o, serviceInstance.getHost(), serviceInstance.getPort());
                return serviceInstance;
            }
    );
    private static final long NEWLY_STARTUP_WEIGHT = 10;
    private static final long HALF_OPEN_WEIGHT = 10;
    //这里通过 RequestDataContext 来确定请求在负载均衡器的上下文
    //需要注意，如果是重试请求，必须使用最初的 RequestDataContext，不能每次重试使用新的 RequestDataContext，否则负载均衡器的上下文也是新的
    //这里使用了 Caffeine 的 weakKeys，如果 RequestDataContext 被回收了，那么对应的 RequestLoadBalancerContext 也会被回收
    //所以，web 和 webflux 还有 gateway 包都加了单元测试验证这一点
    private final Cache<RequestDataContext, RequestLoadBalancerContext> requestRequestDataContextMap =
            Caffeine.newBuilder().weakKeys().weakValues().build();
    //负载均衡次数
    private final LoadingCache<String, AtomicLong> loadBalancedCount = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(k -> new AtomicLong(0));
    private ServiceInstanceListSupplier serviceInstanceListSupplier;
    private RoundRobinLoadBalancer degradation;
    private String serviceId;
    private CircuitBreakerExtractor circuitBreakerExtractor;
    private CircuitBreakerRegistry circuitBreakerRegistry;
    private UnifiedObservationFactory unifiedObservationFactory;

    public TracedCircuitBreakerRoundRobinLoadBalancer(
            ServiceInstanceListSupplier serviceInstanceListSupplier,
            RoundRobinLoadBalancer degradation,
            String serviceId,
            CircuitBreakerExtractor circuitBreakerExtractor,
            CircuitBreakerRegistry circuitBreakerRegistry,
            UnifiedObservationFactory unifiedObservationFactory) {
        this.serviceInstanceListSupplier = serviceInstanceListSupplier;
        this.degradation = degradation;
        this.serviceId = serviceId;
        this.circuitBreakerExtractor = circuitBreakerExtractor;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    /**
     * 从 attributes 中提取出我们想要的，然后放入新的 attributes 中
     * 这样做是因为如果直接使用原来的 attributes，会导致一些不必要的属性被传递到下游，
     * 并且这个 Attributes 一般属于弱引用，如果使用原始 Attribute 某些 key 可能强引用导致这个弱引用也变成强引用
     *
     * @param attributes
     * @return
     */
    public static Map<String, Object> transferAttributes(Map<String, Object> attributes) {
        Map<String, Object> result = Maps.newHashMap();
        //负载均指导 key
        for (String key : TracedCircuitBreakerRoundRobinLoadBalancer.LOAD_BALANCE_KEY_MAP.keySet()) {
            Object o = attributes.get(key);
            if (o != null) {
                result.put(key, o);
                break;
            }
        }
        //链路追踪 span
        Object o = attributes.get(OBSERVATION_KEY);
        if (o != null) {
            result.put(OBSERVATION_KEY, o);
        }
        //需要可变的，后续可能会修改
        return result;
    }

    /**
     * 获取要在 loadBalancedCount 添加的次数，某些情况下，要让实例增加的更多减少负载均衡次数：
     * 1. 实例刚注册没多久，实例需要 JIT 编译以及加载一些类，所以不要把过多请求发过去导致请求堆积
     * 2. 断路器处于 HALF_OPEN 状态，只能接受一定的请求，超过请求个数就还是相当于 OPEN，要限制请求个数
     *
     * @param serviceInstance
     * @return
     */
    private static long getServiceInstanceCallWeightedIncrement(ServiceInstance serviceInstance, CircuitBreaker circuitBreaker) {
        if (isNewlyStartup(serviceInstance)) {
            log.info("TracedCircuitBreakerRoundRobinLoadBalancer-getServiceInstanceCallWeightedIncrement: instance {} is started up within 3 minutes, weight is {}", serviceInstance.getInstanceId(), NEWLY_STARTUP_WEIGHT);
            return NEWLY_STARTUP_WEIGHT;
        }
        if (circuitBreaker != null && Objects.equals(circuitBreaker.getState(), CircuitBreaker.State.HALF_OPEN)) {
            log.info("TracedCircuitBreakerRoundRobinLoadBalancer-getServiceInstanceCallWeightedIncrement: instance {} is HALF_OPEN, weight is {}", serviceInstance.getInstanceId(), HALF_OPEN_WEIGHT);
            return HALF_OPEN_WEIGHT;
        }
        return 1L;
    }

    private static boolean isNewlyStartup(ServiceInstance serviceInstance) {
        if (serviceInstance instanceof EurekaServiceInstance) {
            EurekaServiceInstance eurekaServiceInstance = (EurekaServiceInstance) serviceInstance;
            InstanceInfo instanceInfo = eurekaServiceInstance.getInstanceInfo();
            if (instanceInfo != null) {
                LeaseInfo leaseInfo = instanceInfo.getLeaseInfo();
                if (leaseInfo != null) {
                    long serviceUpTimestamp = leaseInfo.getServiceUpTimestamp();
                    //实例启动 5 分钟内，
                    if (serviceUpTimestamp > 0 && System.currentTimeMillis() - serviceUpTimestamp < 5 * 60 * 1000) {
                        log.info("TracedCircuitBreakerRoundRobinLoadBalancer-isNewlyStartup: instance {} is started up within 5 minutes", serviceInstance.getInstanceId());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        return serviceInstanceListSupplier.get().next()
                .flatMap(serviceInstances -> {
                    Response<ServiceInstance> instanceResponse = null;
                    if (request != null) {
                        Object context = request.getContext();
                        if (context instanceof RequestDataContext) {
                            RequestDataContext requestDataContext = (RequestDataContext) context;
                            Observation observation = (Observation) requestDataContext.getClientRequest().getAttributes().get(OBSERVATION_KEY);
                            if (observation == null) {
                                //我们这里并不是想启动新的 Observation，只是想复用老的，如果老的不存在，其实是有问题的
                                observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
                            }
                            instanceResponse = observation.scoped(() -> {
                                return getInstanceResponse(serviceInstances, requestDataContext);
                            });
                        } else {
                            AlarmUtil.fatal("TracedCircuitBreakerRoundRobinLoadBalancer-getInstanceResponse encounter context is not RequestDataContext {}, please check", context.getClass());
                        }
                    }
                    //如果为null，就降级为 RoundRobin
                    if (instanceResponse == null) {
                        return degradation.choose(request);
                    }
                    return Mono.just(instanceResponse);
                });
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> serviceInstances,
                                                          RequestDataContext requestDataContext) {
        if (CollectionUtils.isEmpty(serviceInstances)) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }

        Map<ServiceInstance, CircuitBreaker> serviceInstanceCircuitBreakerMap = serviceInstances.stream()
                .collect(Collectors.toMap(serviceInstance -> serviceInstance, v -> {
                    return circuitBreakerExtractor
                            .getCircuitBreaker(
                                    circuitBreakerRegistry, requestDataContext,
                                    v.getHost(), v.getPort()
                            );
                }));
        RequestLoadBalancerContext requestLoadBalancerContext = requestRequestDataContextMap
                .get(requestDataContext, k -> {
                    return new RequestLoadBalancerContext();
                });
        Response<ServiceInstance> result = null;
        RequestData clientRequest = requestDataContext.getClientRequest();
        if (clientRequest != null) {
            //第一次，尝试使用 clientRequest 信息进行负载均衡
            if (requestLoadBalancerContext.count == 0) {
                result = loadBalancedByClientRequest(serviceInstances, clientRequest);
                if (result == null) {
                    result = loadBalancedByRequestLoadBalancerContext(
                            serviceInstances, requestLoadBalancerContext, serviceInstanceCircuitBreakerMap
                    );
                } else {
                    if (isNewlyStartup(result.getServer())) {
                        log.info("TracedCircuitBreakerRoundRobinLoadBalancer-getInstanceResponse newly startup, consider normal load balance");
                        result = loadBalancedByRequestLoadBalancerContext(
                                serviceInstances, requestLoadBalancerContext, serviceInstanceCircuitBreakerMap
                        );
                    }
                }
            } else {
                //对于之后的重试，就不尝试使用 clientRequest 信息进行负载均衡
                result = loadBalancedByRequestLoadBalancerContext(
                        serviceInstances, requestLoadBalancerContext, serviceInstanceCircuitBreakerMap
                );
            }
        } else {
            AlarmUtil.fatal("TracedCircuitBreakerRoundRobinLoadBalancer-getInstanceResponse encounter context is RequestDataContext but clientRequest is null, please check");
            result = loadBalancedByRequestLoadBalancerContext(
                    serviceInstances, requestLoadBalancerContext, serviceInstanceCircuitBreakerMap
            );
        }
        recordCurrentLoadBalanceContext(requestLoadBalancerContext, result.getServer(), serviceInstanceCircuitBreakerMap);
        return result;
    }

    private Response<ServiceInstance> loadBalancedByRequestLoadBalancerContext(
            List<ServiceInstance> serviceInstances,
            RequestLoadBalancerContext requestLoadBalancerContext,
            Map<ServiceInstance, CircuitBreaker> serviceInstanceCircuitBreakerMap
    ) {

        serviceInstances = serviceInstances.stream().distinct().collect(Collectors.toList());
        Collections.shuffle(serviceInstances);
        //如果 calledIps 包含了所有服务实例，那么证明所有实例都出现了问题，或者在同一个请求 traceId 中多次调用了同一个微服务的接口
        //这时候我们需要清空 calledIps 防止之后的请求重试到相同实例
        if (serviceInstances.stream().allMatch(serviceInstance -> {
            return requestLoadBalancerContext.calledInstances.contains(getInstanceKey(serviceInstance));
        })) {
            log.info("TracedCircuitBreakerRoundRobinLoadBalancer-getInstanceResponseByRoundRobin: calledInstances contains all instances, clear");
            requestLoadBalancerContext.calledInstances.clear();
            requestLoadBalancerContext.calledNodes.clear();
        }
        //需要先将所有参数缓存起来，否则 comparator 会调用多次，并且可能在排序过程中参数发生改变
        Map<ServiceInstance, ServiceInstanceStat> statMap = serviceInstances.stream().collect(Collectors.toMap(k -> k,
                v -> ServiceInstanceStat.builder()
                        .host(v.getHost())
                        .port(v.getPort())
                        .called(requestLoadBalancerContext.calledInstances.contains(getInstanceKey(v)) ? 1 : 0)
                        .calledNode(requestLoadBalancerContext.calledNodes.contains(v.getMetadata().get(EurekaInstanceConfigBeanAddNodeInfoCustomizer.K8S_NODE_INFO)) ? 1 : 0)
                        .state(serviceInstanceCircuitBreakerMap.get(v).getState())
                        .failureRate(serviceInstanceCircuitBreakerMap.get(v).getMetrics().getFailureRate())
                        .recentLoadBalancedCount(loadBalancedCount.get(v.getInstanceId()).get())
                        .numberOfBufferedCalls(serviceInstanceCircuitBreakerMap.get(v).getMetrics().getNumberOfBufferedCalls())
                        .build()
        ));
        serviceInstances = serviceInstances.stream().sorted(
                Comparator
                        //当前断路器没有打开的优先
                        .<ServiceInstance>comparingInt(serviceInstance -> {
                            CircuitBreaker.State state = statMap.get(serviceInstance).state;
                            //但是 closed 与 half open 需要同级，否则 half open 可能永远没有流量，永远变不成 closed
                            if (state == CircuitBreaker.State.HALF_OPEN) {
                                state = CircuitBreaker.State.CLOSED;
                            }
                            return state.getOrder();
                        })
                        //之前已经调用过的 ip，这里排后面
                        .thenComparingInt(serviceInstance -> {
                            return statMap.get(serviceInstance).called;
                        })
                        //之前已经调用过的网段，这里排后面
                        .thenComparingInt(serviceInstance -> {
                            return statMap.get(serviceInstance).calledNode;
                        })
                        //当前错误率最少的
                        .thenComparingDouble(serviceInstance -> {
                            float failureRate = statMap.get(serviceInstance).failureRate;
                            return failureRate < 0.0 ? 0.0 : failureRate;
                        })
                        //历史负载均衡次数最少的
                        .thenComparingLong(serviceInstance -> {
                            return statMap.get(serviceInstance).recentLoadBalancedCount;
                        })
                        //当前负载请求最少的，这个有一定的延迟，因为负载均衡返回到真正执行，有一定间隔
                        .thenComparingLong(serviceInstance -> {
                            return statMap.get(serviceInstance).numberOfBufferedCalls;
                        })
        ).peek(serviceInstance -> {
            if (requestLoadBalancerContext.detailLog) {
                log.info("loadbalancer stat sorted: {}:{} -> {}", serviceInstance.getHost(), serviceInstance.getPort(), statMap.get(serviceInstance));
            } else {
                log.debug("loadbalancer stat sorted: {}:{} -> {}", serviceInstance.getHost(), serviceInstance.getPort(), statMap.get(serviceInstance));
            }
        }).collect(Collectors.toList());
        if (serviceInstances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }
        ServiceInstance serviceInstance = serviceInstances.get(0);
        return new DefaultResponse(serviceInstance);
    }

    private Response<ServiceInstance> loadBalancedByClientRequest(List<ServiceInstance> serviceInstances, RequestData clientRequest) {
        Map<String, Object> attributes = clientRequest.getAttributes();
        for (Map.Entry<String, AffinityLoadBalancer> entry : LOAD_BALANCE_KEY_MAP.entrySet()) {
            Object o = attributes.get(entry.getKey());
            if (o != null) {
                return new DefaultResponse(entry.getValue().execute(serviceInstances, o));
            }
        }
        return null;
    }

    private void recordCurrentLoadBalanceContext(RequestLoadBalancerContext requestLoadBalancerContext, ServiceInstance serviceInstance, Map<ServiceInstance, CircuitBreaker> serviceInstanceCircuitBreakerMap) {
        requestLoadBalancerContext.calledInstances.add(getInstanceKey(serviceInstance));
        String s = serviceInstance.getMetadata().get(EurekaInstanceConfigBeanAddNodeInfoCustomizer.K8S_NODE_INFO);
        if (StringUtils.isNotBlank(s)) {
            requestLoadBalancerContext.calledNodes.add(s);
        }
        requestLoadBalancerContext.count++;
        loadBalancedCount.get(serviceInstance.getInstanceId())
                .getAndAdd(
                        getServiceInstanceCallWeightedIncrement(
                                serviceInstance,
                                serviceInstanceCircuitBreakerMap.get(serviceInstance)
                        )
                );
    }

    private String getInstanceKey(ServiceInstance serviceInstance) {
        return serviceInstance.getHost() + ":" + serviceInstance.getPort();
    }

    @Data
    @NoArgsConstructor
    private static class RequestLoadBalancerContext {
        //调用过的实例列表
        private final Set<String> calledInstances = Sets.newHashSet();
        //调用过的 Node 列表
        private final Set<String> calledNodes = Sets.newHashSet();
        //对于这个请求，日志级别是 DEBUG (detailLog = false) 或者 INFO (detailLog = true)
        //%1 的概率是 INFO 级别输出，精简日志量
        private final boolean detailLog = ThreadLocalRandom.current().nextInt(0, 100) < 10;
        //请求执行次数，第一次是 0，大于 0 代表是重试
        private int count = 0;
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
