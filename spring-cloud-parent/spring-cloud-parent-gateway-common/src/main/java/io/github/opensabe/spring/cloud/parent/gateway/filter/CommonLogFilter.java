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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.opensabe.base.RespUtil;
import io.github.opensabe.common.utils.AlarmUtil;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.cloud.parent.gateway.common.CommonFilterUtil;
import io.github.opensabe.spring.cloud.parent.gateway.config.GatewayLogProperties;
import io.github.opensabe.spring.cloud.parent.webflux.common.TracedPublisherFactory;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * 打印日志的 Filter
 */
@Log4j2
@Component
@EnableConfigurationProperties(GatewayLogProperties.class)
public class CommonLogFilter extends AbstractTracedFilter {

    public static final int ORDER = TraceIdFilter.ORDER + 1;
    public static final String START_TIME = "START-TIME";

    /**
     * 可以打印日志的 content 类型
     */
    private static final List<MediaType> LEGAL_LOG_MEDIA_TYPES = List.of(
            MediaType.TEXT_XML,
            MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON,
            MediaType.TEXT_PLAIN,
            MediaType.TEXT_XML
    );
    private static final Pattern COMPILE = Pattern.compile("\\p{C}");

    @Autowired
    private TracedPublisherFactory tracedPublisherFactory;
    @Autowired
    private GatewayLogProperties gatewayLogProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> traced(ServerWebExchange exchange, GatewayFilterChain chain) {
        Observation observation = TraceIdFilter.getObservation(exchange);
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        HttpHeaders headers = request.getHeaders();
        MediaType contentType = headers.getContentType();
        RequestPath path = request.getPath();
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        HttpMethod method = request.getMethod();
        log.info("{} -> {}: header: {}; queryParams: {}", method, path, headers.toString(),
                queryParams.entrySet().stream().map(e -> e.getKey() + ':' + e.getValue()).collect(Collectors.joining(",", "[", "]"))
        );

        Mono<Void> validate = validate(exchange);
        if (validate != null) {
            return validate;
        }

        Flux<DataBuffer> dataBufferFlux = tracedPublisherFactory.getTracedFlux(request.getBody(), observation).map(dataBuffer -> {
            if (log.isDebugEnabled()) {
                if (LEGAL_LOG_MEDIA_TYPES.contains(contentType)) {
                    try {
                        String s = CommonFilterUtil.dataBufferToString(dataBuffer);
                        log.debug("body: {}", COMPILE.matcher(s).replaceAll(""));
                        dataBuffer = dataBufferFactory.wrap(s.getBytes(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        log.error("error read body: {}", e.getMessage(), e);
                    }
                } else {
                    log.debug("body: {}", contentType);
                }
            }
            return dataBuffer;
        });
        return chain.filter(exchange.mutate().request(new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                return dataBufferFlux;
            }
        }).response(new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    HttpHeaders responseHeaders = super.getHeaders();
                    long elapsed = System.currentTimeMillis() - (long) exchange.getAttributes().get(START_TIME);
                    long threshold = getSlowCallThreshold(path.value());
                    if (elapsed < threshold) {
                        log.info("response: {} -> {} {} header: {}, time: {}ms", method, path, getStatusCode(), responseHeaders.toString(), elapsed);
                    } else {
                        //报警有相似度聚合算法，为了防止报警聚合错误（将不同的 path 聚合在一起），将 path 多输出几遍
                        log.error("response: {} ->  {} {} {} {} {} {} {} header: {}, time: {}ms", method, path, path, path, path, path, path, getStatusCode(), JsonUtil.toJSONString(responseHeaders), elapsed);
                    }
                    final MediaType contentType = responseHeaders.getContentType();
                    if (contentType != null && body instanceof Flux && LEGAL_LOG_MEDIA_TYPES.contains(contentType) && log.isDebugEnabled()) {
                        //有TCP粘包拆包问题，这个body是多次写入的，一次调用拿不到完整的body，所以这里转换成fluxBody利用其中的buffer来接受完整的body
                        Flux<? extends DataBuffer> fluxBody = tracedPublisherFactory.getTracedFlux(Flux.from(body), observation);
                        return super.writeWith(
                                fluxBody.buffer().map(buffers -> {
                                    try {
                                        var buffer = dataBufferFactory.join(buffers);
                                        String s = CommonFilterUtil.dataBufferToString(buffer);
                                        log.debug("response: body: {}", s);
                                        return dataBufferFactory.wrap(s.getBytes(StandardCharsets.UTF_8));
                                    } catch (Exception e) {
                                        log.error("error while encrypt response: {}", e.getMessage(), e);
                                    }
                                    return null;
                                })
                        );
                    }
                    // if body is not a flux. never got there.
                    return super.writeWith(body);
            }
        }).build());
    }

    private final Cache<String, Long> thresholdCache = Caffeine.newBuilder()
            .maximumSize(100000)
            .expireAfterWrite(Duration.ofHours(1))
            .build();

    private long getSlowCallThreshold(String path) {
        return thresholdCache.get(path, k -> {
            List<GatewayLogProperties.SlowLog> slowLogs = gatewayLogProperties.getSlowLogs();
            if (CollectionUtils.isNotEmpty(slowLogs)) {
                Optional<Long> first = slowLogs.stream().filter(slowLog -> {
                    String pattern = slowLog.getPattern();
                    return antPathMatcher.match(pattern, path);
                }).map(GatewayLogProperties.SlowLog::getThreshold).findFirst();
                if (first.isPresent()) {
                    return first.get();
                }
            }
            return Long.MAX_VALUE;
        });
    }

    private final Cache<String, List<GatewayLogProperties.ParamCheck>> paramCheckCache = Caffeine.newBuilder()
            .maximumSize(100000)
            .expireAfterWrite(Duration.ofHours(1))
            .build();

    private List<GatewayLogProperties.ParamCheck> getParamCheckList(String path) {
        return paramCheckCache.get(path, k -> {
            List<GatewayLogProperties.ParamCheck> paramChecks = gatewayLogProperties.getParamChecks();
            if (CollectionUtils.isNotEmpty(paramChecks)) {
                return paramChecks.stream().filter(slowLog -> {
                    String pattern = slowLog.getPattern();
                    return antPathMatcher.match(pattern, path);
                }).collect(Collectors.toList());
            }
            return List.of();
        });
    }

    private Mono<Void> validate(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        HttpHeaders headers = request.getHeaders();
        RequestPath path = request.getPath();
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        List<String> platforms = headers.get("platform");
        if (CollectionUtils.isNotEmpty(platforms)) {
            String platform = platforms.getFirst();
            List<GatewayLogProperties.ParamCheck> paramCheckList = getParamCheckList(path.value());
            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                String param = entry.getKey();
                List<String> value = entry.getValue();
                GatewayLogProperties.Operation operation = null;
                String message = null;
                if (CollectionUtils.isNotEmpty(value)) {
                    Optional<GatewayLogProperties.ParamCheck> paramCheckOptional = paramCheckList.stream()
                            .filter(paramCheck -> paramCheck.getParams().contains(param))
                            .filter(paramCheck -> new HashSet<>(paramCheck.getInvalidParamValues()).containsAll(value))
                            .findFirst();
                    if (paramCheckOptional.isPresent()) {
                        GatewayLogProperties.ParamCheck paramCheck = paramCheckOptional.get();
                        log.debug("CommonLogFilter-getQueryParams: paramCheck match: {}", paramCheck::toString);
                        Optional<GatewayLogProperties.SpecificOperation> specificOperationOptional = paramCheck.getSpecificOperations().stream()
                                .filter(specificOperation -> {
                                    if (StringUtils.equalsIgnoreCase(specificOperation.getPlatform(), platform)) {
                                        if (CollectionUtils.isNotEmpty(specificOperation.getVersions())) {
                                            List<String> appversion = headers.get("appversion");
                                            if (CollectionUtils.isNotEmpty(appversion)) {
                                                return specificOperation.getVersions().containsAll(appversion);
                                            }
                                        } else {
                                            return true;
                                        }
                                    }
                                    return false;
                                }).findFirst();
                        if (specificOperationOptional.isPresent()) {
                            GatewayLogProperties.SpecificOperation specificOperation = specificOperationOptional.get();
                            log.debug("CommonLogFilter-getQueryParams: specificOperation match: {}", specificOperation::toString);
                            operation = specificOperation.getOperation();
                            message = paramCheck.getPattern() + ":" + paramCheck.getInvalidParamValues() + ", "
                                    + specificOperation.getPlatform() + ":"
                                    + JsonUtil.toJSONString(specificOperation.getVersions());
                        } else {
                            operation = paramCheck.getDefaultOperation();
                            message = paramCheck.getPattern() + ":" + paramCheck.getInvalidParamValues();
                        }
                    }
                }
                if (operation != null) {
                    switch (operation) {
                        case INVALID:
                            log.info("CommonLogFilter-getQueryParams: queryParam: {}, {} is invalid because of validation: {}", param, value, message);
                            return CommonFilterUtil.errorResponse(response, HttpStatus.BAD_REQUEST, RespUtil.invalid("invalid queryParam: " + param + ", value: " + value), dataBufferFactory);
                        case IGNORE:
                            break;
                        case ALARM:
                            AlarmUtil.fatal("CommonLogFilter-getQueryParams: found queryParam should be alarmed: {}, {} in validation: {}, platform: {}, appversion: {}", param, value, message, platform, headers.get("appversion"));
                            break;
                        default:
                            AlarmUtil.fatal("CommonLogFilter-getQueryParams: invalid operation: {}", operation);
                            break;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int ordered() {
        return ORDER;
    }
}