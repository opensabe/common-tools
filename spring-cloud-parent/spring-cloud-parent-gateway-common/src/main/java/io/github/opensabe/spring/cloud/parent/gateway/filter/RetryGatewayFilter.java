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
package io.github.opensabe.spring.cloud.parent.gateway.filter;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.github.opensabe.spring.cloud.parent.gateway.common.CommonFilterUtil;
import io.github.opensabe.spring.cloud.parent.gateway.config.GatewayRetryProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import reactor.core.publisher.Mono;

/**
 * 重试 Filter
 */
@Component
@EnableConfigurationProperties(GatewayRetryProperties.class)
public class RetryGatewayFilter extends AbstractTracedFilter {

    public static final int ORDER = RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER - 1;

    private final GatewayFilter gatewayFilterForRetryable;
    private final GatewayFilter gatewayFilterForNonRetryable;

    private final Set<String> retryablePathPatterns;
    private final LoadingCache<String, Boolean> isPathRetryableCache;

    @Autowired
    public RetryGatewayFilter(GatewayRetryProperties gatewayRetryProperties) {
        RetryGatewayFilterFactory.RetryConfig retryConfig = new RetryGatewayFilterFactory.RetryConfig();
        //这里不限制方法，通过 filter 中判断方法选择重试策略
        retryConfig.setMethods(HttpMethod.values());
        //重试2次，总共三次
        retryConfig.setRetries(2);
        //针对5xx重试，4xx不重试，因为请求面向用户，用户可能会有很多非法请求
        retryConfig.setSeries(
                HttpStatus.Series.SERVER_ERROR
        );
        retryConfig.setExceptions(IOException.class,
                TimeoutException.class,
                //针对Resilience4j的异常
                CallNotPermittedException.class
        );
        RetryGatewayFilterFactory.BackoffConfig backoffConfig = new RetryGatewayFilterFactory.BackoffConfig();
        backoffConfig.setBasedOnPreviousValue(true);
        backoffConfig.setFactor(2);
        backoffConfig.setFirstBackoff(Duration.of(100, ChronoUnit.MILLIS));
        gatewayFilterForRetryable = new RetryGatewayFilterFactory().apply(retryConfig);

        retryConfig = new RetryGatewayFilterFactory.RetryConfig();
        //这里不限制方法，通过 filter 中判断方法选择重试策略
        retryConfig.setMethods(HttpMethod.values());
        //重试2次，总共三次
        retryConfig.setRetries(2);
        //仅针对优雅关闭的 503 SERVICE_UNAVAILABLE 重试
        retryConfig.setStatuses(HttpStatus.SERVICE_UNAVAILABLE);
        //这个不能为空，所以用 1XX 这个不可能的系列
        retryConfig.setSeries(
                HttpStatus.Series.INFORMATIONAL
        );
        //与 GET 请求不同的是，Response Time out 我们不重试，因为请求已经发出了
        retryConfig.setExceptions(
                //链接超时
                io.netty.channel.ConnectTimeoutException.class,
                //各种连接异常
                java.net.ConnectException.class,
                //No route to host
                java.net.NoRouteToHostException.class,
                //针对Resilience4j的异常
                CallNotPermittedException.class
        );
        backoffConfig = new RetryGatewayFilterFactory.BackoffConfig();
        backoffConfig.setBasedOnPreviousValue(true);
        backoffConfig.setFactor(2);
        backoffConfig.setFirstBackoff(Duration.of(100, ChronoUnit.MILLIS));
        gatewayFilterForNonRetryable = new RetryGatewayFilterFactory().apply(retryConfig);
        Set<String> retryablePathPatterns = gatewayRetryProperties.getRetryablePathPatterns();
        this.retryablePathPatterns = retryablePathPatterns == null ? Set.of() : retryablePathPatterns;
        isPathRetryableCache = Caffeine.newBuilder().maximumSize(10000).build(k -> {
            boolean match = false;
            for (String retryablePathPattern : this.retryablePathPatterns) {
                boolean result = CommonFilterUtil.MATCHER.match(retryablePathPattern, k);
                if (result) {
                    match = true;
                    break;
                }
            }
            return match;
        });
    }

    @Override
    public Mono<Void> traced(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        String path = request.getPath().value();
        HttpMethod method = request.getMethod();
        boolean isRetryable = method == HttpMethod.GET || isPathRetryableCache.get(path);
        if (isRetryable) {
            return gatewayFilterForRetryable.filter(exchange, chain);
        } else {
            return gatewayFilterForNonRetryable.filter(exchange, chain);
        }
    }

    @Override
    public int ordered() {
        //必须在RouteToRequestUrlFilter还有LoadBalancerClientFilter之前
        return ORDER;
    }
}
