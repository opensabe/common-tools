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
package io.github.opensabe.spring.cloud.parent.common.secret;


import io.github.opensabe.common.secret.Decryptor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 聚合多路 {@link io.github.opensabe.common.secret.Decryptor} 的复合解密器。
 * <p>
 * 按 {@link org.springframework.core.Ordered} 顺序依次尝试 SPI 解密器，
 * 全部失败后回退内置 AES-CBC / AES-ECB 实现。
 *
 * @author maheng
 */
@Log4j2
public class CompositeDecryptor implements Decryptor {


    /** 按 Order 排序的 SPI 解密器列表。 */
    private final List<Decryptor> decrypters;

    /**
     * 构造复合解密器。
     *
     * @param decrypters Spring 注入的解密器列表，可为空
     */
    public CompositeDecryptor(List<Decryptor> decrypters) {
        if (decrypters == null || decrypters.isEmpty()) {
            this.decrypters = new ArrayList<>(0);
        }else {
            this.decrypters = decrypters;
            AnnotationAwareOrderComparator.sort(this.decrypters);
        }
    }

    /**
     * 依次尝试 SPI 解密器，失败后使用内置 AES 算法。
     *
     * @param encrypted 密文
     * @param cipher 密钥或算法标识
     * @return 明文
     */
    @Override
    public String decrypt(String encrypted, String cipher) {
        String result = null;
        //优先使用自定义的算法解密
        for (Decryptor decryptor : decrypters) {
            try {
                result = decryptor.decrypt(encrypted, cipher);
            }catch (Exception e) {
                log.info("Decryptor: {} decrypt error: {}", decryptor.getClass().getName(), e.getMessage());
            }
            if (StringUtils.isNotBlank(result)) {
                return result;
            }
        }

        //如果自定义的算法解密失败，就是用默认的
        try {
            if (cipher.length() == 24) {
                result = AESCBCDecryptor.decrypt(encrypted, cipher);
            }else {
                result = AESECBDecryptor.decrypt(encrypted, cipher);
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }


    /** AES/CBC/PKCS5Padding 解密器（密钥为 Base64）。 */
    public static class AESCBCDecryptor {
        private static final String AES_CBC = "AES/CBC/PKCS5Padding";

        /**
         * 解密 Base64 密文（前 16 字节为 IV）。
         *
         * @param base64 Base64 密文
         * @param keyString Base64 编码密钥
         * @return 明文
         * @throws Exception 解密失败
         */
        public static String decrypt(String base64, String keyString) throws Exception {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] raw = decoder.decode(keyString);
            SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(AES_CBC);
            byte[] encrypted = decoder.decode(base64);
            byte[] ivByte = new byte[16];
            System.arraycopy(encrypted, 0, ivByte, 0, 16);
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, new IvParameterSpec(ivByte));
            byte[] decrypted = cipher.doFinal(encrypted, 16, encrypted.length - 16);
            return new String(decrypted, StandardCharsets.UTF_8);
        }

    }


    /** AES/ECB/PKCS5Padding 解密器。 */
    public static class AESECBDecryptor {

        private static final String AES_ECB = "AES/ECB/PKCS5Padding";
        private static final int AES_KEY_LENGTH = 16;

        /**
         * 解密原始密文字节。
         *
         * @param cipherBytes 密文
         * @param keyString UTF-8 密钥（截断或填充至 16 字节）
         * @return 明文
         * @throws Exception 解密失败
         */
        public static String decrypt(byte[] cipherBytes, String keyString) throws Exception {
            byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length > AES_KEY_LENGTH) {
                byte[] truncated = new byte[AES_KEY_LENGTH];
                System.arraycopy(keyBytes, 0, truncated, 0, AES_KEY_LENGTH);
                keyBytes = truncated;
            } else if (keyBytes.length < AES_KEY_LENGTH) {
                byte[] padded = new byte[AES_KEY_LENGTH];
                System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
                keyBytes = padded;
            }
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(AES_ECB);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        }

        /**
         * 解密 Base64 编码密文。
         *
         * @param base64Cipher Base64 密文
         * @param keyString UTF-8 密钥
         * @return 明文
         * @throws Exception 解密失败
         */
        public static String decrypt(String base64Cipher, String keyString) throws Exception {
            return decrypt(Base64.getDecoder().decode(base64Cipher), keyString);
        }

    }
}
