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
 * AES-GCM-128 工具类（128位密钥，硬件加速，内网专用）
 * @author maheng
 */

public class AesGcm128Util {

//    private static volatile byte[] PSK = HexFormat.of().parseHex("7f2d189a3e5c0b678a1d0f2e3c4b5a69");
    private static volatile byte[] PSK = null;

    // 算法标识
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    // GCM 标签长度（bits）= 16字节
    private static final int GCM_TAG_LENGTH = 16 * 8;
    // 128位密钥长度（字节）
    public static final int TAG_LENGTH = 16;
    // GCM 推荐 Nonce 长度（字节）
    public static final int NONCE_LENGTH = 12;


    /**
     * 设置共享PSK
     */
    public static synchronized void setPSK(byte[] psk) {
        if (psk == null || psk.length != 16) {
            throw new IllegalArgumentException("Invalid PSK length: " + psk.length);
        }
        PSK = psk.clone();
    }

    /**
     * 生成 128位 随机密钥（用于生成PSK/业务密钥）
     */
    public static byte[] generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(128, new SecureRandom());
        return keyGen.generateKey().getEncoded();
    }

    /**
     * 生成随机 Nonce
     */
    public static byte[] generateNonce() {
        byte[] nonce = new byte[NONCE_LENGTH];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    /**
     * AES-GCM-128 加密
     * @param psk 预共享密钥（16字节）
     * @param nonce 随机数（12字节）
     * @param plainData 明文数据
     * @return 密文 = 加密数据 + Tag
     */
    public static byte[] encrypt(byte[] psk, byte[] nonce, byte[] plainData) throws Exception {
        SecretKey secretKey = new SecretKeySpec(psk, ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
        return cipher.doFinal(plainData);
    }

    /**
     * AES-GCM-128 解密
     * @param psk 预共享密钥（16字节）
     * @param nonce 随机数（12字节）
     * @param encryptedData 密文 = 加密数据 + Tag
     * @return 明文数据
     */
    public static byte[] decrypt(byte[] psk, byte[] nonce, byte[] encryptedData) throws Exception {
        SecretKey secretKey = new SecretKeySpec(psk, ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        return cipher.doFinal(encryptedData);
    }


    /**
     * 解析服务端返回的base64字符串
     * @param base64 加密后的base64
     * @return [业务密钥, MySQL密文]
     */
    public static byte[] decryptBase64(byte[] psk, String base64) throws Exception {
        Assert.notNull(psk, "psk cannot be null");
        // 1.  base64解码
        byte[] data = Base64.getDecoder().decode(base64);

        if (data.length < NONCE_LENGTH + TAG_LENGTH) {
            throw new IllegalArgumentException("Invalid AES-GCM payload");
        }

        // 2. 固定拆分：Nonce(12) + Tag(16) + 密文体
        byte[] nonce = new byte[NONCE_LENGTH];
        byte[] payload = new byte[data.length - NONCE_LENGTH];

        int encryptLength = payload.length - TAG_LENGTH;

        System.arraycopy(data, 0, nonce, 0, NONCE_LENGTH);
        System.arraycopy(data, NONCE_LENGTH+TAG_LENGTH, payload, 0, encryptLength);
        System.arraycopy(data, NONCE_LENGTH, payload,  encryptLength, TAG_LENGTH);

        // 4. AES-GCM-128 解密
        return decrypt(psk, nonce, payload);
    }
    public static byte[] decryptBase64(String base64) throws Exception {
        return decryptBase64(PSK, base64);
    }

    /**
     * AES-GCM-128 加密，将字节转为base64字符串
     * @param psk 预共享密钥（16字节）
     * @param nonce 随机数（12字节）
     * @param plainData 明文数据
     * @return 密文 = [12字节 nonce] + [16字节 tag] + [加密后的payload]
     */
    public static String encryptToBase64(byte[] psk, byte[] nonce, byte[] plainData) throws Exception {
        Assert.notNull(psk, "psk cannot be null");
        if (nonce.length != NONCE_LENGTH) {
            throw new IllegalArgumentException("Invalid nonce");
        }
        //[加密后的 payload] + [16字节 tag]
        byte[] encryptedWithTag = encrypt(psk, nonce, plainData);

        //[12字节 nonce] + [16字节 tag] + [加密后的payload]
        byte[] result = new byte[nonce.length + encryptedWithTag.length];

        int payloadLength = encryptedWithTag.length - TAG_LENGTH;

        System.arraycopy(nonce, 0, result, 0, nonce.length);
        System.arraycopy(encryptedWithTag, payloadLength, result, nonce.length, TAG_LENGTH);
        System.arraycopy(encryptedWithTag, 0, result, TAG_LENGTH + NONCE_LENGTH, payloadLength);

        // 6. 转base64
        return Base64.getEncoder().encodeToString(result);
    }
    public static String encryptToBase64(byte[] nonce, byte[] plainData) throws Exception {
        return encryptToBase64(PSK, nonce, plainData);
    }
    public static String encryptToBase64(byte[] plainData) throws Exception {
        return encryptToBase64(PSK, generateNonce(), plainData);
    }
}