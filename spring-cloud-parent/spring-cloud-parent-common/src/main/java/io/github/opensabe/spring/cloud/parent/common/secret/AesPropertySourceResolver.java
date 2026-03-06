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

import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.cloud.bootstrap.config.PropertySourceBootstrapConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 将 aes properties 解密
 * <p>
 *     cipher.length(固定两位数) + cipher + base64(encrypt)
 * </p>
 * @author maheng
 */
public class AesPropertySourceResolver implements ApplicationContextInitializer<ConfigurableApplicationContext>, ApplicationListener<ContextRefreshedEvent> {

    public static final String SECRET_PROPERTY_SOURCE_NAME = PropertySourceBootstrapConfiguration.BOOTSTRAP_PROPERTY_SOURCE_NAME+"-secretPropertySource";


    private static final String AES_ECB = "AES/ECB/PKCS5Padding";
    private static final int AES_KEY_LENGTH = 16;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        decrypt(applicationContext);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext() instanceof ConfigurableApplicationContext applicationContext) {
            decrypt(applicationContext);
        }
    }

    private void decrypt (ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        PropertySource<?> propertySource = mutablePropertySources.get(SECRET_PROPERTY_SOURCE_NAME);
        //这里必须跟 BootstrapPropertySource 比较，解密完替换为 MapPropertiesSource
        //如果直接跟 MapPropertiesSource 比较，会重复解密，导致报错
        if (propertySource instanceof BootstrapPropertySource<?> bootstrapPropertySource) {
            String[] names = bootstrapPropertySource.getPropertyNames();
            Map<String, Object> map = new HashMap<>(names.length);
            for (String name : names) {
                map.put(name, decryptValue(bootstrapPropertySource.getProperty(name)));
            }
            mutablePropertySources.replace(SECRET_PROPERTY_SOURCE_NAME, new MapPropertySource(SECRET_PROPERTY_SOURCE_NAME, map));
        }
    }

    private String decryptValue (Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        //AxxxMxxx
        String string = value.toString();
        int keyLength = Integer.parseInt(string.substring(0, 2));
        int index = keyLength + 2;
        String key = string.substring(2, index);
        String decrypted = string.substring(index, string.length());
        return decrypt(decrypted, key);
    }

    private String decrypt(String value, String key) {
        try {
            return decryptBase64(value, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


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

    /** 密文为 Base64 字符串时（例如从别处导出为 Base64） */
    public static String decryptBase64(String base64Cipher, String keyString) throws Exception {
        return decrypt(Base64.getDecoder().decode(base64Cipher), keyString);
    }

    /** 密文为 HEX 字符串时（例如 SELECT HEX(AES_ENCRYPT('test','aa'))） */
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
