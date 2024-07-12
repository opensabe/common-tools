package org.springframework.cloud.openfeign.loadbalancer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.common.loadbalancer.TracedCircuitBreakerRoundRobinLoadBalancer;
import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycle;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycleValidator;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.ResponseData;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.cloud.openfeign.loadbalancer.LoadBalancerUtils.executeWithLoadBalancerLifecycleProcessing;

/**
 * FeignBlockingLoadBalancerClient 的改写，将获取每个实例的断路器数据需要的信息填充到 lb 请求
 * 然后在负载均衡器进行获取从而拿到实例的负载均衡请求
 * @see FeignBlockingLoadBalancerClient
 */
@Log4j2
public class FeignBlockingLoadBalancerClientExtend implements Client {
	private final Client delegate;

	private final LoadBalancerClient loadBalancerClient;

	private final LoadBalancerClientFactory loadBalancerClientFactory;

	private final List<LoadBalancerFeignRequestTransformer> transformers;

	private final UnifiedObservationFactory unifiedObservationFactory;


	public FeignBlockingLoadBalancerClientExtend(Client delegate, LoadBalancerClient loadBalancerClient,
												 LoadBalancerClientFactory loadBalancerClientFactory,
												 List<LoadBalancerFeignRequestTransformer> transformers,
												 UnifiedObservationFactory unifiedObservationFactory) {
		this.delegate = delegate;
		this.loadBalancerClient = loadBalancerClient;
		this.loadBalancerClientFactory = loadBalancerClientFactory;
		this.transformers = transformers;
		this.unifiedObservationFactory = unifiedObservationFactory;
	}

	/**
	 * 不能使用并发不安全的 WeakHashMap
	 * 需要使用 weakKeys，因为这里不确定何时 Request 会结束，需要保证不影响 Request 的垃圾回收
	 * value 跟随 key 回收，value 不能使用 WeakReference
	 * 但是需要注意 value 中不能有 key 的引用，否则会相当于有强引用引用了 key 导致 weakKey 没有应有的效果
	 */
	private final Cache<RequestTemplate, DefaultRequest> requestRequestDataContextMap =
			Caffeine.newBuilder().weakKeys().weakValues()
					//这里设置 1 小时，如果有因为超过时间过期的证明可能有内存泄漏
					.expireAfterAccess(Duration.ofHours(1))
					.removalListener((RequestTemplate key, DefaultRequest value, RemovalCause cause) -> {
						if (cause == RemovalCause.EXPIRED) {
							log.warn("RequestTemplate {} was evicted from requestRequestDataContextMap, " +
											"value: {}, maybe memory leak",
									key, cause, value);
						}
					})
					.build();

	@Override
	public Response execute(Request request, Request.Options options) throws IOException {
		final URI originalUri = URI.create(request.url());
		//对于同一个请求，requestTemplate 是保持一致的
		RequestTemplate requestTemplate = request.requestTemplate();
		String serviceId = originalUri.getHost();
		Assert.state(serviceId != null, "Request URI does not contain a valid hostname: " + originalUri);
		String hint = getHint(serviceId);
		//需要复用 RequestDataContext，因为重试我们做在了外层，外层重试对于 Request 是同一个
		//对于重试 我们不能每次重新生成新的 RequestDataContext
		DefaultRequest<RequestDataContext> lbRequest =
				requestRequestDataContextMap.get(requestTemplate, k -> {
					return new DefaultRequest<>(
							new RequestDataContext(
									buildRequestData(request), getHint(serviceId)
							)
					);
				});


		Set<LoadBalancerLifecycle> supportedLifecycleProcessors = LoadBalancerLifecycleValidator
				.getSupportedLifecycleProcessors(
						loadBalancerClientFactory.getInstances(serviceId, LoadBalancerLifecycle.class),
						RequestDataContext.class, ResponseData.class, ServiceInstance.class);
		supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStart(lbRequest));
		ServiceInstance instance = loadBalancerClient.choose(serviceId, lbRequest);
		org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse = new DefaultResponse(
				instance);
		if (instance == null) {
			String message = "Load balancer does not contain an instance for the service " + serviceId;
			if (log.isWarnEnabled()) {
				log.warn(message);
			}
			supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
					.onComplete(new CompletionContext<ResponseData, ServiceInstance, RequestDataContext>(
							CompletionContext.Status.DISCARD, lbRequest, lbResponse)));
			return Response.builder().request(request).status(HttpStatus.SERVICE_UNAVAILABLE.value())
					.body(message, StandardCharsets.UTF_8).build();
		}
		String reconstructedUrl = loadBalancerClient.reconstructURI(instance, originalUri).toString();
		Request newRequest = buildRequest(request, reconstructedUrl, instance);
		return executeWithLoadBalancerLifecycleProcessing(delegate, options, newRequest, lbRequest, lbResponse,
				supportedLifecycleProcessors);
	}

	/**
	 * 修改的就是这里，原来这个方法是在 LoadBalancerUtils 里面
	 * 这里就是将 Request 的额外信息放进去
	 * @param request
	 * @return
	 */
	private RequestData buildRequestData(Request request) {
		//我们这里并不是想启动新的 Observation，只是想复用老的，如果老的不存在，其实是有问题的
		Observation createObservation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
		HttpHeaders requestHeaders = new HttpHeaders();
		request.headers().forEach((key, value) -> requestHeaders.put(key, new ArrayList<>(value)));
		return new RequestData(HttpMethod.valueOf(request.httpMethod().name()), URI.create(request.url()),
				requestHeaders, null,
				Map.of(
						REQUEST_TEMPLATE,
						//注意这里一定要复制一个新对象，否则会导致 requestRequestDataContextMap 的 key 无法回收，因为 value 强引用了 key
						RequestTemplate.from(request.requestTemplate()),
						TracedCircuitBreakerRoundRobinLoadBalancer.OBSERVATION_KEY,
						createObservation
				)
		);
	}

	public static final String REQUEST_TEMPLATE = "request_template";


	protected Request buildRequest(Request request, String reconstructedUrl) {
		return Request.create(request.httpMethod(), reconstructedUrl, request.headers(), request.body(),
				request.charset(), request.requestTemplate());
	}

	protected Request buildRequest(Request request, String reconstructedUrl, ServiceInstance instance) {
		Request newRequest = buildRequest(request, reconstructedUrl);
		if (transformers != null) {
			for (LoadBalancerFeignRequestTransformer transformer : transformers) {
				newRequest = transformer.transformRequest(newRequest, instance);
			}
		}
		return newRequest;
	}

	// Visible for Sleuth instrumentation
	public Client getDelegate() {
		return delegate;
	}

	private String getHint(String serviceId) {
		LoadBalancerProperties properties = loadBalancerClientFactory.getProperties(serviceId);
		String defaultHint = properties.getHint().getOrDefault("default", "default");
		String hintPropertyValue = properties.getHint().get(serviceId);
		return hintPropertyValue != null ? hintPropertyValue : defaultHint;
	}
}
