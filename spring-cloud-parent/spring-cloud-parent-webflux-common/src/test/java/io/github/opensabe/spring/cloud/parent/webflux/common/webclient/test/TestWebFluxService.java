package io.github.opensabe.spring.cloud.parent.webflux.common.webclient.test;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 测试可以正常创建一个微服务
 */
@Log4j2
@AutoConfigureObservability
@SpringBootTest(
        properties = {
                "webclient.jfr.enabled=false",
                "spring.server.jfr.enabled=false",
                "eureka.client.enabled=false",
        },
        classes = TestWebFluxService.TestConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class TestWebFluxService {
    @SpringBootApplication
    static class TestConfiguration {
        @Bean
        public TestService testService() {
            return new TestService();
        }
    }


    @RestController
    static class TestService {
        /**
         * @return
         * @see org.springframework.web.filter.OncePerRequestFilter
         * @see org.springframework.web.filter.ServerHttpObservationFilter
         */
        @GetMapping("/test")
        public String test() throws InterruptedException {
            TimeUnit.SECONDS.sleep(1);
            log.info("sleep complete");
            return Thread.currentThread().getName();
        }

        @GetMapping("/test-mono")
        public Mono<String> testMono() {
            Mono<String> result = Mono.delay(Duration.ofSeconds(1))
                    .flatMap(l -> {
                        log.info("sleep {} complete", l);
                        return Mono.just(Thread.currentThread().getName());
                    });
            log.info("method returned");
            return result;
        }

        @GetMapping("/test-deferred")
        public DeferredResult<String> testDeferred() {
            DeferredResult<String> result = new DeferredResult<>();
            CompletableFuture.runAsync(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("sleep complete");
                result.setResult(Thread.currentThread().getName());
            });
            log.info("method returned");
            return result;
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void test() {
        String thread1 = webTestClient.get().uri("/test").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).toString();
        String thread2 = webTestClient.get().uri("/test-mono").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).toString();
        String thread3 = webTestClient.get().uri("/test-deferred").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).toString();
    }
}
