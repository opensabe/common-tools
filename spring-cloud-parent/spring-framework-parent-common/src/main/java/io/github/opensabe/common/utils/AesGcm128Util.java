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
package io.github.opensabe.common.utils;


import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM-128 加解密工具（128 位密钥，内网场景）。
 * <p>
 * 线格式：Base64 编码的 {@code nonce(12) + tag(16) + ciphertext}；{@link #PSK} 可通过 {@link #setPSK(byte[])} 注入全局密钥。
 */
public class AesGcm128Util {

    /** 全局预共享密钥（16 字节），由 {@link #setPSK(byte[])} 设置。 */
    private static volatile byte[] PSK = null;

    /** JCA 算法名。 */
    private static final String ALGORITHM = "AES";
    /** 变换字符串：AES/GCM/NoPadding。 */
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    /** GCM 认证标签位长（128 bit = 16 字节）。 */
    private static final int GCM_TAG_LENGTH = 16 * 8;
    /** GCM 认证标签字节长度。 */
    public static final int TAG_LENGTH = 16;
    /** GCM 推荐 Nonce 字节长度。 */
    public static final int NONCE_LENGTH = 12;

    /**
     * 设置全局预共享密钥（须为 16 字节）。
     *
     * @param psk 128 位 PSK
     */
    public static synchronized void setPSK(byte[] psk) {
        if (psk == null || psk.length != 16) {
            throw new IllegalArgumentException("Invalid PSK length: " + psk.length);
        }
        PSK = psk.clone();
    }

    /**
     * 生成 128 位随机 AES 密钥字节。
     *
     * @return 16 字节密钥
     * @throws Exception JCA 初始化失败时
     */
    public static byte[] generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(128, new SecureRandom());
        return keyGen.generateKey().getEncoded();
    }

    /**
     * 生成 {@link #NONCE_LENGTH} 字节随机 Nonce。
     *
     * @return Nonce 字节数组
     */
    public static byte[] generateNonce() {
        byte[] nonce = new byte[NONCE_LENGTH];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    /**
     * AES-GCM-128 加密。
     *
     * @param psk       预共享密钥（16 字节）
     * @param nonce     随机数（12 字节）
     * @param plainData 明文
     * @return 密文（payload + GCM tag）
     * @throws Exception 加解密引擎异常
     */
    public static byte[] encrypt(byte[] psk, byte[] nonce, byte[] plainData) throws Exception {
        SecretKey secretKey = new SecretKeySpec(psk, ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
        return cipher.doFinal(plainData);
    }

    /**
     * AES-GCM-128 解密。
     *
     * @param psk           预共享密钥（16 字节）
     * @param nonce         随机数（12 字节）
     * @param encryptedData 密文（payload + GCM tag）
     * @return 明文
     * @throws Exception 加解密引擎异常
     */
    public static byte[] decrypt(byte[] psk, byte[] nonce, byte[] encryptedData) throws Exception {
        SecretKey secretKey = new SecretKeySpec(psk, ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        return cipher.doFinal(encryptedData);
    }

    /**
     * 解密 Base64 线格式：{@code nonce(12) + tag(16) + ciphertext}。
     *
     * @param psk    预共享密钥（16 字节）
     * @param base64 Base64 编码密文包
     * @return 明文
     * @throws Exception 格式非法或解密失败
     */
    public static byte[] decryptBase64(byte[] psk, String base64) throws Exception {
        Assert.notNull(psk, "psk cannot be null");
        byte[] data = Base64.getDecoder().decode(base64);

        if (data.length < NONCE_LENGTH + TAG_LENGTH) {
            throw new IllegalArgumentException("Invalid AES-GCM payload");
        }

        byte[] nonce = new byte[NONCE_LENGTH];
        byte[] payload = new byte[data.length - NONCE_LENGTH];

        int encryptLength = payload.length - TAG_LENGTH;

        System.arraycopy(data, 0, nonce, 0, NONCE_LENGTH);
        System.arraycopy(data, NONCE_LENGTH+TAG_LENGTH, payload, 0, encryptLength);
        System.arraycopy(data, NONCE_LENGTH, payload,  encryptLength, TAG_LENGTH);

        return decrypt(psk, nonce, payload);
    }

    /**
     * 使用全局 {@link #PSK} 解密 Base64 密文包。
     *
     * @param base64 Base64 编码密文包
     * @return 明文
     * @throws Exception 格式非法或解密失败
     * @see #decryptBase64(byte[], String)
     */
    public static byte[] decryptBase64(String base64) throws Exception {
        return decryptBase64(PSK, base64);
    }

    /**
     * 加密并输出 Base64 线格式：{@code nonce(12) + tag(16) + ciphertext}。
     *
     * @param psk       预共享密钥（16 字节）
     * @param nonce     随机数（12 字节）
     * @param plainData 明文
     * @return Base64 编码结果
     * @throws Exception 加解密引擎异常
     */
    public static String encryptToBase64(byte[] psk, byte[] nonce, byte[] plainData) throws Exception {
        Assert.notNull(psk, "psk cannot be null");
        if (nonce.length != NONCE_LENGTH) {
            throw new IllegalArgumentException("Invalid nonce");
        }
        byte[] encryptedWithTag = encrypt(psk, nonce, plainData);

        byte[] result = new byte[nonce.length + encryptedWithTag.length];

        int payloadLength = encryptedWithTag.length - TAG_LENGTH;

        System.arraycopy(nonce, 0, result, 0, nonce.length);
        System.arraycopy(encryptedWithTag, payloadLength, result, nonce.length, TAG_LENGTH);
        System.arraycopy(encryptedWithTag, 0, result, TAG_LENGTH + NONCE_LENGTH, payloadLength);

        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * 使用全局 {@link #PSK} 加密并输出 Base64。
     *
     * @param nonce     随机数（12 字节）
     * @param plainData 明文
     * @return Base64 编码结果
     * @throws Exception 加解密引擎异常
     * @see #encryptToBase64(byte[], byte[], byte[])
     */
    public static String encryptToBase64(byte[] nonce, byte[] plainData) throws Exception {
        return encryptToBase64(PSK, nonce, plainData);
    }

    /**
     * 使用全局 {@link #PSK} 与随机 Nonce 加密并输出 Base64。
     *
     * @param plainData 明文
     * @return Base64 编码结果
     * @throws Exception 加解密引擎异常
     * @see #encryptToBase64(byte[], byte[], byte[])
     */
    public static String encryptToBase64(byte[] plainData) throws Exception {
        return encryptToBase64(PSK, generateNonce(), plainData);
    }
}
