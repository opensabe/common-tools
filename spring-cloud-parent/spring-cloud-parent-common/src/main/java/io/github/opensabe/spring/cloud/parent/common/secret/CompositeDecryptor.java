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
 *
 * 聚合所有的解密算法，根据order顺序，依次解密
 * @author maheng
 */
public class CompositeDecryptor implements Decryptor {


    private List<Decryptor> decrypters;

    /**
     * 使用 spring spi 加载解密算法
     * @param decrypters spi中加载的解密算法
     */
    public CompositeDecryptor(List<Decryptor> decrypters) {
        if (decrypters == null || decrypters.isEmpty()) {
            this.decrypters = new ArrayList<>(0);
        }else {
            this.decrypters = decrypters;
            AnnotationAwareOrderComparator.sort(this.decrypters);
        }
    }

    @Override
    public String decrypt(String base64, String cipher) {
        String result;
        //优先使用自定义的算法解密
        for (Decryptor decryptor : decrypters) {
            result = decryptor.decrypt(base64, cipher);
            if (StringUtils.isNotBlank(result)) {
                return result;
            }
        }

        //如果自定义的算法解密失败，就是用默认的
        try {
            if (cipher.length() == 24) {
                result = AESCBCDecryptor.decrypt(base64, cipher);
            }else {
                result = AESECBDecryptor.decryptBase64(base64, cipher);
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }


    public static class AESCBCDecryptor {
        private static final String AES_ECB = "AES/CBC/PKCS5Padding";

        public static String decrypt(String base64, String keyString) throws Exception {
            Base64.Decoder decoder = Base64.getDecoder();

            byte[] keyBytes = decoder.decode(keyString);
            byte[] bytes = decoder.decode(base64);

            byte[] ivBytes = new byte[16];
            System.arraycopy(bytes, 0, ivBytes, 0, 16);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(AES_ECB);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] plain = cipher.doFinal(bytes, 16, bytes.length - 16);
            return new String(plain, StandardCharsets.UTF_8);
        }

    }


    public static class AESECBDecryptor {

        private static final String AES_ECB = "AES/ECB/PKCS5Padding";
        private static final int AES_KEY_LENGTH = 16;

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
         * 密文为 Base64 字符串时（例如从别处导出为 Base64）
         */
        public static String decryptBase64(String base64Cipher, String keyString) throws Exception {
            return decrypt(Base64.getDecoder().decode(base64Cipher), keyString);
        }

        /**
         * 密文为 HEX 字符串时（例如 SELECT HEX(AES_ENCRYPT('test','aa'))）
         */
        public static String decryptHex(String hexCipher, String keyString) throws Exception {
            return decrypt(hexToBytes(hexCipher), keyString);
        }

        private static byte[] hexToBytes(String hex) {
            int len = hex.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i + 1), 16));
            }
            return data;
        }

    }
}
