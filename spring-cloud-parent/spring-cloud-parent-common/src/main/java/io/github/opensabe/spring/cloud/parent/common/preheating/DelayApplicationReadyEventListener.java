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
package io.github.opensabe.spring.cloud.parent.common.preheating;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;

import com.google.common.collect.Lists;

import io.github.opensabe.spring.cloud.parent.common.config.OnlyOnceApplicationListener;
import lombok.extern.log4j.Log4j2;

/**
 * 应用就绪预热监听器。
 * <p>
 * 在 {@link ApplicationReadyEvent} 触发后，向本机 Health 端点并发发起大量异步请求以预热
 * HTTP 客户端与线程池，并预热各服务的 LoadBalancer 实例选择，从而延迟就绪信号的实际生效时间。
 */
@Log4j2
public class DelayApplicationReadyEventListener extends OnlyOnceApplicationListener<ApplicationReadyEvent> {

    /**
     * 并发 Health 检查请求数量。
     */
    private static final int PREHEAT_REQUEST_COUNT = 50000;

    /** @see PreheatingProperties */
    @Autowired
    private PreheatingProperties preheatingProperties;

    /** @see WebEndpointProperties */
    @Autowired
    private WebEndpointProperties webEndpointProperties;

    /** @see DiscoveryClient */
    @Autowired
    private DiscoveryClient discoveryClient;

    /** @see LoadBalancerClientFactory */
    @Autowired
    private LoadBalancerClientFactory clientFactory;

    /** 本机 HTTP 服务端口。 */
    @Value("${server.port}")
    private int port;

    /**
     * 执行一次性预热：并发 Health 请求与 LoadBalancer 实例选择。
     *
     * @param event 应用就绪事件
     */
    @Override
    protected void onlyOnce(ApplicationReadyEvent event) {
        log.info("DelayApplicationReadyEventListener-onApplicationEvent: delay application ready start, should wait {}", preheatingProperties.getDelayReadyTime());
        String basePath = webEndpointProperties.getBasePath();
        String url = "http://127.0.0.1:" + port +
                (StringUtils.startsWith(basePath, "/") ? basePath : "/" + basePath)
                + "/health";
        List<CompletableFuture<HttpResponse<String>>> futures = Lists.newArrayList();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        for (int i = 0; i < PREHEAT_REQUEST_COUNT; i++) {
            futures.add(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[PREHEAT_REQUEST_COUNT]))
                    .get(preheatingProperties.getDelayReadyTime().toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // 超时或部分失败不影响后续 LoadBalancer 预热
        }

        discoveryClient.getServices().forEach(service -> {
            log.info("DelayApplicationReadyEventListener-onApplicationEvent: preheat load balance client for service {}", service);
            ReactorLoadBalancer<ServiceInstance> loadBalancer = clientFactory.getInstance(service, ReactorServiceInstanceLoadBalancer.class);
            if (loadBalancer != null) {
                loadBalancer.choose(null).block();
            }
        });

        log.info("DelayApplicationReadyEventListener-onApplicationEvent: delay application ready end");
    }
}
