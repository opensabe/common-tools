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

@Log4j2
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(ElasticsearchRestClientAutoConfiguration.class)
public class ElasticSearchConfiguration implements DisposableBean {
    private static final Cache<Long, Observation> CACHE = Caffeine.newBuilder()
            .weakKeys()
            .weakValues()
            //最多5分钟，防止 ES 异常没有捕获，导致 Observation 一直不 stop
            .expireAfterWrite(Duration.ofMinutes(5))
            .evictionListener((key, value, cause) -> {
                if (cause.wasEvicted()) {
                    if (value instanceof Observation) {
                        ((Observation) value).stop();
                    }
                }
            })
            .build();
    private static final AtomicLong COUNTER = new AtomicLong(0);
    @Autowired
    private ElasticSearchProperties properties;
    @Autowired
    private GlobalSecretManager globalSecretManager;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;
    private ElasticsearchClient elasticsearchClient;

    @Bean
    public ElasticSearchClientObservationToJFRGenerator elasticSearchClientObservationToJFRGenerator() {
        return new ElasticSearchClientObservationToJFRGenerator();
    }

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

    @Bean
    public Rest5Client rest5ClientForElasticSearch(Rest5ClientBuilder rest5ClientBuilder) {
        return rest5ClientBuilder.build();
    }

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

    @Bean
    public ScriptedSearcher scriptedSearcherForElasticSearch(ElasticsearchClient elasticsearchClient) {
        return new ScriptedSearcher(elasticsearchClient);
    }

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
