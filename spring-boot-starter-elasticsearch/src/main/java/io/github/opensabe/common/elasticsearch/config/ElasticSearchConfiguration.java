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
package io.github.opensabe.common.elasticsearch.config;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.elasticsearch.autoconfigure.ElasticsearchRestClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.Jackson3JsonpMapper;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder;
import io.github.opensabe.common.elasticsearch.jfr.ElasticSearchClientObservationToJFRGenerator;
import io.github.opensabe.common.elasticsearch.observation.ElasticSearchClientConvention;
import io.github.opensabe.common.elasticsearch.observation.ElasticSearchClientObservationContext;
import io.github.opensabe.common.elasticsearch.observation.ElasticSearchClientObservationDocumentation;
import io.github.opensabe.common.elasticsearch.script.ScriptedSearcher;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;

/**
 * Elasticsearch Java API Client 与 Rest5 传输层的 Spring 配置。
 * <p>
 * 通过 HC5 拦截器关联 Micrometer Observation，并用 Caffeine 缓存兜底未正常 stop 的 observation；
 * 传输层挂载 {@link SecretFilteringInstrumentation} 在 HTTP 发出前过滤敏感串。
 * </p>
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(ElasticsearchRestClientAutoConfiguration.class)
public class ElasticSearchConfiguration implements DisposableBean {

    /**
     * 进行中的 ES 请求 observation 缓存；5 分钟过期，防止异常路径未 stop 导致泄漏。
     */
    private static final Cache<Long, Observation> CACHE = Caffeine.newBuilder()
            .weakKeys()
            .weakValues()
            .expireAfterWrite(Duration.ofMinutes(5))
            .evictionListener((key, value, cause) -> {
                if (cause.wasEvicted()) {
                    if (value instanceof Observation) {
                        ((Observation) value).stop();
                    }
                }
            })
            .build();

    /**
     * 单调递增的请求计数器，作为 observation 在 {@link #CACHE} 中的键。
     */
    private static final AtomicLong COUNTER = new AtomicLong(0);

    /**
     * ES 连接与集群属性。
     */
    @Autowired
    private ElasticSearchProperties properties;

    /**
     * 全局敏感串过滤管理器。
     */
    @Autowired
    private GlobalSecretManager globalSecretManager;

    /**
     * 统一 Observation 工厂。
     */
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    /**
     * 容器关闭时需显式 close 的客户端引用。
     */
    private ElasticsearchClient elasticsearchClient;

    /**
     * 注册 ES 客户端 Observation → JFR 事件桥接器。
     *
     * @return JFR 生成器 Bean
     */
    @Bean
    public ElasticSearchClientObservationToJFRGenerator elasticSearchClientObservationToJFRGenerator() {
        return new ElasticSearchClientObservationToJFRGenerator();
    }

    /**
     * 构建 Rest5 低层客户端 Builder，配置 observation 拦截器与 keep-alive。
     *
     * @return 未 build 的 {@link Rest5ClientBuilder}
     */
    @Bean
    public Rest5ClientBuilder rest5ClientBuilderForElasticSearch() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        List<HttpHost> httpHosts = Lists.newArrayList();
        for (String s : properties.getAddresses().split(",")) {
            String[] split = s.split(":");
            httpHosts.add(new HttpHost(properties.getSecure() ? "https" : "http", split[0], Integer.parseInt(split[1])));
        }
        return Rest5Client.builder(httpHosts.toArray(new HttpHost[0]))
                .setHttpClientConfigCallback(httpClientBuilder -> configureHttpClient(httpClientBuilder));
    }

    /**
     * 为异步 HTTP 客户端添加请求/响应 observation 拦截器。
     *
     * @param httpClientBuilder HC5 异步客户端构建器
     * @return 配置后的构建器
     */
    private HttpAsyncClientBuilder configureHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        return httpClientBuilder
                .addRequestInterceptorFirst((HttpRequestInterceptor) (request, entityDetails, context) -> {
                    String uri = request.getRequestUri();
                    String params = "";
                    if (uri.contains("?")) {
                        String[] split = uri.split("\\?");
                        uri = split[0];
                        params = split[1];
                    }
                    ElasticSearchClientObservationContext observationContext = new ElasticSearchClientObservationContext(uri, params);
                    Observation observation = ElasticSearchClientObservationDocumentation.CLIENT_REQUEST.start(
                            null,
                            ElasticSearchClientConvention.DEFAULT,
                            () -> observationContext,
                            unifiedObservationFactory.getObservationRegistry()
                    );
                    context.setAttribute("observation", observation);
                    context.setAttribute("observationContext", observationContext);
                    long incrementAndGet = COUNTER.incrementAndGet();
                    context.setAttribute("counter", incrementAndGet);
                    CACHE.put(incrementAndGet, observation);
                })
                .addResponseInterceptorLast((HttpResponseInterceptor) (response, entityDetails, context) -> {
                    // Fail paths with no response rely on CACHE evictionListener to stop();
                    // secret filter runs in transport before HTTP, so it never inserts into CACHE.
                    ElasticSearchClientObservationContext observationContext =
                            (ElasticSearchClientObservationContext) context.getAttribute("observationContext");
                    Observation observation = (Observation) context.getAttribute("observation");
                    Object counterAttr = context.getAttribute("counter");
                    if (observationContext != null) {
                        observationContext.setResponse(response.toString());
                    }
                    if (observation != null) {
                        observation.stop();
                    }
                    if (counterAttr instanceof Long counter) {
                        CACHE.invalidate(counter);
                    }
                })
                .setKeepAliveStrategy((httpResponse, httpContext) -> TimeValue.ofSeconds(10));
    }

    /**
     * 创建 Rest5 低层客户端实例。
     *
     * @param rest5ClientBuilder 已配置的 builder
     * @return {@link Rest5Client}
     */
    @Bean
    public Rest5Client rest5ClientForElasticSearch(Rest5ClientBuilder rest5ClientBuilder) {
        return rest5ClientBuilder.build();
    }

    /**
     * 创建带 Jackson 3 映射与密钥过滤 instrumentation 的 {@link ElasticsearchClient}。
     *
     * @param rest5Client Rest5 低层客户端
     * @return 高级 ES 客户端
     */
    @Bean
    public ElasticsearchClient elasticsearchClientForElasticSearch(Rest5Client rest5Client) {
        Rest5ClientTransport transport = new Rest5ClientTransport(
                rest5Client,
                new Jackson3JsonpMapper(),
                null,
                new SecretFilteringInstrumentation(globalSecretManager));
        elasticsearchClient = new ElasticsearchClient(transport);
        return elasticsearchClient;
    }

    /**
     * 注册脚本化搜索辅助 Bean。
     *
     * @param elasticsearchClient ES 客户端
     * @return {@link ScriptedSearcher}
     */
    @Bean
    public ScriptedSearcher scriptedSearcherForElasticSearch(ElasticsearchClient elasticsearchClient) {
        return new ScriptedSearcher(elasticsearchClient);
    }

    /**
     * 容器销毁时关闭 ES 客户端。
     */
    @Override
    public void destroy() {
        if (this.elasticsearchClient != null) {
            try {
                log.info("Closing Elasticsearch client");
                this.elasticsearchClient.close();
            } catch (final Exception ex) {
                log.error("Error closing Elasticsearch client: ", ex);
            }
        }
    }
}
