package io.github.opensabe.spring.cloud.parent.web.common.feign;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import feign.Client;
import feign.Request;
import feign.Response;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClientExtend;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;

import java.io.IOException;
import java.util.List;

/**
 * 由于初始化 FeignBlockingLoadBalancerClient 需要 LoadBalancerClient
 * 但是由于 Spring Cloud 2020 之后，Spring Cloud LoadBalancer BlockingClient 的加载，强制加入了顺序
 * @see org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration
 * 这个自动配置加入了 @AutoConfigureAfter(LoadBalancerAutoConfiguration.class)
 * 导致我们在初始化的 FeignClient 的时候，无法拿到 BlockingClient
 * 所以，需要通过 ObjectProvider 封装 LoadBalancerClient，在真正调用 FeignClient 的时候通过 ObjectProvider 拿到 LoadBalancerClient 来创建 FeignBlockingLoadBalancerClient
 */
public class FeignBlockingLoadBalancerClientDelegate implements Client {
    private volatile FeignBlockingLoadBalancerClientExtend feignBlockingLoadBalancerClient;

    private final Client delegate;
    private final ObjectProvider<LoadBalancerClient> loadBalancerClientObjectProvider;
    private final LoadBalancerClientFactory loadBalancerClientFactory;
    private final List<LoadBalancerFeignRequestTransformer> transformers;
    private final UnifiedObservationFactory unifiedObservationFactory;

    public FeignBlockingLoadBalancerClientDelegate(
            Client delegate,
            ObjectProvider<LoadBalancerClient> loadBalancerClientObjectProvider,
            LoadBalancerClientFactory loadBalancerClientFactory,
            List<LoadBalancerFeignRequestTransformer> transformers,
            UnifiedObservationFactory unifiedObservationFactory
    ) {
        this.delegate = delegate;
        this.loadBalancerClientObjectProvider = loadBalancerClientObjectProvider;
        this.loadBalancerClientFactory = loadBalancerClientFactory;
        this.transformers = transformers;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        if (feignBlockingLoadBalancerClient == null) {
            synchronized (this) {
                if (feignBlockingLoadBalancerClient == null) {
                    feignBlockingLoadBalancerClient = new FeignBlockingLoadBalancerClientExtend(
                            this.delegate,
                            this.loadBalancerClientObjectProvider.getIfAvailable(),
                            this.loadBalancerClientFactory,
                            this.transformers,
                            this.unifiedObservationFactory
                    );
                }
            }
        }
        return feignBlockingLoadBalancerClient.execute(request, options);
    }
}