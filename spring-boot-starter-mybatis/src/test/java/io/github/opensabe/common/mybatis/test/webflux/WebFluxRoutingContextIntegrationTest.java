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
package io.github.opensabe.common.mybatis.test.webflux;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * 验证 {@link io.github.opensabe.common.mybatis.configuration.WebInterceptorConfiguration} 中
 * {@code chain.filter(ex).contextWrite(operId) + transformDeferredContextual + doOnEach} 在请求线程及
 * {@code subscribeOn} 调度线程上恢复 {@link io.github.opensabe.common.mybatis.webflux.WebFluxRoutingContext}，
 * 使 {@link io.github.opensabe.common.mybatis.interceptor.WebfluxDataSourceSwitchInterceptor#resolutionProbeOperIdFromBoundContext()}
 * 能读到与请求头 operId 一致的值。
 */
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = WebFluxRoutingContextTestApplication.class,
        properties = {
                "spring.main.web-application-type=reactive"
        }
)
@AutoConfigureWebTestClient
@DisplayName("WebFluxRoutingContext 与 operId 过滤链集成")
public class WebFluxRoutingContextIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("subscribeOn(boundedElastic) 时 WebFluxRoutingContext 已含 operId，可解析为期望 header 值")
    public void whenSubscribeOnBoundedElastic_resolvedOperIdMatchesHeader() {
        var expected = "itOperIdBounded";
        webTestClient.get()
                .uri("/internal/probe/oper/bounded-elastic")
                .header("operId", expected)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("subscribeOn(immediate) 时同样可解析 operId")
    public void whenSubscribeOnImmediate_resolvedOperIdMatchesHeader() {
        var expected = "itOperIdImmediate";
        webTestClient.get()
                .uri("/internal/probe/oper/immediate")
                .header("operId", expected)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("多线程并发请求：各 operId 在 subscribeOn 工作线程上解析互不串号")
    public void concurrentRequests_eachResolvedOperIdMatchesItsOwnHeader() {
        int threadPoolSize = 16;
        int roundsPerWorker = 4;
        int total = threadPoolSize * roundsPerWorker;
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        try {
            CompletableFuture<?>[] futures = IntStream.range(0, total)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        String uri = (i % 2 == 0)
                                ? "/internal/probe/oper/bounded-elastic"
                                : "/internal/probe/oper/immediate";
                        String expected = "mt-concurrent-" + i;
                        String body = webTestClient.get()
                                .uri(uri)
                                .header("operId", expected)
                                .exchange()
                                .expectStatus()
                                .isOk()
                                .expectBody(String.class)
                                .returnResult()
                                .getResponseBody();
                        Assertions.assertEquals(
                                expected,
                                body,
                                "resolved operId must match this request only (i=" + i + ", uri=" + uri + ")");
                    }, executor))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();
        } finally {
            executor.shutdown();
        }
    }
}
