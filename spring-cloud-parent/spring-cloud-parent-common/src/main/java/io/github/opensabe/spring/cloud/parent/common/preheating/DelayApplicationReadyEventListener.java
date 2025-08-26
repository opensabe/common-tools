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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
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
 * 用于预热的
 * 延迟 ApplicationReadyEvent 完成
 */
@Log4j2
public class DelayApplicationReadyEventListener extends OnlyOnceApplicationListener<ApplicationReadyEvent> {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @Autowired
    private PreheatingProperties preheatingProperties;
    @Autowired
    private WebEndpointProperties webEndpointProperties;
    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    private LoadBalancerClientFactory clientFactory;
    @Value("${server.port}")
    private int port;

    @Override
    protected void onlyOnce(ApplicationReadyEvent event) {
        //每个spring-cloud应用只能初始化一次
        log.info("DelayApplicationReadyEventListener-onApplicationEvent: delay application ready start, should wait {}", preheatingProperties.getDelayReadyTime());
        String basePath = webEndpointProperties.getBasePath();
        String url = "http://127.0.0.1:" + port +
                (StringUtils.startsWith(basePath, "/") ? basePath : "/" + basePath)
                + "/health";
        int size = 50000;
        List<CompletableFuture<HttpResponse<String>>> futures = Lists.newArrayList();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        for (int i = 0; i < size; i++) {
            futures.add(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[size]))
                    .get(preheatingProperties.getDelayReadyTime().toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
        }

        //Preheat load balance client
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
