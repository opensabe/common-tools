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

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class AppleLoginClientSecretAuthenticator {
    private static final String APPLE_LOGIN_AUDIENCE = "https://appleid.apple.com";
    private final ECPrivateKey signingKey;
    private String issuerId;
    private String keyId;
    private String bundleId;

    AppleLoginClientSecretAuthenticator(String issuerId, String keyId, String bundleId, String signingKey) {
        try {
            signingKey = signingKey.replace("-----BEGIN PRIVATE KEY-----", "").replaceAll("\\R+", "").replace("-----END PRIVATE KEY-----", "");
            byte[] derEncodedSigningKey = Base64.getDecoder().decode(signingKey);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(derEncodedSigningKey);
            this.signingKey = (ECPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException var8) {
            throw new AssertionError(var8);
        } catch (InvalidKeySpecException var9) {
            throw new RuntimeException(var9);
        }

        this.keyId = keyId;
        this.issuerId = issuerId;
        this.bundleId = bundleId;
    }

    public String getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(String issuerId) {
        this.issuerId = issuerId;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String generateToken() {
        Instant now = Instant.now();
        return JWT.create()
                .withAudience(APPLE_LOGIN_AUDIENCE)
                .withIssuedAt(now)
                .withExpiresAt(now.plus(ChronoUnit.MINUTES.getDuration().multipliedBy(5L)))
                .withIssuer(this.issuerId)
                .withKeyId(this.keyId)
                .withSubject(bundleId)
                .sign(Algorithm.ECDSA256(this.signingKey));
    }
}
