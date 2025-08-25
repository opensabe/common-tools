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

import com.apple.itunes.storekit.client.APIException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AppleLoginAPIClient {
    private static final String BASE_URL = "https://appleid.apple.com";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;

    private final AppleLoginClientSecretAuthenticator appleLoginClientSecretAuthenticator;
    private final HttpUrl urlBase = HttpUrl.parse(BASE_URL);
    private final ObjectMapper objectMapper;

    private final String redirectUri;

    private final String clientId;

    public String getClientId() {
        return clientId;
    }

    public AppleLoginAPIClient(String signingKey, String keyId, String issuerId, String bundleId, String redirectUri) {
        this.appleLoginClientSecretAuthenticator = new AppleLoginClientSecretAuthenticator(issuerId, keyId, bundleId, signingKey);
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
        this.redirectUri = redirectUri;
        this.clientId = bundleId;
    }


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

    private Response getResponse(Request request) throws IOException {
        Call call = httpClient.newCall(request);
        return call.execute();
    }

    private <T> T makeHttpCall(String path, String method, Map<String, List<String>> queryParameters, Object body, FormBody formBody, Class<T> clazz) throws IOException, APIException {
        try (Response r = makeRequest(path, method, queryParameters, body, formBody)) {
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

    public TokenResponse authToken(String code) throws APIException, IOException {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("client_id", this.appleLoginClientSecretAuthenticator.getBundleId());
        builder.add("client_secret", this.appleLoginClientSecretAuthenticator.generateToken());
        builder.add("code", code);
        builder.add("grant_type", "authorization_code");
        if (Objects.nonNull(this.redirectUri)) {
            builder.add("redirect_uri", this.redirectUri);
        }
        TokenResponse tokenResponse = makeHttpCall("/auth/token", "POST", Map.of(), null, builder.build(), TokenResponse.class);
        return tokenResponse;
    }

    public static class TokenResponse {

        private String access_token;

        private Long expires_in;

        private String id_token;

        private String refresh_token;

        private String token_type;

        private String error;

        private String error_description;

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public Long getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Long expires_in) {
            this.expires_in = expires_in;
        }

        public String getId_token() {
            return id_token;
        }

        public void setId_token(String id_token) {
            this.id_token = id_token;
        }

        public String getRefresh_token() {
            return refresh_token;
        }

        public void setRefresh_token(String refresh_token) {
            this.refresh_token = refresh_token;
        }

        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        public String getError_description() {
            return error_description;
        }

        public void setError_description(String error_description) {
            this.error_description = error_description;
        }
    }

    public AuthKeys authKeys() throws IOException {
        Request.Builder requestBuilder = new Request.Builder();
        HttpUrl.Builder urlBuilder = urlBase.resolve("/auth/keys").newBuilder();
        requestBuilder.url(urlBuilder.build());
        Response response = getResponse(requestBuilder.build());
        return objectMapper.readValue(response.body().charStream(), AuthKeys.class);
    }

    public static class AuthKeys {
        private List<AuthKey> keys;

        public List<AuthKey> getKeys() {
            return keys;
        }

        public void setKeys(List<AuthKey> keys) {
            this.keys = keys;
        }
    }

    public static class AuthKey {
        private String kty;

        private String kid;

        private String use;

        private String alg;

        private String n;

        private String e;

        public String getKty() {
            return kty;
        }

        public void setKty(String kty) {
            this.kty = kty;
        }

        public String getKid() {
            return kid;
        }

        public void setKid(String kid) {
            this.kid = kid;
        }

        public String getUse() {
            return use;
        }

        public void setUse(String use) {
            this.use = use;
        }

        public String getAlg() {
            return alg;
        }

        public void setAlg(String alg) {
            this.alg = alg;
        }

        public String getN() {
            return n;
        }

        public void setN(String n) {
            this.n = n;
        }

        public String getE() {
            return e;
        }

        public void setE(String e) {
            this.e = e;
        }
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
