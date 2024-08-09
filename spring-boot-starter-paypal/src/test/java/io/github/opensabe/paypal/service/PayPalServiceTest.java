package io.github.opensabe.paypal.service;

import io.github.opensabe.common.core.AppException;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.paypal.bo.PayPalTokenResponseBO;
import io.github.opensabe.paypal.config.PayPalProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//todo 在 github action 里面加入 secret，之后通过环境变量读取
@Disabled
@SpringBootTest(classes = App.class)
public class PayPalServiceTest {

    @Autowired
    private PayPalProperties properties;

    @Autowired
    private PayPalService payPalService;

    /**
     * 获取token 单测
     *
     * @throws AppException
     */
    @Test
    public void getTokenTest() throws AppException {

        System.out.println(payPalService.getToken());
    }

    /**
     * 获取PayPal的plans列表 单测
     *
     * @throws AppException
     */
    @Test
    public void getPlansTest() {
        System.out.println(payPalService.getPlans());
    }

    /**
     * 获取PayPal API token 单测
     */
    @Test
    public void obtainTokenFromApiTest() {
        PayPalTokenResponseBO result = payPalService.obtainTokenFromApi(properties.getUrl(), properties.getClientId(), properties.getClientSecret());
        System.out.println(JsonUtil.toJSONString(result));
    }

    /**
     * 获取plan详情 单测
     */
    @Test
    public void getPlanDetailsTest() {
        String planId = "P-xxxxxxx";
        System.out.println(payPalService.getPlanDetails(planId));
    }

}