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

import com.apple.itunes.storekit.client.APIException;
import com.apple.itunes.storekit.client.BearerTokenAuthenticator;
import com.apple.itunes.storekit.model.ErrorPayload;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabe.apple.appstoreconnectapi.inapppurchasesv2.InAppPurchasesV2Response;
import io.github.opensabe.apple.appstoreconnectapi.subscriptiongroup.SubscriptionGroupsResponse;
import okhttp3.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

public class AppleStoreConnectAPIClient {
    private static final String BASE_URL = "https://api.appstoreconnect.apple.com";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final BearerTokenAuthenticator bearerTokenAuthenticator;
    private final HttpUrl urlBase = HttpUrl.parse(BASE_URL);
    private final ObjectMapper objectMapper;

    private final Long appleStoreId;

    public AppleStoreConnectAPIClient(String signingKey, String keyId, String issuerId, String bundleId, Long appStoreId) {
        this.bearerTokenAuthenticator = new BearerTokenAuthenticator(signingKey, keyId, issuerId, bundleId);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        this.httpClient = builder.build();
        this.objectMapper = new ObjectMapper();
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        this.appleStoreId = appStoreId;
    }


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
        } else if (method.equals("POST")) {
            requestBuilder.method(method, RequestBody.create("", null));
        } else {
            requestBuilder.method(method, null);
        }
        return getResponse(requestBuilder.build());
    }

    private Response getResponse(Request request) throws IOException {
        Call call = httpClient.newCall(request);
        return call.execute();
    }

    private <T> T makeHttpCall(String path, String method, Map<String, List<String>> queryParameters, Object body, Class<T> clazz) throws IOException, APIException {
        try (Response r = makeRequest(path, method, queryParameters, body)) {
            if (r.code() >= 200 && r.code() < 300) {
                if (clazz.equals(Void.class)) {
                    return null;
                }
                // Success
                ResponseBody responseBody = r.body();
                if (responseBody == null) {
                    throw new RuntimeException("Response code was 2xx but no body returned");
                }
                try {
                    return objectMapper.readValue(responseBody.charStream(), clazz);
                } catch (JsonProcessingException e) {
                    throw new APIException(r.code(), e);
                }
            } else {
                // Best effort to decode the body
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

    public InAppPurchasesV2Response inAppPurchasesV2() throws APIException, IOException {
        return makeHttpCall("/v1/apps/" + appleStoreId + "/inAppPurchasesV2", "GET", Map.of(), null, InAppPurchasesV2Response.class);
    }


    public SubscriptionGroupsResponse subscriptions(String productGroupId) throws APIException, IOException {
        return makeHttpCall("/v1/subscriptionGroups/" + productGroupId + "/subscriptions", "GET", Map.of(), null, SubscriptionGroupsResponse.class);
    }
//    /**
//     * 官网文档的JWT方式 自己实现
//     * @return
//     * @throws NoSuchAlgorithmException
//     * @throws InvalidKeySpecException
//     */
//    private String buildAuthorizationToken() throws NoSuchAlgorithmException, InvalidKeySpecException {
//        LinkedHashMap<String, Object> jwtHeader = new LinkedHashMap<>();
//        jwtHeader.put("alg", "ES256");
//        jwtHeader.put("kid", this.getKeyId());
//        jwtHeader.put("typ", "JWT");
//
//        LinkedHashMap<String, Object> jwtPayload = new LinkedHashMap<>();
//        jwtPayload.put("iss", this.getIssuerId());
//        long currentTimeSecond = System.currentTimeMillis() / 1000L;
//        jwtPayload.put("iat", currentTimeSecond);
//        jwtPayload.put("exp", currentTimeSecond + 20 * 60);
//        jwtPayload.put("aud", "appstoreconnect-v1");
//
//        byte[] derEncodedSigningKey = Base64.getDecoder().decode(this.getSigningKey());
//        KeyFactory keyFactory = KeyFactory.getInstance("EC");
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(derEncodedSigningKey);
//        String sign = JWT.create()
//                .withHeader(jwtHeader)
//                .withPayload(jwtPayload)
//                .sign(Algorithm.ECDSA256((ECPrivateKey) keyFactory.generatePrivate(keySpec)));
//        return sign;
//    }
}
