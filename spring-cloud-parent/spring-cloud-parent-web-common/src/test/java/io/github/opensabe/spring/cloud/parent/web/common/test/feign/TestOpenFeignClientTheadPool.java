package io.github.opensabe.spring.cloud.parent.web.common.test.feign;

import io.github.opensabe.spring.cloud.parent.web.common.test.CommonMicroServiceTest;
import feign.Request;
import feign.RetryableException;
import feign.httpclient.ApacheHttpClient;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureObservability
@ActiveProfiles("threadpool")
@EnableFeignClients
public class TestOpenFeignClientTheadPool extends CommonMicroServiceTest {

    static final String TEST_SERVICE_1 = "ThreadPoolTestService1";
    static final String CONTEXT_ID_1 = "ThreadPoolTestService1Client1";
    static final String TEST_SERVICE_2 = "ThreadPoolTestService2";
    static final String CONTEXT_ID_2 = "ThreadPoolTestService2Client1";
    static final String TEST_SERVICE_3 = "ThreadPoolTestService3";
    static final String CONTEXT_ID_3 = "ThreadPoolTestService3Client1";
    static final String TEST_SERVICE_4 = "ThreadPoolTestService4";
    static final String CONTEXT_ID_4 = "ThreadPoolTestService4Client1";

    static final int DEFAULT_THREAD_POOL_SIZE = 10;
    static final int TEST_SERVICE_2_THREAD_POOL_SIZE = 5;
    static final int TEST_SERVICE_3_THREAD_POOL_SIZE = 1;

    static final String THREAD_ID_HEADER = "Threadid";

    @SpringBootApplication
    static class MockConfig {
    }

    @FeignClient(name = TEST_SERVICE_1, contextId = CONTEXT_ID_1)
    static interface TestService1Client {
        @GetMapping("/anything")
        HttpBinAnythingResponse anything();
    }

    @FeignClient(name = TEST_SERVICE_2, contextId = CONTEXT_ID_2)
    static interface TestService2Client {
        @GetMapping("/anything")
        HttpBinAnythingResponse anything();
    }

    @FeignClient(name = TEST_SERVICE_3, contextId = CONTEXT_ID_3)
    static interface TestService3Client {
        @GetMapping("/anything")
        HttpBinAnythingResponse anything();
        @GetMapping("/delay/1")
        HttpBinAnythingResponse delay();
    }

    @FeignClient(name = TEST_SERVICE_4, contextId = CONTEXT_ID_4)
    static interface TestService4Client {
        @GetMapping("/anything")
        HttpBinAnythingResponse anything();
    }

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    @Autowired
    private ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry;
    @Autowired
    private TestService1Client testService1Client;
    @Autowired
    private TestService2Client testService2Client;
    @Autowired
    private TestService3Client testService3Client;
    @Autowired
    private TestService4Client testService4Client;
    @Autowired
    private ObservationRegistry observationRegistry;

    @MockBean(name = "service1")
    private ServiceInstance serviceInstance1;
    @MockBean(name = "service2")
    private ServiceInstance serviceInstance2;
    @MockBean(name = "service3")
    private ServiceInstance serviceInstance3;
    @MockBean(name = "service4")
    private ServiceInstance serviceInstance4;
    @MockBean
    private SimpleDiscoveryClient discoveryClient;
    @SpyBean
    private ApacheHttpClient apacheHttpClient;


    @BeforeEach
    void setup() throws IOException {
        when(serviceInstance1.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance1.getInstanceId()).thenReturn("service1Instance1");
        when(serviceInstance1.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance1.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance1.getScheme()).thenReturn("http");

        when(serviceInstance2.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance2.getInstanceId()).thenReturn("service2Instance1");
        when(serviceInstance2.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance2.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance2.getScheme()).thenReturn("http");


        when(serviceInstance3.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance3.getInstanceId()).thenReturn("service3Instance1");
        when(serviceInstance3.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance3.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance3.getScheme()).thenReturn("http");

        when(serviceInstance4.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance4.getInstanceId()).thenReturn("service4Instance1");
        when(serviceInstance4.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance4.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance4.getScheme()).thenReturn("http");

        when(discoveryClient.getInstances(TEST_SERVICE_1)).thenReturn(List.of(serviceInstance1));
        when(discoveryClient.getInstances(TEST_SERVICE_2)).thenReturn(List.of(serviceInstance2));
        when(discoveryClient.getInstances(TEST_SERVICE_3)).thenReturn(List.of(serviceInstance3));
        when(discoveryClient.getInstances(TEST_SERVICE_4)).thenReturn(List.of(serviceInstance4));

        //为了验证执行最后 http 请求的线程究竟是哪个，我们捕获 ApacheHttpClient 的真实调用，
        //并在调用前设置一个 Header，这个 Header 的值就是当前线程的 ID
        doAnswer(invocation -> {
            // 获取参数
            Request request = invocation.getArgument(0);
            // 反射修改 headers
            Field headers = ReflectionUtils.findField(Request.class, "headers");
            ReflectionUtils.makeAccessible(headers);
            Map<String, Collection<String>> map = (Map<String, Collection<String>>) ReflectionUtils.getField(headers, request);
            HashMap<String, Collection<String>> stringCollectionHashMap = new HashMap<>(map);
            stringCollectionHashMap.put(THREAD_ID_HEADER, List.of(String.valueOf(Thread.currentThread().getId())));
            ReflectionUtils.setField(headers, request, stringCollectionHashMap);
            // 执行真实方法
            return invocation.callRealMethod();
        }).when(apacheHttpClient).execute(any(Request.class), any(Request.Options.class));

    }

    /**
     * 测试一个服务的线程池已满，但是其他服务不受影响
     * @throws InterruptedException
     */
    @Test
    void testOneServiceThreadPoolAlreadyFulledButOtherServiceNotAffect() throws InterruptedException {
        //防止断路器影响"
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //设定测试前参数"
        Thread[] threads = new Thread[100];
        AtomicBoolean threadIsFull = new AtomicBoolean(false);
        AtomicBoolean passed = new AtomicBoolean(true);
        //循环100次"
        for (int i = 0; i < 100; i++) {
            threads[i] = new Thread(() -> {
                HttpBinAnythingResponse response;
                try {
                    response = testService4Client.anything();
                } catch (RetryableException ignored) {
                    passed.set(false);
                }
                try {
                    response = testService3Client.delay();
                } catch (RetryableException ignored) {
                    threadIsFull.set(true);
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < 100; i++) {
            threads[i].join();
        }

        Assertions.assertTrue( threadIsFull.get() && passed.get());
    }

    /**
     * 测试不同微服务的线程不一样
     * @throws InterruptedException
     */
    @Test
    void testDifferentServiceWithDifferentThread() throws InterruptedException {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //设定测试前参数
        Thread[] threads = new Thread[100];
        AtomicBoolean passed = new AtomicBoolean(true);
        //循环异步调用 100 次，验证不同微服务的线程不一样
        for (int i = 0; i < 100; i++) {
            threads[i] = new Thread(() -> {
                var parent = Observation.start("parent", observationRegistry);
                parent.scoped(() -> {
                    HttpBinAnythingResponse response = testService1Client.anything();
                    String threadId1 = response.getHeaders().get(THREAD_ID_HEADER).get(0);
                    response = testService2Client.anything();
                    String threadId2 = response.getHeaders().get(THREAD_ID_HEADER).get(0);
                    //如果不同微服务的线程一样，则不通过
                    if (StringUtils.equalsIgnoreCase(threadId1, threadId2)) {
                        passed.set(false);
                    }
                });
            });
            threads[i].start();
        }
        for (int i = 0; i < 100; i++) {
            threads[i].join();
        }
        Assertions.assertTrue( passed.get());
    }

    /**
     * 测试线程池隔离的实际配置
     */
    @Test
    void testConfigureThreadPool() {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        //执行一次请求，触发线程池创建
        testService1Client.anything();
        testService2Client.anything();
        testService3Client.anything();
        testService4Client.anything();
        //验证线程隔离的实际配置
        Set<ThreadPoolBulkhead> threadPoolBulkheads = threadPoolBulkheadRegistry.getAllBulkheads();
        Set<String> collect = threadPoolBulkheads.stream().map(ThreadPoolBulkhead::getName)
                .filter(name -> name.contains(CONTEXT_ID_1) || name.contains(CONTEXT_ID_2) || name.contains(CONTEXT_ID_3) || name.contains(CONTEXT_ID_4)).collect(Collectors.toSet());
        Assertions.assertTrue( collect.size() == 4);

        threadPoolBulkheads.forEach(threadPoolBulkhead -> {
            if (threadPoolBulkhead.getName().contains(CONTEXT_ID_1)) {
                Assertions.assertTrue( threadPoolBulkhead.getBulkheadConfig().getCoreThreadPoolSize() == DEFAULT_THREAD_POOL_SIZE);
                Assertions.assertTrue( threadPoolBulkhead.getBulkheadConfig().getMaxThreadPoolSize() == DEFAULT_THREAD_POOL_SIZE);
            } else if (threadPoolBulkhead.getName().contains(CONTEXT_ID_2)) {
                Assertions.assertTrue( threadPoolBulkhead.getBulkheadConfig().getCoreThreadPoolSize() == TEST_SERVICE_2_THREAD_POOL_SIZE);
                Assertions.assertTrue( threadPoolBulkhead.getBulkheadConfig().getMaxThreadPoolSize() == TEST_SERVICE_2_THREAD_POOL_SIZE);
            } else if (threadPoolBulkhead.getName().contains(CONTEXT_ID_3)) {
                Assertions.assertTrue( threadPoolBulkhead.getBulkheadConfig().getCoreThreadPoolSize() == TEST_SERVICE_3_THREAD_POOL_SIZE);
                Assertions.assertTrue( threadPoolBulkhead.getBulkheadConfig().getMaxThreadPoolSize() == TEST_SERVICE_3_THREAD_POOL_SIZE);
                Assertions.assertTrue( threadPoolBulkhead.getBulkheadConfig().getQueueCapacity() == TEST_SERVICE_3_THREAD_POOL_SIZE);
            }
        });
    }
}
