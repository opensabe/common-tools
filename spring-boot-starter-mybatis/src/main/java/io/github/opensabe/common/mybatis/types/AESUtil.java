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
package io.github.opensabe.common.mybatis.types;

import io.github.opensabe.common.utils.Base64;
import lombok.extern.log4j.Log4j2;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author musaxi on 2017/11/2.
 */
@Log4j2
public class AESUtil {

    /**
     * AES加密（默认IV）
     */
    public static String Encrypt(String plain, String key) throws Exception {
        if(key == null) {
            System.out.print("Key为空null");
            return null;
        } else if(key.length() != 16) {
            System.out.print("Key长度不是16位");
            return null;
        } else {
            byte[] raw = key.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec("8070605040302010".getBytes());
            cipher.init(1, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(plain.getBytes());
            return Base64.encodeS(encrypted);
        }
    }

    /**
     * AES解密（默认IV）
     */
    public static String Decrypt(String plain, String key) throws Exception {
        try {
            if(key == null) {
                log.error("Key为空null");
                return null;
            } else if(key.length() != 16) {
                log.error("Key长度不是16位");
                return null;
            } else {
                byte[] raw = key.getBytes("ASCII");
                SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                IvParameterSpec iv = new IvParameterSpec("8070605040302010".getBytes());
                cipher.init(2, skeySpec, iv);
                byte[] encrypted1 = Base64.decode(plain);

                try {
                    byte[] original = cipher.doFinal(encrypted1);
                    String originalString = new String(original);
                    return originalString;
                } catch (Throwable var9) {
                    log.error("解密异常，key：" + key, var9);
                    return null;
                }
            }
        } catch (Throwable var10) {
            log.error("解密异常，plain=" + plain + ",key=" + key, var10);
            return null;
        }
    }

    /**
     * AES加密
     */
    public static byte[] encrypt(String plain, String key) throws Exception {
        if (plain == null || key == null) return null;
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec secret = new SecretKeySpec(key.getBytes("utf-8"), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        return cipher.doFinal(plain.getBytes("utf-8"));
    }

    /**
     * AES加密（附加额外iv）
     */
    public static byte[] encrypt(String plain, String key, String iv) throws Exception {
        if (plain == null || key == null || iv == null) return null;
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key.getBytes("utf-8"), "AES"),
                new IvParameterSpec(iv.getBytes()));
        return cipher.doFinal(plain.getBytes("utf-8"));
    }

    /**
     * AES解密
     */
    public static byte[] decrypt(byte[] encrypted, String key) throws Exception {
        if (encrypted == null || key == null) return null;
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
        return cipher.doFinal(encrypted);
    }

    /**
     * AES解密（附加额外iv）
     */
    public static byte[] decrypt(byte[] encrypted, String key, String iv) throws Exception {
        if (encrypted == null || key == null || iv == null) return null;
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(key.getBytes("utf-8"), "AES"),
                new IvParameterSpec(iv.getBytes()));
        return cipher.doFinal(encrypted);
    }
}