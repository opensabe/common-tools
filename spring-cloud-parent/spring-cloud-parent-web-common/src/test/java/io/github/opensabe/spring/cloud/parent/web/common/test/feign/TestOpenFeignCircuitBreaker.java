package io.github.opensabe.spring.cloud.parent.web.common.test.feign;

import io.github.opensabe.spring.cloud.parent.web.common.test.CommonMicroServiceTest;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;


@ActiveProfiles("circuitbreaker")
@SpringBootTest
@AutoConfigureObservability
@EnableFeignClients
public class TestOpenFeignCircuitBreaker extends CommonMicroServiceTest {
    static final String TEST_SERVICE_CIRCUITBREAKER = "testServiceCircuitbreaker";
    static final String CONTEXT_ID_CIRCUITBREAKER = "testServiceCircuitbreakerClient";
    static final int TEST_SERVICE_CIRCUITBREAKER_FAILURE_RATE_THRESHOLD = 30;
    static final int TEST_SERVICE_CIRCUITBREAKER_MINIMUM_NUMBER_OF_CALLS = 10;

    @SpringBootApplication
    static class MockConfig {
    }

    @FeignClient(name = TEST_SERVICE_CIRCUITBREAKER, contextId = CONTEXT_ID_CIRCUITBREAKER)
    static interface TestServiceCircuitbreakerClient {
        @GetMapping("/anything")
        HttpBinAnythingResponse anything();

        @GetMapping("/status/200")
        String testCircuitBreakerStatus200();

        @GetMapping("/status/500")
        String testCircuitBreakerStatus500();

        @PostMapping("/status/500")
        String testPostRetryStatus500();

        @GetMapping("/delay/1")
        String testGetDelayOneSecond();

        @GetMapping("/delay/3")
        String testGetDelayThreeSeconds();
    }

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    LoadBalancerClientFactory loadBalancerClientFactory;

    @Autowired
    RetryRegistry retryRegistry;

    @Autowired
    TestServiceCircuitbreakerClient testServiceCircuitbreakerClient;

    @MockBean
    SimpleDiscoveryClient discoveryClient;
    List<ServiceInstance> serviceInstances = List.of(new DefaultServiceInstance(
            "service2Instance2", TEST_SERVICE_CIRCUITBREAKER, GOOD_HOST, GOOD_PORT, false, Map.ofEntries(Map.entry("zone", "zone1"))
    ));

    @BeforeEach
    void setup() {
        when(discoveryClient.getInstances(TEST_SERVICE_CIRCUITBREAKER)).thenReturn(serviceInstances);
    }

    /**
     * 测试断路器的配置是对的
     */
    @Test
    public void testCircuitBreakerConfiguration() {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //调用FeignClient 确保对应的 NamedContext 被初始化
        testServiceCircuitbreakerClient.anything();

        var circuitBreakers = circuitBreakerRegistry.getAllCircuitBreakers();
        var collect = circuitBreakers.stream().map(CircuitBreaker::getName)
                .filter(name -> {
                    try {
                        return name.contains(TestServiceCircuitbreakerClient.class.getMethod("anything").toGenericString());
                    } catch (NoSuchMethodException e) {
                        return false;
                    }
                }).collect(Collectors.toSet());
        //验证断路器的实际配置，符合我们的填入的配置
        Assertions.assertEquals(1, collect.size());
        circuitBreakers.forEach(circuitBreaker -> {
            if (circuitBreaker.getName().contains(TestServiceCircuitbreakerClient.class.getName())) {
                Assertions.assertEquals((int) circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold(), (int) TEST_SERVICE_CIRCUITBREAKER_FAILURE_RATE_THRESHOLD);
                Assertions.assertEquals(TEST_SERVICE_CIRCUITBREAKER_MINIMUM_NUMBER_OF_CALLS, circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls());
            }
        });
    }

    /**
     *
     * @throws InterruptedException
     */
    @Test
    public void testCircuitBreakerTiming() throws InterruptedException {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //调用FeignClient 确保对应的 NamedContext 被初始化
        testServiceCircuitbreakerClient.anything();
        //获取断路器
        var circuitBreakers = circuitBreakerRegistry.getAllCircuitBreakers();
        //打开对应的断路器，我们的断路器是按照类名方法名命名的
        circuitBreakers.stream().forEach(circuitBreaker -> {
            if (circuitBreaker.getName().contains("TestServiceCircuitbreakerClient")) {
                circuitBreaker.transitionToOpenState();
            }
        });
        //断路器打开后，我们的请求会直接失败
        Assertions.assertThrows(
                RetryableException.class,
                () -> testServiceCircuitbreakerClient.anything()
        );
        //等待断路器超时，断路器不会恢复
        TimeUnit.MILLISECONDS.sleep(2000);
        Assertions.assertThrows(
                RetryableException.class,
                () -> testServiceCircuitbreakerClient.anything()
        );
        //等待断路器超时，断路器会恢复
        TimeUnit.MILLISECONDS.sleep(8000);
        var result = testServiceCircuitbreakerClient.anything();
        Assertions.assertTrue(result.getData().isBlank());
    }

    @Test
    public void testCircuitBreakerStatus() {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //调用 FeignClient 确保对应的 NamedContext 被初始化
        testServiceCircuitbreakerClient.testCircuitBreakerStatus200();
        //获取断路器
        var circuitBreakers = circuitBreakerRegistry.getAllCircuitBreakers();
        //打开对应的断路器，我们的断路器是按照类名方法名命名的
        circuitBreakers.stream().forEach(circuitBreaker -> {
            if (circuitBreaker.getName().contains("TestServiceCircuitbreakerClient")
                    && circuitBreaker.getName().contains("testCircuitBreakerStatus200")) {
                circuitBreaker.transitionToOpenState();
            }
        });
        //断路器打开后，我们的请求会直接失败
        Assertions.assertThrows(
                RetryableException.class,
                () -> testServiceCircuitbreakerClient.testCircuitBreakerStatus200()
        );
        //验证只会打开对应方法的断路器，不会影响其他方法
        var result = testServiceCircuitbreakerClient.anything();
        Assertions.assertTrue(result.getData().isBlank());

        //转换为半开状态
        circuitBreakerRegistry.getAllCircuitBreakers().stream().forEach(circuitBreaker -> {
            if (circuitBreaker.getName().contains("TestServiceCircuitbreakerClient")
                    && circuitBreaker.getName().contains("testCircuitBreakerStatus200")) {
                circuitBreaker.transitionToHalfOpenState();
            }
        });
        //验证半开状态下，断路器会尝试请求
        var result1 = testServiceCircuitbreakerClient.testCircuitBreakerStatus200();
        Assertions.assertNull(result1);

        //再次打开对应的断路器，这样检验断路器的状态表现正常
        circuitBreakerRegistry.getAllCircuitBreakers().stream().forEach(circuitBreaker -> {
            if (circuitBreaker.getName().contains("TestServiceCircuitbreakerClient")
                    && circuitBreaker.getName().contains("testCircuitBreakerStatus200")) {
                circuitBreaker.transitionToOpenState();
            }
        });
        //断路器打开后，我们的请求会直接失败
        Assertions.assertThrows(
                RetryableException.class,
                () -> testServiceCircuitbreakerClient.testCircuitBreakerStatus200()
        );
        //依然不影响其他方法
        result = testServiceCircuitbreakerClient.anything();
        Assertions.assertTrue(result.getData().isBlank());

        //关闭对应的断路器
        circuitBreakerRegistry.getAllCircuitBreakers().stream().forEach(circuitBreaker -> {
            if (circuitBreaker.getName().contains("TestServiceCircuitbreakerClient")
                    && circuitBreaker.getName().contains("testCircuitBreakerStatus200")) {
                circuitBreaker.transitionToClosedState();
            }
        });
        //验证断路器关闭后，可以正常请求
        var result3 = testServiceCircuitbreakerClient.testCircuitBreakerStatus200();
        Assertions.assertNull(result3);
    }

}
