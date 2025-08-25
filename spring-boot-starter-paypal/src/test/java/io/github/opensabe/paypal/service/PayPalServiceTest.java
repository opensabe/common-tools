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

import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.paypal.bo.PayPalTokenResponseBO;
import io.github.opensabe.paypal.config.PayPalProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//todo 在 github action 里面加入 secret，之后通过环境变量读取
@Disabled
@SpringBootTest(classes = App.class)
@DisplayName("PayPal服务测试")
public class PayPalServiceTest {

    @Autowired
    private PayPalProperties properties;

    @Autowired
    private PayPalService payPalService;

    /**
     * 获取token 单测
     *
     */
    @Test
    @DisplayName("测试获取PayPal访问令牌")
    public void getTokenTest() {

        System.out.println(payPalService.getToken());
    }

    /**
     * 获取PayPal的plans列表 单测
     *
     */
    @Test
    @DisplayName("测试获取PayPal计划列表")
    public void getPlansTest() {
        System.out.println(payPalService.getPlans());
    }

    /**
     * 获取PayPal API token 单测
     */
    @Test
    @DisplayName("测试从API获取PayPal令牌")
    public void obtainTokenFromApiTest() {
        PayPalTokenResponseBO result = payPalService.obtainTokenFromApi(properties.getUrl(), properties.getClientId(), properties.getClientSecret());
        System.out.println(JsonUtil.toJSONString(result));
    }

    /**
     * 获取plan详情 单测
     */
    @Test
    @DisplayName("测试获取PayPal计划详情")
    public void getPlanDetailsTest() {
        String planId = "P-xxxxxxx";
        System.out.println(payPalService.getPlanDetails(planId));
    }

}