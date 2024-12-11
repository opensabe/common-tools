package io.github.opensabe.common.elasticsearch.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import io.github.opensabe.common.elasticsearch.jfr.ElasticSearchClientObservationToJFRGenerator;
import io.github.opensabe.common.elasticsearch.observation.ElasticSearchClientConvention;
import io.github.opensabe.common.elasticsearch.observation.ElasticSearchClientObservationContext;
import io.github.opensabe.common.elasticsearch.observation.ElasticSearchClientObservationDocumentation;
import io.github.opensabe.common.elasticsearch.script.ScriptedSearcher;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.secret.FilterSecretStringResult;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(ElasticsearchRestClientAutoConfiguration.class)
public class ElasticSearchConfiguration implements DisposableBean {
    @Autowired
    private ElasticSearchProperties properties;
    @Autowired
    private GlobalSecretManager globalSecretManager;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    private RestHighLevelClient restHighLevelClient;

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

    @Bean
    public ElasticSearchClientObservationToJFRGenerator elasticSearchClientObservationToJFRGenerator() {
        return new ElasticSearchClientObservationToJFRGenerator();
    }


    @Bean
    public RestClientBuilder restClientBuilder () {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        List<HttpHost> httpHosts = Lists.newArrayList();
        for (String s : properties.getAddresses().split(",")) {
            String[] split = s.split(":");
            httpHosts.add(new HttpHost(split[0], Integer.parseInt(split[1]), properties.getSecure() ? "https" : "http"));
        }
        return RestClient.builder(httpHosts.toArray(new HttpHost[0]))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                            // Intercept request and modify the body
                            if (request instanceof HttpEntityEnclosingRequest) {
                                HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
                                HttpEntity entity = entityRequest.getEntity();
                                if (entity != null) {
                                    String originalBody = EntityUtils.toString(entity);
                                    FilterSecretStringResult filterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(originalBody);
                                    if (filterSecretStringResult.isFoundSensitiveString()) {
                                        throw new RuntimeException("Sensitive string found in ES request");
                                    }
                                }
                            }
                        })
                        .addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                            String uri = request.getRequestLine().getUri();
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
                        .addInterceptorLast((HttpResponseInterceptor) (response, context) -> {
                            ElasticSearchClientObservationContext observationContext = (ElasticSearchClientObservationContext) context.getAttribute("observationContext");
                            Observation observation = (Observation) context.getAttribute("observation");
                            observationContext.setResponse(response.toString());
                            observation.stop();
                            long counter = (long) context.getAttribute("counter");
                            CACHE.invalidate(counter);
                        })
                        .setKeepAliveStrategy((httpResponse, httpContext) -> Duration.ofSeconds(10).toMillis())
                        /* optionally perform some other configuration of httpClientBuilder here if needed */
                        .setDefaultIOReactorConfig(IOReactorConfig.custom()
                                /* optionally perform some other configuration of IOReactorConfig here if needed */
                                .setSoKeepAlive(true)
                                .build())
                );
    }

    @Bean
    public RestClient restClient (RestClientBuilder restClientBuilder) {
        return restClientBuilder.build();
    }
    @Bean
    public RestHighLevelClient getRestHighLevelClient(RestClientBuilder restClientBuilder) {
        restHighLevelClient = new RestHighLevelClient(restClientBuilder);
        return restHighLevelClient;
    }

    @Bean
    public ScriptedSearcher scriptedSearcher(RestHighLevelClient restHighLevelClient) {
        return new ScriptedSearcher(restHighLevelClient);
    }

    @Override
    public void destroy() {
        if (this.restHighLevelClient != null) {
            try {
                log.info("Closing Elasticsearch client");
                this.restHighLevelClient.close();
            } catch (final Exception ex) {
                log.error("Error closing Elasticsearch client: ", ex);
            }
        }
    }
}
