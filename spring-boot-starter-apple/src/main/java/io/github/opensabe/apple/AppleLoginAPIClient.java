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
package io.github.opensabe.apple;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.apple.itunes.storekit.client.APIException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Apple Sign In（{@code appleid.apple.com}）HTTP 客户端。
 * <p>
 * 使用 {@link AppleLoginClientSecretAuthenticator} 生成 client_secret，经 OkHttp 调用 OAuth 与 JWKS 接口。
 */
public class AppleLoginAPIClient {

    /** Apple ID 服务根 URL。 */
    private static final String BASE_URL = "https://appleid.apple.com";

    /** JSON 请求体 MediaType。 */
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /** 共享 HTTP 客户端。 */
    private final OkHttpClient httpClient;

    /** ES256 client_secret 生成器。 */
    private final AppleLoginClientSecretAuthenticator appleLoginClientSecretAuthenticator;

    /** 解析自 {@link #BASE_URL} 的基址。 */
    private final HttpUrl urlBase = HttpUrl.parse(BASE_URL);

    /** Jackson 3 映射器（字段可见、忽略未知属性）。 */
    private final ObjectMapper objectMapper;

    /** OAuth 授权码交换时的 redirect_uri。 */
    private final String redirectUri;

    /** OAuth client_id（与 bundleId 相同）。 */
    private final String clientId;

    /**
     * @param signingKey Apple 私钥（PKCS#8 DER Base64）
     * @param keyId 密钥 ID
     * @param issuerId Team/Issuer ID
     * @param bundleId 应用 Bundle ID（client_id）
     * @param redirectUri 可选 redirect_uri
     */
    public AppleLoginAPIClient(String signingKey, String keyId, String issuerId, String bundleId, String redirectUri) {
        this.appleLoginClientSecretAuthenticator = new AppleLoginClientSecretAuthenticator(issuerId, keyId, bundleId, signingKey);
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
        this.redirectUri = redirectUri;
        this.clientId = bundleId;
    }

    /**
     * @return OAuth client_id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 构造并执行 HTTP 请求（表单或 JSON 体）。
     *
     * @param path 相对路径
     * @param method HTTP 方法
     * @param queryParameters 查询参数
     * @param body JSON 体；与 {@code formBody} 互斥
     * @param formBody 表单体；POST 且无 JSON 时使用
     * @return 原始响应（调用方须关闭）
     */
    private Response makeRequest(String path, String method, Map<String, List<String>> queryParameters, Object body, FormBody formBody) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.addHeader("content-type", "application/x-www-form-urlencoded");
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
            requestBuilder.method(method, formBody);
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
     * 执行 HTTP 调用并解析 JSON 响应体。
     *
     * @param path 路径
     * @param method 方法
     * @param queryParameters 查询参数
     * @param body JSON 体
     * @param formBody 表单体
     * @param clazz 响应类型；{@link Void} 表示无体
     * @param <T> 响应类型
     * @return 反序列化结果
     * @throws IOException 网络或 IO 错误
     * @throws APIException 非 2xx 或解析失败
     */
    private <T> T makeHttpCall(String path, String method, Map<String, List<String>> queryParameters, Object body, FormBody formBody, Class<T> clazz) throws IOException, APIException {
        try (Response r = makeRequest(path, method, queryParameters, body, formBody)) {
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
                        return objectMapper.readValue(responseBody.charStream(), clazz);
                    }
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
     * 使用授权码交换 token（{@code /auth/token}）。
     *
     * @param code 授权码
     * @return Token 响应
     * @throws APIException Apple API 错误
     * @throws IOException IO 错误
     */
    public TokenResponse authToken(String code) throws APIException, IOException {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("client_id", this.appleLoginClientSecretAuthenticator.getBundleId());
        builder.add("client_secret", this.appleLoginClientSecretAuthenticator.generateToken());
        builder.add("code", code);
        builder.add("grant_type", "authorization_code");
        if (Objects.nonNull(this.redirectUri)) {
            builder.add("redirect_uri", this.redirectUri);
        }
        return makeHttpCall("/auth/token", "POST", Map.of(), null, builder.build(), TokenResponse.class);
    }

    /**
     * 获取 Sign In with Apple JWKS（{@code /auth/keys}）。
     *
     * @return 公钥集合
     * @throws IOException IO 错误
     */
    public AuthKeys authKeys() throws IOException {
        Request.Builder requestBuilder = new Request.Builder();
        HttpUrl.Builder urlBuilder = urlBase.resolve("/auth/keys").newBuilder();
        requestBuilder.url(urlBuilder.build());
        try (Response response = getResponse(requestBuilder.build())) {
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body from Apple auth/keys");
            }
            return objectMapper.readValue(body.charStream(), AuthKeys.class);
        }
    }

    /**
     * {@code /auth/token} 响应体。
     */
    public static class TokenResponse {

        /** 访问令牌。 */
        private String access_token;

        /** 过期秒数。 */
        private Long expires_in;

        /** ID Token（JWT）。 */
        private String id_token;

        /** 刷新令牌。 */
        private String refresh_token;

        /** 令牌类型（通常为 Bearer）。 */
        private String token_type;

        /** OAuth 错误码。 */
        private String error;

        /** OAuth 错误描述。 */
        private String error_description;

        /** @return {@link #error} */
        public String getError() {
            return error;
        }

        /** @param error OAuth 错误码 */
        public void setError(String error) {
            this.error = error;
        }

        /** @return {@link #access_token} */
        public String getAccess_token() {
            return access_token;
        }

        /** @param access_token 访问令牌 */
        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        /** @return {@link #expires_in} */
        public Long getExpires_in() {
            return expires_in;
        }

        /** @param expires_in 过期秒数 */
        public void setExpires_in(Long expires_in) {
            this.expires_in = expires_in;
        }

        /** @return {@link #id_token} */
        public String getId_token() {
            return id_token;
        }

        /** @param id_token ID Token */
        public void setId_token(String id_token) {
            this.id_token = id_token;
        }

        /** @return {@link #refresh_token} */
        public String getRefresh_token() {
            return refresh_token;
        }

        /** @param refresh_token 刷新令牌 */
        public void setRefresh_token(String refresh_token) {
            this.refresh_token = refresh_token;
        }

        /** @return {@link #token_type} */
        public String getToken_type() {
            return token_type;
        }

        /** @param token_type 令牌类型 */
        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        /** @return {@link #error_description} */
        public String getError_description() {
            return error_description;
        }

        /** @param error_description 错误描述 */
        public void setError_description(String error_description) {
            this.error_description = error_description;
        }
    }

    /**
     * JWKS 容器（{@code keys} 数组）。
     */
    public static class AuthKeys {

        /** JWK 列表。 */
        private List<AuthKey> keys;

        /** @return {@link #keys} */
        public List<AuthKey> getKeys() {
            return keys;
        }

        /** @param keys JWK 列表 */
        public void setKeys(List<AuthKey> keys) {
            this.keys = keys;
        }
    }

    /**
     * 单条 JSON Web Key。
     */
    public static class AuthKey {

        /** 密钥类型。 */
        private String kty;

        /** 密钥 ID。 */
        private String kid;

        /** 用途。 */
        private String use;

        /** 算法。 */
        private String alg;

        /** RSA 模数。 */
        private String n;

        /** RSA 指数。 */
        private String e;

        /** @return {@link #kty} */
        public String getKty() {
            return kty;
        }

        /** @param kty 密钥类型 */
        public void setKty(String kty) {
            this.kty = kty;
        }

        /** @return {@link #kid} */
        public String getKid() {
            return kid;
        }

        /** @param kid 密钥 ID */
        public void setKid(String kid) {
            this.kid = kid;
        }

        /** @return {@link #use} */
        public String getUse() {
            return use;
        }

        /** @param use 用途 */
        public void setUse(String use) {
            this.use = use;
        }

        /** @return {@link #alg} */
        public String getAlg() {
            return alg;
        }

        /** @param alg 算法 */
        public void setAlg(String alg) {
            this.alg = alg;
        }

        /** @return {@link #n} */
        public String getN() {
            return n;
        }

        /** @param n RSA 模数 */
        public void setN(String n) {
            this.n = n;
        }

        /** @return {@link #e} */
        public String getE() {
            return e;
        }

        /** @param e RSA 指数 */
        public void setE(String e) {
            this.e = e;
        }
    }
}
