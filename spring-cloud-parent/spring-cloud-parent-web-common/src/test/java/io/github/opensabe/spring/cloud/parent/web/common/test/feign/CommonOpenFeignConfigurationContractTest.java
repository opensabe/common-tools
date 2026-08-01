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
package io.github.opensabe.spring.cloud.parent.web.common.test.feign;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import feign.Client;

import io.github.opensabe.spring.cloud.parent.web.common.feign.FeignBlockingLoadBalancerClientDelegate;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Upgrade gate: CommonOpenFeignConfiguration must keep custom LB Feign Client as @Primary.
 * Does not require Testcontainers — bean wiring only.
 */
@EnableFeignClients
@SpringBootTest(
        classes = CommonOpenFeignConfigurationContractTest.App.class,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.openfeign.circuitbreaker.enabled=true"
        })
@DisplayName("CommonOpenFeignConfiguration bean 契约")
class CommonOpenFeignConfigurationContractTest {

    @Autowired
    private Client client;

    @Test
    @DisplayName("注入的 Feign Client 为 FeignBlockingLoadBalancerClientDelegate")
    void primaryClientIsCustomDelegate() {
        assertNotNull(client);
        assertInstanceOf(FeignBlockingLoadBalancerClientDelegate.class, client);
    }

    @FeignClient(name = "contractService", contextId = "contractServiceClient")
    interface ContractClient {
        @GetMapping("/status/200")
        String ping();
    }

    @SpringBootApplication
    static class App {
    }
}
