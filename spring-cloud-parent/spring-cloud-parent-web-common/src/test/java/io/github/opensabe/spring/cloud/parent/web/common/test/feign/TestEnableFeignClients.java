package io.github.opensabe.spring.cloud.parent.web.common.test.feign;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.test.context.ActiveProfiles;

public class TestEnableFeignClients {
    @EnableFeignClients
    @SpringBootTest
    @ActiveProfiles("")
    static class TestEnableFeignClientsWithoutAnyDefaultConfiguration {
        @FeignClient(name = "test")
        interface TestFeignClient {

        }
    }
}
