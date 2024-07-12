package io.github.opensabe.spring.cloud.parent.web.common.test.feign;

import io.github.opensabe.spring.cloud.parent.web.common.feign.RetryableMethod;
import io.github.opensabe.spring.cloud.parent.web.common.test.CommonMicroServiceTest;
import feign.Request;
import feign.httpclient.ApacheHttpClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.AbstractRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;

@ActiveProfiles("retrytest")
@SpringBootTest
@AutoConfigureObservability
@EnableFeignClients
public class TestOpenFeignClientRetry extends CommonMicroServiceTest {
    static final String TEST_SERVICE_1 = "RetryTestService1";
    static final String CONTEXT_ID_1 = "RetryTestService1Client1";

    static final String TEST_SERVICE_2 = "RetryTestService2";
    static final String CONTEXT_ID_2 = "RetryTestService2Client2";

    static final String TEST_SERVICE_3 = "RetryTestService3";
    static final String CONTEXT_ID_3 = "RetryTestService3Client3";

    static final String TEST_SERVICE_4 = "RetryTestService4";
    static final String CONTEXT_ID_4 = "RetryTestService4Client4";

    static final String TEST_SERVICE_5 = "RetryTestService5";
    static final String CONTEXT_ID_5 = "RetryTestService5Client5";

    static final String TEST_SERVICE_6 = "RetryTestService6";
    static final String CONTEXT_ID_6 = "RetryTestService6Client6";

    static final String TEST_SERVICE_7 = "RetryTestService7";
    static final String CONTEXT_ID_7 = "RetryTestService7Client7";

    static final String TEST_SERVICE_8 = "RetryTestService8";
    static final String CONTEXT_ID_8 = "RetryTestService8Client8";

    @SpyBean
    private ApacheHttpClient apacheHttpClient;

    @SpringBootApplication
    static class MockConfig {
    }

    @FeignClient(name = TEST_SERVICE_8, contextId = CONTEXT_ID_8)
    static interface TestService8Client {
        @GetMapping("/status/200")
        Map get();
        @PostMapping("/status/200")
        Map post();
    }

    @FeignClient(name = TEST_SERVICE_7, contextId = CONTEXT_ID_7)
    static interface TestService7Client {
        @PostMapping("/delay/3")
        Map testPostDelayThreeSeconds();
    }


    @FeignClient(name = TEST_SERVICE_6, contextId = CONTEXT_ID_6)
    static interface TestService6Client {
        @GetMapping("/delay/3")
        Map testGetDelayThreeSeconds();
    }


    @FeignClient(name = TEST_SERVICE_5, contextId = CONTEXT_ID_5)
    static interface TestService5Client {
        @GetMapping("/delay/1")
        Map testGetDelayOneSecond();
    }

    @FeignClient(name = TEST_SERVICE_4, contextId = CONTEXT_ID_4)
    static interface TestService4Client {
        @PostMapping("/status/500")
        Map testPostRetryStatus500();

        @RetryableMethod
        @PostMapping("/status/500")
        Map testPostRetryMethodStatus500();
    }


    @FeignClient(name = TEST_SERVICE_3, contextId = CONTEXT_ID_3)
    static interface TestService3Client {
        @PostMapping("/status/500")
        Map testPostRetryStatus500();
    }


    @FeignClient(name = TEST_SERVICE_1, contextId = CONTEXT_ID_1)
    static interface TestService1Client {
        @GetMapping("/status/500")
        Map testGetRetryStatus500();
    }


    @FeignClient(name = TEST_SERVICE_2, contextId = CONTEXT_ID_2)
    static interface TestService2Client {
        @GetMapping("/status/500")
        Map testGetRetryStatus500();
    }

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    RetryRegistry retryRegistry;
    @Autowired
    TestService2Client testService2Client;

    @Autowired
    TestService1Client testService1Client;

    @Autowired
    TestService3Client testService3Client;

    @Autowired
    TestService4Client testService4Client;

    @Autowired
    TestService5Client testService5Client;

    @Autowired
    TestService6Client testService6Client;

    @Autowired
    TestService7Client testService7Client;

    @Autowired
    TestService8Client testService8Client;

    @MockBean(name = "service8_2")
    ServiceInstance serviceInstance8_2;
    @MockBean(name = "service8_1")
    ServiceInstance serviceInstance8_1;
    @MockBean(name = "service7")
    ServiceInstance serviceInstance7;
    @MockBean(name = "service6")
    ServiceInstance serviceInstance6;
    @MockBean(name = "service5")
    ServiceInstance serviceInstance5;
    @MockBean(name = "service4")
    ServiceInstance serviceInstance4;
    @MockBean(name = "service3")
    ServiceInstance serviceInstance3;
    @MockBean(name = "service2")
    ServiceInstance serviceInstance2;
    @MockBean(name = "service1")
    ServiceInstance serviceInstance1;

    @MockBean
    SimpleDiscoveryClient discoveryClient;

    @Autowired
    private List<AbstractRegistry> registries;
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @BeforeEach
    void setup() {
        
        when(serviceInstance7.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance7.getInstanceId()).thenReturn("service7Instance");
        when(serviceInstance7.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance7.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance7.getScheme()).thenReturn("http");

        when(serviceInstance6.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance6.getInstanceId()).thenReturn("service6Instance");
        when(serviceInstance6.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance6.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance6.getScheme()).thenReturn("http");

        when(serviceInstance5.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance5.getInstanceId()).thenReturn("service5Instance");
        when(serviceInstance5.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance5.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance5.getScheme()).thenReturn("http");

        when(serviceInstance4.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance4.getInstanceId()).thenReturn("service4Instance");
        when(serviceInstance4.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance4.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance4.getScheme()).thenReturn("http");

        when(serviceInstance3.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance3.getInstanceId()).thenReturn("service3Instance");
        when(serviceInstance3.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance3.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance3.getScheme()).thenReturn("http");

        when(serviceInstance2.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance2.getInstanceId()).thenReturn("service2Instance");
        when(serviceInstance2.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance2.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance2.getScheme()).thenReturn("http");

        when(serviceInstance1.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance1.getInstanceId()).thenReturn("service1Instance");
        when(serviceInstance1.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance1.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance1.getScheme()).thenReturn("http");

        when(discoveryClient.getInstances(TEST_SERVICE_1)).thenReturn(List.of(serviceInstance1));
        when(discoveryClient.getInstances(TEST_SERVICE_2)).thenReturn(List.of(serviceInstance2));
        when(discoveryClient.getInstances(TEST_SERVICE_3)).thenReturn(List.of(serviceInstance3));
        when(discoveryClient.getInstances(TEST_SERVICE_4)).thenReturn(List.of(serviceInstance4));
        when(discoveryClient.getInstances(TEST_SERVICE_5)).thenReturn(List.of(serviceInstance5));
        when(discoveryClient.getInstances(TEST_SERVICE_6)).thenReturn(List.of(serviceInstance6));
        when(discoveryClient.getInstances(TEST_SERVICE_7)).thenReturn(List.of(serviceInstance7));
        when(discoveryClient.getInstances(TEST_SERVICE_8)).thenReturn(List.of(serviceInstance8_1, serviceInstance8_2));

    }

    /**
     * 测试默认次数，默认为 3 次，方法为get ，遇到 internal server error 错误应该重试 3 次
     */
    @Test
    public void testRetryByDefaultGetDefault() throws IOException {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //测试默认遇到错误重试 3 次
        try {
            testService1Client.testGetRetryStatus500();
        } catch (Exception exception) {
        }
        //最终运行是否 3 次
        verify(apacheHttpClient, times(3))
                .execute(any(Request.class), any(Request.Options.class));
    }

    /**
     * 测试自定义次数为 6 次，方法为 get，遇到 internal server error 错误应该重试 6 次
     */
    @Test
    void testRetryByDefaultGetCustomized() throws IOException {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //测试自定义遇到错误重试6次
        try {
            testService2Client.testGetRetryStatus500();
        } catch (Exception exception) {
        }
        //最终运行是否 6 次
        verify(apacheHttpClient, times(6))
                .execute(any(Request.class), any(Request.Options.class));
    }

    /**
     * 测试默认 post 方法 internal server error 重试次数 post 只能 1 次
     */
    @Test
    void testRetryByDefaultPostDefault() throws IOException {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //测试默认遇到错误重试 1 次
        atomicInteger.set(0);
        try {
            testService3Client.testPostRetryStatus500();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        //最终运行是否 1 次
        verify(apacheHttpClient, times(1))
                .execute(any(Request.class), any(Request.Options.class));
    }

    /**
     * 测试自定义 post 方法 internal server error 重试次数为 1 次
     */
    @Test
    void testRetryByDefaultPostCustomized() throws IOException {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //测试默认遇到错误重试n 1 次
        try {
            testService4Client.testPostRetryStatus500();
        } catch (Exception exception) {
        }
        //最终运行是否 1 次
        verify(apacheHttpClient, times(1))
                .execute(any(Request.class), any(Request.Options.class));
    }

    /**
     * 测试默认遇到错误重试 1 次, 但是此方法上有 @RetryableMethod 需要重试
     */
    @Test
    void testRetryByRetryableMethodPostCustomized() throws IOException {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //测试默认遇到错误重试1次,但是此方法上有@RetryableMethod需要重试
        try {
            testService4Client.testPostRetryMethodStatus500();
        } catch (Exception exception) {
        }
        //最终运行是否 3 次
        verify(apacheHttpClient, times(3))
                .execute(any(Request.class), any(Request.Options.class));
    }


    /**
     * 测试默认 get 方法 readTimeout 为 2 秒，模拟对方 api 需要 1 秒，也就是不超时，直接 1 次
     */
    @Test
    void testRetryByDefaultGetDefaultNotTimeout() throws IOException {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //不超时，也没有异常
        testService5Client.testGetDelayOneSecond();
        //最终运行是否 1 次
        verify(apacheHttpClient, times(1))
                .execute(any(Request.class), any(Request.Options.class));
    }

    /**
     * 测试默认 get方法 readTimeout 为 2 秒，模拟对方 api 需要 3 秒，也就是超时，需要重试 3 次
     */
    @Test
    void testRetryByDefaultGetDefaultTimeout() throws IOException {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //超时
        try {
            testService6Client.testGetDelayThreeSeconds();

        } catch (Exception exception) {
        }
        //最终运行是否 3 次
        verify(apacheHttpClient, times(3))
                .execute(any(Request.class), any(Request.Options.class));
    }


    /**
     * 测试默认 post 方法 readTimeout 为 2 秒，模拟对方 api 需要 3 秒，也就是超时，但是 post 只需要重试 1 次
     */
    @Test
    void testRetryByDefaultPostDefaultTimeout() throws IOException {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //post超时
        try {
            testService7Client.testPostDelayThreeSeconds();
        } catch (Exception exception) {
        }
        //最终运行是否 1 次
        verify(apacheHttpClient, times(1))
                .execute(any(Request.class), any(Request.Options.class));
    }

    /**
     * 测试一个好的一个连不上的服务的重试
     */
    @Test
    void testRetryConnectTimeout() throws IOException {
        when(serviceInstance8_1.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance8_1.getInstanceId()).thenReturn("service8Instance1");
        when(serviceInstance8_1.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance8_1.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance8_1.getScheme()).thenReturn("http");
        when(serviceInstance8_2.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance8_2.getInstanceId()).thenReturn("service8Instance2");
        when(serviceInstance8_2.getHost()).thenReturn(CONNECT_TIMEOUT_HOST);
        when(serviceInstance8_2.getPort()).thenReturn(CONNECT_TIMEOUT_PORT);
        when(serviceInstance8_2.getScheme()).thenReturn("http");
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //清理调用记录
        clearInvocations(apacheHttpClient);
        //测试 3 次，虽然有不正常的实例，但是调用不会遇到异常
        for (int i = 0; i < 3; i++) {
            testService8Client.get();
        }
        //因为有重试，不止 3 次
        verify(apacheHttpClient, atLeast(3))
                .execute(any(Request.class), any(Request.Options.class));
        //就算是 post 也会重试
        //测试 3 次，虽然有不正常的实例，但是调用不会遇到异常
        for (int i = 0; i < 3; i++) {
            testService8Client.post();
        }
        //因为有重试，不止 3 次
        verify(apacheHttpClient, atLeast(3))
                .execute(any(Request.class), any(Request.Options.class));
    }

    /**
     * 测试一个好的一个读取超时的服务的重试
     */
    @Test
    void testRetryReadTimeout() throws IOException {
        when(serviceInstance8_1.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance8_1.getInstanceId()).thenReturn("service8Instance1");
        when(serviceInstance8_1.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance8_1.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance8_1.getScheme()).thenReturn("http");
        when(serviceInstance8_2.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance8_2.getInstanceId()).thenReturn("service8Instance2");
        when(serviceInstance8_2.getHost()).thenReturn(READ_TIMEOUT_HOST);
        when(serviceInstance8_2.getPort()).thenReturn(READ_TIMEOUT_PORT);
        when(serviceInstance8_2.getScheme()).thenReturn("http");
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //测试 3 次，虽然有不正常的实例，但是调用不会遇到异常
        for (int i = 0; i < 3; i++) {
            testService8Client.get();
        }
        //因为有重试，不止 3 次
        verify(apacheHttpClient, atLeast(3))
                .execute(any(Request.class), any(Request.Options.class));
        //清理调用记录
        clearInvocations(apacheHttpClient);
        //post 因为请求已经发出就不会重试
        //测试 3 次
        for (int i = 0; i < 3; i++) {
            try {
                testService8Client.post();
            } catch (Exception exception) {
            }
        }
        //针对 post 不会重试，就是调用 3 次
        verify(apacheHttpClient, times(3))
                .execute(any(Request.class), any(Request.Options.class));
    }

    /**
     * 测试一个好的一个直接 Reset （模拟正在关闭）的服务的重试
     */
    @Test
    void testRetryResetPeer() throws IOException {
        when(serviceInstance8_1.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance8_1.getInstanceId()).thenReturn("service8Instance1");
        when(serviceInstance8_1.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance8_1.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance8_1.getScheme()).thenReturn("http");
        when(serviceInstance8_2.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance8_2.getInstanceId()).thenReturn("service8Instance2");
        when(serviceInstance8_2.getHost()).thenReturn(RESET_PEER_HOST);
        when(serviceInstance8_2.getPort()).thenReturn(RESET_PEER_PORT);
        when(serviceInstance8_2.getScheme()).thenReturn("http");
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //测试 3 次，虽然有不正常的实例，但是调用不会遇到异常
        for (int i = 0; i < 3; i++) {
            testService8Client.get();
        }
        //因为有重试，不止 3 次
        verify(apacheHttpClient, atLeast(3))
                .execute(any(Request.class), any(Request.Options.class));
        //清理调用记录
        clearInvocations(apacheHttpClient);
        //post 因为请求已经发出就不会重试
        //测试 3 次
        for (int i = 0; i < 3; i++) {
            try {
                testService8Client.post();
            } catch (Exception exception) {
            }
        }
        //针对 post 不会重试，就是调用 3 次
        verify(apacheHttpClient, times(3))
                .execute(any(Request.class), any(Request.Options.class));
    }

}
