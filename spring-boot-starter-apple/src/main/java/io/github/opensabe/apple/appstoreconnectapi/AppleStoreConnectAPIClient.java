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
package io.github.opensabe.apple.appstoreconnectapi;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

import com.apple.itunes.storekit.client.APIException;
import com.apple.itunes.storekit.client.BearerTokenAuthenticator;
import com.apple.itunes.storekit.model.ErrorPayload;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import io.github.opensabe.apple.appstoreconnectapi.inapppurchasesv2.InAppPurchasesV2Response;
import io.github.opensabe.apple.appstoreconnectapi.subscriptiongroup.SubscriptionGroupsResponse;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * App Store Connect API（{@code api.appstoreconnect.apple.com}）轻量客户端。
 * <p>
 * 基于 Apple {@link BearerTokenAuthenticator} 与 OkHttp；当前暴露内购 V2 与订阅组查询。
 */
public class AppleStoreConnectAPIClient {

    /** App Store Connect API 根 URL。 */
    private static final String BASE_URL = "https://api.appstoreconnect.apple.com";

    /** JSON 请求体 MediaType。 */
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /** HTTP 客户端。 */
    private final OkHttpClient httpClient;

    /** JWT Bearer 令牌生成器。 */
    private final BearerTokenAuthenticator bearerTokenAuthenticator;

    /** 解析自 {@link #BASE_URL} 的基址。 */
    private final HttpUrl urlBase = HttpUrl.parse(BASE_URL);

    /** Jackson 3 映射器。 */
    private final ObjectMapper objectMapper;

    /** App Store 应用 numeric ID。 */
    private final Long appleStoreId;

    /**
     * @param signingKey 私钥材料
     * @param keyId 密钥 ID
     * @param issuerId Issuer ID
     * @param bundleId Bundle ID
     * @param appStoreId App Store 应用 ID
     */
    public AppleStoreConnectAPIClient(String signingKey, String keyId, String issuerId, String bundleId, Long appStoreId) {
        this.bearerTokenAuthenticator = new BearerTokenAuthenticator(signingKey, keyId, issuerId, bundleId);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        this.httpClient = builder.build();
        this.objectMapper = JsonMapper.builder()
                .changeDefaultVisibility(v -> v
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE))
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        this.appleStoreId = appStoreId;
    }

    /**
     * 构造带 Bearer 认证的 HTTP 请求并执行。
     *
     * @param path API 路径
     * @param method HTTP 方法
     * @param queryParameters 查询参数
     * @param body 可选 JSON 请求体
     * @return 原始响应（调用方须关闭）
     */
    private Response makeRequest(String path, String method, Map<String, List<String>> queryParameters, Object body) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.addHeader("Authorization", "Bearer " + bearerTokenAuthenticator.generateToken());
        requestBuilder.addHeader("Accept", "application/json");
        HttpUrl.Builder urlBuilder = urlBase.resolve(path).newBuilder();
        for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
            for (String queryValue : entry.getValue()) {
                urlBuilder.addQueryParameter(entry.getKey(), queryValue);
            }
        }
        requestBuilder.url(urlBuilder.build());
        if (body != null) {
            RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(body), JSON);
            requestBuilder.method(method, requestBody);
        } else if ("POST".equals(method)) {
            requestBuilder.method(method, RequestBody.create("", null));
        } else {
            requestBuilder.method(method, null);
        }
        return getResponse(requestBuilder.build());
    }

    /**
     * 同步执行 OkHttp 调用。
     *
     * @param request 请求
     * @return 响应
     */
    private Response getResponse(Request request) throws IOException {
        Call call = httpClient.newCall(request);
        return call.execute();
    }

    /**
     * 执行 API 调用并解析 JSON；非 2xx 时解析 {@link ErrorPayload} 并抛出 {@link APIException}。
     *
     * @param path 路径
     * @param method 方法
     * @param queryParameters 查询参数
     * @param body 请求体
     * @param clazz 响应类型
     * @param <T> 响应类型
     * @return 反序列化结果
     * @throws IOException IO 错误
     * @throws APIException Apple API 错误
     */
    private <T> T makeHttpCall(String path, String method, Map<String, List<String>> queryParameters, Object body, Class<T> clazz) throws IOException, APIException {
        try (Response r = makeRequest(path, method, queryParameters, body)) {
            if (r.code() >= 200 && r.code() < 300) {
                if (clazz.equals(Void.class)) {
                    return null;
                }
                ResponseBody responseBody = r.body();
                if (responseBody == null) {
                    throw new RuntimeException("Response code was 2xx but no body returned");
                }
                try {
                    return objectMapper.readValue(responseBody.charStream(), clazz);
                } catch (JacksonException e) {
                    throw new APIException(r.code(), e);
                }
            } else {
                try {
                    ResponseBody responseBody = r.body();
                    if (responseBody != null) {
                        ErrorPayload errorPayload = objectMapper.readValue(responseBody.charStream(), ErrorPayload.class);
                        throw new APIException(r.code(), errorPayload.getErrorCode(), errorPayload.getErrorMessage());
                    }
                } catch (APIException e) {
                    throw e;
                } catch (Exception e) {
                    throw new APIException(r.code(), e);
                }
                throw new APIException(r.code());
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 列出应用的内购 V2 产品。
     *
     * @return 内购 V2 响应
     * @throws APIException API 错误
     * @throws IOException IO 错误
     */
    public InAppPurchasesV2Response inAppPurchasesV2() throws APIException, IOException {
        return makeHttpCall("/v1/apps/" + appleStoreId + "/inAppPurchasesV2", "GET", Map.of(), null, InAppPurchasesV2Response.class);
    }

    /**
     * 列出订阅组下的订阅产品。
     *
     * @param productGroupId 订阅组 ID
     * @return 订阅组响应
     * @throws APIException API 错误
     * @throws IOException IO 错误
     */
    public SubscriptionGroupsResponse subscriptions(String productGroupId) throws APIException, IOException {
        return makeHttpCall("/v1/subscriptionGroups/" + productGroupId + "/subscriptions", "GET", Map.of(), null, SubscriptionGroupsResponse.class);
    }
}
