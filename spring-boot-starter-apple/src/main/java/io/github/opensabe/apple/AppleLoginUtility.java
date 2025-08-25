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

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Log4j2
public class AppleLoginUtility {

    public static final String ISSUER_CONTENT = "https://appleid.apple.com";

    private final AppleLoginAPIClient appleLoginAPIClient;

    public AppleLoginUtility(AppleLoginAPIClient appleLoginAPIClient) {
        this.appleLoginAPIClient = appleLoginAPIClient;
    }

    public VerifyUserResult verifyUser(String code) {
        AppleLoginAPIClient.AuthKeys authKeys;
        try {
            authKeys = appleLoginAPIClient.authKeys();
        } catch (Throwable e) {
            log.fatal("io.github.opensabe.apple.AppleLoginUtility.verifyUser [fetch auth keys fail] ", e);
            return VerifyUserResult.builder().error("fetch auth keys fail").build();
        }
        AppleLoginAPIClient.TokenResponse tokenResponse;
        try {
            tokenResponse = appleLoginAPIClient.authToken(code);
            String error = tokenResponse.getError();
            if (Objects.nonNull(error)) {
                log.fatal("io.github.opensabe.apple.AppleLoginUtility.verifyUser [code auth token fail] ");
                return VerifyUserResult.builder().error(error + " : " + tokenResponse.getError_description()).build();
            }
        } catch (Throwable e) {
            log.fatal("io.github.opensabe.apple.AppleLoginUtility.verifyUser [code auth token fail] ", e);
            return VerifyUserResult.builder().error("code auth token fail").build();
        }

        String idToken = tokenResponse.getId_token();
        DecodedJWT decode = JWT.decode(idToken);
        // Verify the JWS E256 signature using the server’s public key
        String keyId = decode.getKeyId();
        Optional<AppleLoginAPIClient.AuthKey> any = authKeys.getKeys().stream().filter(authKey -> Objects.equals(authKey.getKid(), keyId)).findAny();
        if (any.isEmpty()) {
            log.fatal("io.github.opensabe.apple.AppleLoginUtility.verifyUser [verify user id token fail key id not found]");
            return VerifyUserResult.builder().error("verify user id token fail key id not found").build();
        }
        // Verify the nonce for the authentication

        // Verify that the iss field contains https://appleid.apple.com
        String issuer = decode.getIssuer();
        if (!ISSUER_CONTENT.contains(issuer)) {
            log.fatal("io.github.opensabe.apple.AppleLoginUtility.verifyUser [verify user id token fail issuer not contain]");
            return VerifyUserResult.builder().error("verify user id token fail issuer not contain").build();
        }

        // Verify that the aud field is the developer’s client_id
        List<String> audience = decode.getAudience();
        if (!audience.contains(this.appleLoginAPIClient.getClientId())) {
            log.fatal("io.github.opensabe.apple.AppleLoginUtility.verifyUser [verify user id token fail client not equal]");
            return VerifyUserResult.builder().error("verify user id token fail client not equal").build();
        }

        // Verify that the time is earlier than the exp value of the token
        Date now = new Date();
        Date expiresAt = decode.getExpiresAt();
        if (now.after(expiresAt)) {
            log.fatal("io.github.opensabe.apple.AppleLoginUtility.verifyUser [verify user id token fail expired]");
            return VerifyUserResult.builder().error("verify user id token fail client expired").build();
        }

        String subject = decode.getSubject();
        Claim emailClaim = decode.getClaims().get("email");
        String email = Objects.nonNull(emailClaim) ? emailClaim.asString() : "";
        return VerifyUserResult.builder().sub(subject).email(email).build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyUserResult {

        private String sub;

        private String email;

        private String error;
    }

    public enum AppleLoginPlafromEnum {
        WEB, IOS
    }
}
