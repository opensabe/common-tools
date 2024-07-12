package io.github.opensabe.spring.cloud.parent.web.common.config;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.ThreadPoolBulkHeadDecorator;
import io.github.opensabe.spring.cloud.parent.web.common.feign.FeignBlockingLoadBalancerClientDelegate;
import io.github.opensabe.spring.cloud.parent.web.common.feign.FeignRequestCircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.web.common.feign.Resilience4jFeignClient;
import io.github.opensabe.spring.cloud.parent.web.common.jfr.FeignJFRProperties;
import io.github.opensabe.spring.cloud.parent.web.common.jfr.FeignObservationToJFRGenerator;
import feign.httpclient.ApacheHttpClient;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.FeignClientFactory;
import org.springframework.cloud.openfeign.FeignClientSpecification;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FeignJFRProperties.class)
public class CommonOpenFeignConfiguration implements BeanPostProcessor {

    /**
     * @see org.springframework.cloud.openfeign.FeignClientsRegistrar#registerDefaultConfiguration
     */
    public static final String DEFAULT_FEIGN_CLIENT_NAME_PREFIX = "default.";
    /**
     * @see org.springframework.cloud.openfeign.FeignClientsRegistrar#registerDefaultConfiguration
     */
    public static final String GLOBAL_DEFAULT = "default";

    /**
     * 开源化准备工作，不能写死 @EnableFeignClients 扫描路径，但是要实现添加我们框架中的默认配置
     * 监控{@link FeignClientFactory}, 如果configuration中包含default,就在default的配置类中追加
     * {@link DefaultOpenFeignConfiguration},如果不包含default,就创建一个包含{@link DefaultOpenFeignConfiguration}的configuration
     * @param bean the new bean instance
     * @param beanName the name of the bean
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FeignClientFactory feignClientFactory) {
            Map<String, FeignClientSpecification> configurations = feignClientFactory.getConfigurations();
            boolean hasDefault = false;
            //遍历所有配置
            for (FeignClientSpecification feignClientSpecification : configurations.values()) {
                //是否通过 @EnableFeignClients 指定了 defaultConfiguration
                //这里的代码参考 FeignClientsRegistrar 的 registerDefaultConfiguration 方法
                if (feignClientSpecification.getName().startsWith(DEFAULT_FEIGN_CLIENT_NAME_PREFIX)) {
                    //检查是否已经在 default 配置包含了我们框架中的 default 配置
                    if (ArrayUtils.contains(feignClientSpecification.getConfiguration(), DefaultOpenFeignConfiguration.class)) {
                        hasDefault = true;
                        break;
                    }
                    //如果没有就追加
                    feignClientSpecification.setConfiguration(ArrayUtils.add(feignClientSpecification.getConfiguration(), DefaultOpenFeignConfiguration.class));
                    hasDefault = true;
                }
            }
            //如果没有我们框架中的 default 配置，就创建并追加
            if (!hasDefault) {
                configurations.put(
                        DEFAULT_FEIGN_CLIENT_NAME_PREFIX + DefaultOpenFeignConfiguration.class.getSimpleName(),
                        new FeignClientSpecification(
                                DEFAULT_FEIGN_CLIENT_NAME_PREFIX + DefaultOpenFeignConfiguration.class.getSimpleName(),
                                GLOBAL_DEFAULT,
                                new Class[]{DefaultOpenFeignConfiguration.class}
                        )
                );
            }
            return feignClientFactory;
        }
        return bean;
    }

    @Bean
    public CircuitBreakerExtractor feignCircuitBreakerExtractor() {
        return new FeignRequestCircuitBreakerExtractor();
    }

    @Bean
    public HttpClient getHttpClient() {
        // 长连接保持5分钟
        PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(5, TimeUnit.MINUTES);
        // 总连接数
        pollingConnectionManager.setMaxTotal(1000);
        // 同路由的并发数
        pollingConnectionManager.setDefaultMaxPerRoute(1000);

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setConnectionManager(pollingConnectionManager);
        // 保持长连接配置，需要在头添加Keep-Alive
        httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        return httpClientBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApacheHttpClient internalApacheHttpClient(HttpClient httpClient) {
        return new ApacheHttpClient(httpClient);
    }

    /**
     *
     * @param apacheHttpClient
     * @param loadBalancerClientProvider 为何使用 ObjectProvider 请参考 FeignBlockingLoadBalancerClientDelegate 的注释
     * @param threadPoolBulkheadRegistry
     * @param circuitBreakerRegistry
     * @param loadBalancerClientFactory
     * @return FeignBlockingLoadBalancerClientDelegate 为何使用这个不直接用 FeignBlockingLoadBalancerClient 请参考 FeignBlockingLoadBalancerClientDelegate 的注释
     */
    @Bean
    @Primary
    public FeignBlockingLoadBalancerClientDelegate feignBlockingLoadBalancerCircuitBreakableClient(
            ApacheHttpClient apacheHttpClient,
            ObjectProvider<LoadBalancerClient> loadBalancerClientProvider,
            ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry,
            @Autowired(required = false) ThreadPoolBulkHeadDecorator threadPoolBulkHeadDecorators,
            CircuitBreakerRegistry circuitBreakerRegistry,
            LoadBalancerClientFactory loadBalancerClientFactory,
            List<LoadBalancerFeignRequestTransformer> transformers,
            UnifiedObservationFactory unifiedObservationFactory
    ) {
        return new FeignBlockingLoadBalancerClientDelegate(
                new Resilience4jFeignClient(
                        apacheHttpClient,
                        threadPoolBulkheadRegistry,
                        threadPoolBulkHeadDecorators,
                        circuitBreakerRegistry,
                        unifiedObservationFactory
                ),
                loadBalancerClientProvider,
                loadBalancerClientFactory,
                transformers,
                unifiedObservationFactory
        );
    }

    @Bean
    public FeignObservationToJFRGenerator feignObservationToJFRGenerator(FeignJFRProperties feignJFRProperties) {
        return new FeignObservationToJFRGenerator(feignJFRProperties);
    }
}