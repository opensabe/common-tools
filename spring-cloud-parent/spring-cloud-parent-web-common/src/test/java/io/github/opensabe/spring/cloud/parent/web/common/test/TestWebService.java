package io.github.opensabe.spring.cloud.parent.web.common.test;

import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试可以正常创建一个微服务
 * 以及相关序列化与反序列化正常
 */
@Log4j2
@SpringJUnitConfig
@AutoConfigureObservability
@SpringBootTest(
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.openfeign.jfr.enabled=false",
                "spring.servlet.jfr.enabled=false"
        },
        classes = TestWebService.TestConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class TestWebService {
    @SpringBootApplication
    static class TestConfiguration {
        @Bean
        public TestService testService() {
            return new TestService();
        }

        @Bean
        public TestSecretProvider testSecretProvider(GlobalSecretManager globalSecretManager) {
            return new TestSecretProvider(globalSecretManager);
        }
    }

    @RestController
    static class TestService {
        /**
         * @see org.springframework.web.filter.OncePerRequestFilter
         * @see org.springframework.web.filter.ServerHttpObservationFilter
         * @return
         */
        @GetMapping("/test")
        public String test() throws InterruptedException {
            TimeUnit.SECONDS.sleep(1);
            log.info("sleep complete");
            return Thread.currentThread().getName();
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AllTypeObj {
            private String name;
            private int age;
            private Integer age0;
            private LocalDateTime time1;
            private Instant time2;
            private Date time3;
            private boolean flag;
            private Boolean flag0;
            private double money;
            private Double money0;
            private long count;
            private Long count0;
            private float weight;
            private Float weight0;
            private short height;
            private Short height0;
            private BigDecimal price;
        }

        @Data
        @NoArgsConstructor
        public static class AllTypeObjExtend extends AllTypeObj {
            private String extend;

            @Builder
            public AllTypeObjExtend(String name, int age, Integer age0, LocalDateTime time1, Instant time2, Date time3, boolean flag, Boolean flag0, double money, Double money0, long count, Long count0, float weight, Float weight0, short height, Short height0, BigDecimal price, String extend) {
                super(name, age, age0, time1, time2, time3, flag, flag0, money, money0, count, count0, weight, weight0, height, height0, price);
                this.extend = extend;
            }
        }

        public record AllTypeRecord(
                String name, int age, Integer age0,
                LocalDateTime time1, Instant time2, Date time3,
                boolean flag, Boolean flag0, double money,
                Double money0, long count, Long count0,
                float weight, Float weight0, short height,
                Short height0, BigDecimal price
        ) {
        }

        @PostMapping("/test-all-type")
        public AllTypeObj testAllType(@RequestBody AllTypeObj obj) {
            return obj;
        }

        @PostMapping("/test-all-type-record")
        public AllTypeRecord testAllTypeRecord(@RequestBody AllTypeRecord obj) {
            return obj;
        }

        @GetMapping("/local-data-time/serialize")
        public LocalDateTime testSerializeLocalDateTime () {
            return LocalDateTime.now();
        }
        @PostMapping ("/local-data-time/deserialize")
        public LocalDateTime testDeSerializeLocalDateTime (@RequestBody Lod time) {
            return time.time;
        }

        @GetMapping("/test-mono")
        public Mono<String> testMono() {
            Mono<String> result =  Mono.delay(Duration.ofSeconds(1))
                    .flatMap(l -> {
                        log.info("sleep {} complete", l);
                        return Mono.just(Thread.currentThread().getName());
                    });
            log.info("method returned");
            return result;
        }

        record Lod (LocalDateTime time) {

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

        @GetMapping("/test-secret-string")
        public String testSecretString() {
            return SECRET;
        }

        @GetMapping("/test-secret-obj")
        public Map<String, String> testSecretObj() {
            return Map.of("test", SECRET);
        }

        @GetMapping("/test-secret-obj-mono")
        public Mono<Map<String, String>> testSecretObjMono() {
            return Mono.delay(Duration.ofSeconds(1))
                    .flatMap(l -> {
                        log.info("sleep {} complete", l);
                        return Mono.just(Map.of("test", SECRET));
                    });
        }

        @GetMapping("/test-secret-obj-deferred")
        public DeferredResult<Map<String, String>> testSecretObjDeferred() {
            DeferredResult<Map<String, String>> result = new DeferredResult<>();
            CompletableFuture.runAsync(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("sleep complete");
                result.setResult(Map.of("test", SECRET));
            });
            log.info("method returned");
            return result;
        }

        @GetMapping("/test-secret-header-key")
        public Map testSecretHeaderKey(HttpServletResponse httpServletResponse) {
            httpServletResponse.addHeader(SECRET, "test");
            return Map.of();
        }

        @GetMapping("/test-secret-header-value")
        public Map testSecretHeaderValue(HttpServletResponse httpServletResponse) {
            httpServletResponse.addHeader("test", SECRET);
            return Map.of();
        }

        @GetMapping("/test-secret-header-value-mono")
        public Mono<Map> testSecretHeaderValueMono(HttpServletResponse httpServletResponse) {
            httpServletResponse.addHeader("test", SECRET);
            return Mono.delay(Duration.ofSeconds(1))
                    .flatMap(l -> {
                        log.info("sleep {} complete", l);
                        return Mono.just(Map.of());
                    });
        }
    }

    /**
     * 用于测试调用接口
     */
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Test
    public void testTestService() {
        //spring boot test 调用接口
        ResponseEntity<String> forEntity = testRestTemplate.getForEntity("/test", String.class);
        String threadName1 = forEntity.getBody();
        forEntity = testRestTemplate.getForEntity("/test-mono", String.class);
        String threadName2 = forEntity.getBody();
        forEntity = testRestTemplate.getForEntity("/test-deferred", String.class);
        String threadName3 = forEntity.getBody();
        //断言三次调用的线程不一样
        assertNotEquals(threadName1, threadName2);
        assertNotEquals(threadName1, threadName3);
        assertNotEquals(threadName2, threadName3);
    }

    @Test
    public void testAllType() {
        TestService.AllTypeObjExtend obj = TestService.AllTypeObjExtend.builder()
                .name("name")
                .age(1)
                .age0(2)
                //这里我们的序列化方式，会让时间戳精确到毫秒，不会有毫秒以下的时间
                //所以这里比较需要截断毫秒
                .time1(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
                .time2(Instant.now())
                .time3(new Date())
                .flag(true)
                .flag0(false)
                .money(1.1)
                .money0(2.2)
                .count(1)
                .count0(2L)
                .weight(1.1f)
                .weight0(2.2f)
                .height((short)1)
                .height0((short)2)
                .price(BigDecimal.ONE)
                //多余字段，判断不会有异常
                .extend("extend")
                .build();
        TestService.AllTypeObj result = testRestTemplate.postForObject("/test-all-type", obj, TestService.AllTypeObj.class);
        assertThat(result).extracting(
                TestService.AllTypeObj::getName,
                TestService.AllTypeObj::getAge,
                TestService.AllTypeObj::getAge0,
                TestService.AllTypeObj::getTime1,
                TestService.AllTypeObj::getTime2,
                TestService.AllTypeObj::getTime3,
                TestService.AllTypeObj::isFlag,
                TestService.AllTypeObj::getFlag0,
                TestService.AllTypeObj::getMoney,
                TestService.AllTypeObj::getMoney0,
                TestService.AllTypeObj::getCount,
                TestService.AllTypeObj::getCount0,
                TestService.AllTypeObj::getWeight,
                TestService.AllTypeObj::getWeight0,
                TestService.AllTypeObj::getHeight,
                TestService.AllTypeObj::getHeight0,
                TestService.AllTypeObj::getPrice
        ).containsExactly(
                obj.getName(),
                obj.getAge(),
                obj.getAge0(),
                obj.getTime1(),
                obj.getTime2(),
                obj.getTime3(),
                obj.isFlag(),
                obj.getFlag0(),
                obj.getMoney(),
                obj.getMoney0(),
                obj.getCount(),
                obj.getCount0(),
                obj.getWeight(),
                obj.getWeight0(),
                obj.getHeight(),
                obj.getHeight0(),
                obj.getPrice());
    }

    @Test
    public void testAllTypeRecord() {
        TestService.AllTypeRecord obj = new TestService.AllTypeRecord(
                "name",
                1,
                2,
                //这里我们的序列化方式，会让时间戳精确到毫秒，不会有毫秒以下的时间
                LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                Instant.now(),
                new Date(),
                true,
                false,
                1.1,
                2.2,
                1,
                2L,
                1.1f,
                2.2f,
                (short)1,
                (short)2,
                BigDecimal.ONE
        );
        TestService.AllTypeRecord result = testRestTemplate.postForObject("/test-all-type-record", obj, TestService.AllTypeRecord.class);
        assertEquals(obj, result);
    }

    @Test
    public void testSerializeLocalDataTime () {
        String str = testRestTemplate.getForObject("/local-data-time/serialize", String.class);
        assertTrue(str.matches("^\\d+$"));
    }
    @Test
    public void testDeSerializeLocalDataTime () {
        long now = System.currentTimeMillis();
        log.info("now: {}", now);
        Long str = testRestTemplate.postForObject("/local-data-time/deserialize",Map.of("time",now), Long.class);
        log.info("get: {}", str);
        assertEquals(now, str);
    }

    private static final String SECRET = "secretString";

    public static class TestSecretProvider extends SecretProvider {
        protected TestSecretProvider(GlobalSecretManager globalSecretManager) {
            super(globalSecretManager);
        }

        @Override
        protected String name() {
            return "testSecretProvider";
        }

        @Override
        protected long reloadTimeInterval() {
            return 1;
        }

        @Override
        protected TimeUnit reloadTimeIntervalUnit() {
            return TimeUnit.DAYS;
        }

        @Override
        protected Map<String, Set<String>> reload() {
            return Map.of(
                    "testSecretProviderKey", Set.of(SECRET)
            );
        }
    }

    @Test
    public void testSecret() throws InterruptedException {
        ResponseEntity<String> forEntity = testRestTemplate.getForEntity("/test-secret-string", String.class);
        assertEquals(forEntity.getStatusCode(), HttpStatus.OK);
        assertFalse(StringUtils.containsIgnoreCase(forEntity.getBody(), SECRET));
        assertFalse(StringUtils.containsIgnoreCase(forEntity.getHeaders().toString(), SECRET));

        forEntity = testRestTemplate.getForEntity("/test-secret-obj", String.class);
        assertEquals(forEntity.getStatusCode(), HttpStatus.OK);
        assertFalse(StringUtils.containsIgnoreCase(forEntity.getBody(), SECRET));
        assertFalse(StringUtils.containsIgnoreCase(forEntity.getHeaders().toString(), SECRET));

        forEntity = testRestTemplate.getForEntity("/test-secret-header-key", String.class);
        assertEquals(forEntity.getStatusCode(), HttpStatus.OK);
        assertFalse(StringUtils.containsIgnoreCase(forEntity.getBody(), SECRET));
        assertFalse(StringUtils.containsIgnoreCase(forEntity.getHeaders().toString(), SECRET));

        forEntity = testRestTemplate.getForEntity("/test-secret-header-value", String.class);
        assertEquals(forEntity.getStatusCode(), HttpStatus.OK);
        assertFalse(StringUtils.containsIgnoreCase(forEntity.getBody(), SECRET));
        assertFalse(StringUtils.containsIgnoreCase(forEntity.getHeaders().toString(), SECRET));
    }
}
