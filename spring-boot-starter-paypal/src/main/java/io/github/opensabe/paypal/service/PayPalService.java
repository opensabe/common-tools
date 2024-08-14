package io.github.opensabe.paypal.service;

import com.alibaba.fastjson.JSON;
import io.github.opensabe.common.core.AppException;
import io.github.opensabe.common.core.ErrorCode;
import io.github.opensabe.paypal.bo.PayPalTokenResponseBO;
import io.github.opensabe.paypal.config.PayPalProperties;
import io.github.opensabe.paypal.dto.PayPalPlanDTO;
import io.github.opensabe.paypal.dto.PayPalPlanDetailResponseDTO;
import io.github.opensabe.paypal.dto.PayPalPlanResponseDTO;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * PayPal 通用service
 * https://developer.paypal.com/api/rest/
 */
@Service
@Log4j2
public class PayPalService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取token的uri
     */
    public static final String PAYPAL_TOKEN_URI = "/oauth2/token";

    /**
     * 获取plans的uri
     * 目前写死1页20条，生产环境也够用了
     */
    public static final String PAYPAL_PLANS_URI = "/billing/plans";

    /**
     * 获取plans 详情的 的uri
     */
    public static final String PAYPAL_PLANS_DETAIL_URI = "/billing/plans/";

    /**
     * PayPal token 缓存key
     */
    public static final String PAYPAL_TOKEN_REDIS_KEY = "patron:paypal:token";

    @Autowired
    private PayPalProperties properties;

    private OkHttpClient okHttpClient;

    @Autowired
    public PayPalService(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * 获取token
     *
     * @return
     * @throws AppException
     */
    public String getToken() throws AppException {
        if (Objects.isNull(properties)
                || Objects.isNull(properties.getUrl())
                || Objects.isNull(properties.getClientId())
                || Objects.isNull(properties.getClientSecret())) {
            throw new AppException(ErrorCode.INVALID, "api or clientId or secret not null");
        }

        // 如果缓存中找到token，则返回
        String token = redisTemplate.opsForValue().get(PAYPAL_TOKEN_REDIS_KEY);

        // redis找到，但无效，则重新生成，这种情况需要根据请求api得到结果来处理，就删除redis中的缓存即可。

        // redis找不到，则重新生成，并缓存到redis中（按照token的失效时间设置其缓存有效期）
        if (StringUtils.isBlank(token)) {
            PayPalTokenResponseBO payPalTokenResponseBO = this.obtainTokenFromApi(properties.getUrl(), properties.getClientId(), properties.getClientSecret());
            log.info("PayPalService.getToken payPalTokenResponseBO:{}", payPalTokenResponseBO);

            if (Objects.nonNull(payPalTokenResponseBO) && StringUtils.isNotBlank(payPalTokenResponseBO.getAccessToken())) {
                // 找到token，缓存起来，并返回
                token = payPalTokenResponseBO.getAccessToken();

                // 缓存起来
                redisTemplate.opsForValue().set(PAYPAL_TOKEN_REDIS_KEY, token, payPalTokenResponseBO.getExpiresIn(), TimeUnit.SECONDS);
            }
        }

        return token;
    }

    /**
     * 获取PayPal API token
     * https://developer.paypal.com/api/rest/
     *
     * @param url          PayPal请求地址
     * @param clientId     clientId
     * @param clientSecret clientSecret
     */
    public PayPalTokenResponseBO obtainTokenFromApi(String url, String clientId, String clientSecret) {
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .build();

        // 构建一个formBody builder
        FormBody.Builder formBuilder = new FormBody.Builder();
        // 固定填写
        formBuilder.add("grant_type", "client_credentials");

        // 添加Basic Authentication 认证
        String credentials = clientId + ":" + clientSecret;
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        // 构建formBody，将其传入Request请求中
        FormBody body = formBuilder.build();
        Request request = new Request.Builder()
                .url(url + PAYPAL_TOKEN_URI)
                .post(body)
                .addHeader("Authorization", basicAuth)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        Call call = okHttpClient.newCall(request);

        // 返回请求结果
        try (Response response = call.execute()) {
            return JSON.parseObject(response.body().string(), PayPalTokenResponseBO.class);
        } catch (IOException e) {
            log.error("PayPalService.obtainTokenFromApi error:{}", e);
        }

        return null;
    }

    /**
     * 获取PayPal的plans列表
     *
     * @return plans集合
     */
    public List<PayPalPlanDTO> getPlans() {
        List<PayPalPlanDTO> result = new ArrayList<>();

        String url = properties.getUrl() + PAYPAL_PLANS_URI;
        List<PayPalPlanDTO> payPalPlanDTOS = this.obtainPlansFromApi(url + "?page_size=20&page=1");

        if (CollectionUtils.isNotEmpty(payPalPlanDTOS)) {
            result.addAll(payPalPlanDTOS);
        }

        // 如果查询的数量超过20条，则在查询下一页，生产环境不会超过20条，这里就最多查询前2页就好
        if (CollectionUtils.isNotEmpty(payPalPlanDTOS) && payPalPlanDTOS.size() == 20) {
            payPalPlanDTOS = this.obtainPlansFromApi(url + "?page_size=20&page=2");

            if (CollectionUtils.isNotEmpty(payPalPlanDTOS)) {
                result.addAll(payPalPlanDTOS);
            }
        }

        return result;
    }

    /**
     * 获取PayPal API plans
     * https://developer.paypal.com/docs/api/subscriptions/v1/#plans_list
     *
     * @return
     */
    private List<PayPalPlanDTO> obtainPlansFromApi(String url) {
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .build();

        String basicAuth = null;
        try {
            basicAuth = "Bearer " + getToken();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", basicAuth)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        Call call = okHttpClient.newCall(request);

        // 返回请求结果
        try (Response response = call.execute()) {
            PayPalPlanResponseDTO payPalPlanResponseDTO = JSON.parseObject(response.body().string(), PayPalPlanResponseDTO.class);
            return payPalPlanResponseDTO.getPlans();
        } catch (IOException e) {
            log.error("PayPalService.obtainTokenFromApi error:{}", e);
        }

        return null;
    }

    /**
     * 获取plan详情
     *
     * @param planId planId
     * @return
     */
    public PayPalPlanDetailResponseDTO getPlanDetails(String planId) {
        if (StringUtils.isNotBlank(planId)) {
            return this.obtainPlansDetailFromApi(properties.getUrl(), planId);
        }
        return null;
    }

    /**
     * 获取PayPal API plans detail
     * https://developer.paypal.com/docs/api/subscriptions/v1/#plans_get
     *
     * @param url
     * @return
     */
    private PayPalPlanDetailResponseDTO obtainPlansDetailFromApi(String url, String planId) {
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .build();

        String basicAuth = null;
        try {
            basicAuth = "Bearer " + getToken();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }

        Request request = new Request.Builder()
                .url(url + PAYPAL_PLANS_DETAIL_URI + planId)
                .addHeader("Authorization", basicAuth)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        Call call = okHttpClient.newCall(request);

        // 返回请求结果
        try (Response response = call.execute()) {
            return JSON.parseObject(response.body().string(), PayPalPlanDetailResponseDTO.class);
        } catch (IOException e) {
            log.error("PayPalService.obtainPlansDetailFromApi error:{}", e);
        }

        return null;
    }
}
