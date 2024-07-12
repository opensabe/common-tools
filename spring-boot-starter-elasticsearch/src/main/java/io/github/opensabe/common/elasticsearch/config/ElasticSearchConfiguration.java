package io.github.opensabe.common.elasticsearch.config;

import com.google.common.collect.Lists;
import io.github.opensabe.common.elasticsearch.script.ScriptedSearcher;
import io.github.opensabe.common.secret.FilterSecretStringResult;
import io.github.opensabe.common.secret.GlobalSecretManager;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
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

@Log4j2
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(ElasticsearchRestClientAutoConfiguration.class)
public class ElasticSearchConfiguration implements DisposableBean {
    @Autowired
    private ElasticSearchProperties properties;
    @Autowired
    private GlobalSecretManager globalSecretManager;

    private RestHighLevelClient restHighLevelClient;


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
