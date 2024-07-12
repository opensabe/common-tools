package io.github.opensabe.spring.cloud.parent.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import io.github.opensabe.spring.cloud.parent.common.CommonConstant;
import io.github.opensabe.spring.cloud.parent.common.loadbalancer.TracedCircuitBreakerRoundRobinLoadBalancer;
import io.github.opensabe.spring.cloud.parent.webflux.common.TracedPublisherFactory;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycle;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycleValidator;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.ResponseData;
import org.springframework.cloud.gateway.config.GatewayLoadBalancerProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

/**
 * 默认的 ReactiveLoadBalancerClientFilter 里面可能会有 span 缺失，所以这里修正下
 * 并且加入了对于 socketio 的端口识别，路径必须包含 socket.io
 * @see TraceIdFilter
 * @see AbstractTracedFilter
 * @see org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter
 */
@Log4j2
@Component
public class TracedReactiveLoadBalancerClientFilter extends ReactiveLoadBalancerClientFilter {
    private final LoadBalancerClientFactory clientFactory;

    private final GatewayLoadBalancerProperties properties;


    public TracedReactiveLoadBalancerClientFilter(
            LoadBalancerClientFactory clientFactory,
            GatewayLoadBalancerProperties properties
    ) {
        super(clientFactory, properties);
        this.clientFactory = clientFactory;
        this.properties = properties;
    }

    /**
     * 不能使用并发不安全的 WeakHashMap
     * 需要使用 weakKeys，因为这里不确定何时 Request 会结束，需要保证不影响 Request 的垃圾回收
     * value 跟随 key 回收，value 不能使用 WeakReference
     */
    //由于使用了 Observation，Observation 的 context 里面有 ServerHttpRequest
    //但是 DefaultRequest 引用了 Observation，形成闭环，导致 weakKey 失效
    //所以这里使用 ServerWebExchange 作为 key
    private final Cache<ServerWebExchange, DefaultRequest> serverRequestDataContextMap =
            Caffeine.newBuilder()
                    .weakKeys()
                    .weakValues()
                    //这里设置 1 小时，如果有因为超过时间过期的证明可能有内存泄漏
                    .expireAfterAccess(Duration.ofHours(1))
                    .evictionListener((ServerWebExchange key, DefaultRequest value, RemovalCause cause) -> {
                        if (cause == RemovalCause.EXPIRED) {
                            log.warn("serverRequestDataContextMap expired, key: {}, value: {}, maybe memroy leak", key, value);
                        }
                    })
                    .build();

    @Autowired
    private TracedPublisherFactory tracedPublisherFactory;

    //为了有 traceId
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Observation observation = TraceIdFilter.getObservation(exchange);
        URI url = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        String schemePrefix = exchange.getAttribute(GATEWAY_SCHEME_PREFIX_ATTR);
        if (url == null || (!"lb".equals(url.getScheme()) && !"lb".equals(schemePrefix))) {
            return chain.filter(exchange);
        }
        // preserve the original url
        addOriginalRequestUrl(exchange, url);

        if (log.isTraceEnabled()) {
            log.trace(ReactiveLoadBalancerClientFilter.class.getSimpleName() + " url before: " + url);
        }

        URI requestUri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        String serviceId = requestUri.getHost();
        Set<LoadBalancerLifecycle> supportedLifecycleProcessors = LoadBalancerLifecycleValidator
                .getSupportedLifecycleProcessors(clientFactory.getInstances(serviceId, LoadBalancerLifecycle.class),
                        RequestDataContext.class, ResponseData.class, ServiceInstance.class);
        //修正 RequestAttribute 加入 Span，让 LoadBalancer 也能获取到，因为 LoadBalancer 是异步的
        DefaultRequest<RequestDataContext> lbRequest =
                serverRequestDataContextMap.get(exchange, k -> {
                    return new DefaultRequest<>(
                            new RequestDataContext(
                                    new RequestData(
                                            exchange.getRequest(),
                                            //将 Attributes 放入 RequestData
                                            TracedCircuitBreakerRoundRobinLoadBalancer
                                                    .transferAttributes(exchange.getAttributes())
                                    ), getHint(serviceId)
                            )
                    );
                });
        return choose(lbRequest, serviceId, supportedLifecycleProcessors, observation).doOnNext(response -> {

                    if (!response.hasServer()) {
                        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                                .onComplete(new CompletionContext<>(CompletionContext.Status.DISCARD, lbRequest, response)));
                        throw NotFoundException.create(properties.isUse404(), "Unable to find instance for " + url.getHost());
                    }

                    ServiceInstance retrievedInstance = response.getServer();

                    URI uri = exchange.getRequest().getURI();

                    // if the `lb:<scheme>` mechanism was used, use `<scheme>` as the default,
                    // if the loadbalancer doesn't provide one.
                    String overrideScheme = retrievedInstance.isSecure() ? "https" : "http";
                    if (schemePrefix != null) {
                        overrideScheme = url.getScheme();
                    }

                    DelegatingServiceInstance serviceInstance = new DelegatingServiceInstance(retrievedInstance,
                            overrideScheme);

                    URI requestUrl = reconstructURI(serviceInstance, uri);

                    if (log.isTraceEnabled()) {
                        log.trace("LoadBalancerClientFilter url chosen: " + requestUrl);
                    }
                    exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
                    exchange.getAttributes().put(GATEWAY_LOADBALANCER_RESPONSE_ATTR, response);
                    supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStartRequest(lbRequest, response));
                }).then(chain.filter(exchange))
                .doOnError(throwable -> supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                        .onComplete(new CompletionContext<ResponseData, ServiceInstance, RequestDataContext>(
                                CompletionContext.Status.FAILED, throwable, lbRequest,
                                exchange.getAttribute(GATEWAY_LOADBALANCER_RESPONSE_ATTR)))))
                .doOnSuccess(aVoid -> supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                        .onComplete(new CompletionContext<ResponseData, ServiceInstance, RequestDataContext>(
                                CompletionContext.Status.SUCCESS, lbRequest,
                                exchange.getAttribute(GATEWAY_LOADBALANCER_RESPONSE_ATTR),
                                new ResponseData(exchange.getResponse(), new RequestData(exchange.getRequest()))))));
    }

    private Mono<Response<ServiceInstance>> choose(Request<RequestDataContext> lbRequest, String serviceId,
                                                   Set<LoadBalancerLifecycle> supportedLifecycleProcessors,
                                                   Observation observation) {
        ReactorLoadBalancer<ServiceInstance> loadBalancer = this.clientFactory.getInstance(serviceId,
                ReactorServiceInstanceLoadBalancer.class);
        if (loadBalancer == null) {
            throw new NotFoundException("No loadbalancer available for " + serviceId);
        }
        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStart(lbRequest));
        return tracedPublisherFactory.getTracedMono(loadBalancer.choose(lbRequest), observation);
    }

    private String getHint(String serviceId) {
        LoadBalancerProperties loadBalancerProperties = clientFactory.getProperties(serviceId);
        Map<String, String> hints = loadBalancerProperties.getHint();
        String defaultHint = hints.getOrDefault("default", "default");
        String hintPropertyValue = hints.get(serviceId);
        return hintPropertyValue != null ? hintPropertyValue : defaultHint;
    }

    /**
     * 覆盖重写 URI 的方法，因为里面可能会有 SOCKET.IO 的路径，需要替换下
     * @param serviceInstance
     * @param original
     * @return
     */
    @Override
    protected URI reconstructURI(ServiceInstance serviceInstance, URI original) {
        if (original.getPath().contains(CommonConstant.SOCKET_IO_PATH)) {
            if (serviceInstance.getMetadata().containsKey(CommonConstant.SOCKET_IO_PATH)) {
                String port = serviceInstance.getMetadata().get(CommonConstant.SOCKET_IO_PATH);
                serviceInstance = new DefaultServiceInstance(
                        serviceInstance.getInstanceId(),
                        serviceInstance.getServiceId(),
                        serviceInstance.getHost(),
                        Integer.parseInt(port),
                        serviceInstance.isSecure(),
                        serviceInstance.getMetadata()
                );
            }
        }
        return super.reconstructURI(serviceInstance, original);
    }
}
