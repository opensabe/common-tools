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
package io.github.opensabe.paypal.service;

import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openapitools.client.ApiClient;
import org.openapitools.client.api.SimulateEventApi;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.github.opensabe.paypal.config.PayPalAutoConfig;
import io.github.opensabe.paypal.config.PayPalProperties;

import static org.assertj.core.api.Assertions.assertThat;

//todo 在 github action 里面加入 secret，之后通过环境变量读取
@Disabled
@Ignore
public class SimulateEventApiTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("paypal.enable=true",
                    "paypal.client-id=${paypal.client-id}",
                    "paypal.client-secret=${paypal.client-secret}",
                    "paypal.server-index=0",
                    "paypal.webhook-id=${paypal.webhook-id}"
            )
            .withConfiguration(AutoConfigurations.of(PayPalAutoConfig.class));

    @Test
    public void payPalAutoConfigurationPropertiesTest() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(PayPalProperties.class);
                    PayPalProperties paypalProperties = context.getBean(PayPalProperties.class);
                    Assertions.assertEquals(paypalProperties.getClientId(), "${paypal.client-id}");
                    Assertions.assertEquals(paypalProperties.getClientSecret(), "${paypal.client-secret}");
                    Assertions.assertEquals(paypalProperties.getServerIndex(), 0);
                    Assertions.assertEquals(paypalProperties.getWebhookId(), "${paypal.webhook-id}");
                });
    }

    @Test
    public void payPalAutoConfigurationSimulateEventApiTest() {
        contextRunner
                .run(context -> {
                    PayPalService payPalService = context.getBean(PayPalService.class);
                    String token = payPalService.getToken();
                    ApiClient apiClient = new ApiClient();
                    apiClient.setAccessToken(token);
                    Integer serverIndex = context.getEnvironment().getProperty("paypal.server-index", Integer.class);
                    apiClient.setServerIndex(serverIndex);

                    SimulateEventApi simulateEventApi = new SimulateEventApi(apiClient);
                    String webhookId = context.getEnvironment().getProperty("paypal.webhook-id");
//                    new SimulateEvent()
//                            .eventType("CATALOG.PRODUCT.CREATED")
//                            .webhookId(webhookId)
//                                    .
//                    simulateEventApi.simulateEventPost()
                });
    }
}
