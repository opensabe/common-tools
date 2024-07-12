package io.github.opensabe.common.utils;

import org.apache.commons.codec.binary.Base64;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * AES 加密工具类
 *
 * @author houpu
 * @date 2017/8/17 13:20
 */
public class AESUtil {

//    private static final Logger log = LoggerFactory.getLogger(AESUtil.class);

    private static final String SHA1PRNG = "SHA1PRNG";   // SHA1PRNG 强随机种子算法, 要区别4.2以上版本的调用方法
    private static final String KEY_ALGORITHM = "AES";   //AES 加密
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";   //默认的加密算法


    /**
     * 加密
     *
     * @param content
     * @param key
     * @return
     */
    public static String encrypt(String content, String key) throws Exception {
        return encrypt(content, key, DEFAULT_CIPHER_ALGORITHM);
    }
    public static String encryptNodeJS(String content, String key) throws Exception {
        return encrypt(content, key, "AES/ECB/PKCS5Padding");
    }

    public static String encrypt(String content, String key, String cipherAlgorithm) throws Exception {
        byte[] raw = Base64.decodeBase64(key);
        SecretKeySpec sKeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        SecureRandom r = new SecureRandom();
        byte[] ivBytes = new byte[16];
        r.nextBytes(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, new IvParameterSpec(ivBytes));
        byte[] encrypted = cipher.doFinal(content.getBytes("utf-8"));
        return Base64.encodeBase64String(byteMerger(ivBytes, encrypted));
    }

    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /**
     * 解密
     *
     * @param base64Encrypted
     * @param key
     * @return
     */
    public static String decrypt(String base64Encrypted, String key, String cipherAlgorithm) throws Exception {
        byte[] raw = Base64.decodeBase64(key);
        SecretKeySpec sKeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);

        byte[] encrypted = Base64.decodeBase64(base64Encrypted);
        byte[] ivByte = new byte[16];
        System.arraycopy(encrypted, 0, ivByte, 0, 16);
        cipher.init(Cipher.DECRYPT_MODE, sKeySpec, new IvParameterSpec(ivByte));
        byte[] decrypted = cipher.doFinal(encrypted, 16, encrypted.length - 16);
        return new String(decrypted, "utf-8");
    }

    public static String decryptNodeJS(String base64Encrypted, String key) throws Exception {
        return decrypt(base64Encrypted, key, "AES/ECB/PKCS5Padding");
    }
    public static String decrypt(String base64Encrypted, String key) throws Exception {
        return decrypt(base64Encrypted, key, DEFAULT_CIPHER_ALGORITHM);
    }

        /**
         * 对密钥进行处理
         *
         * @param seed
         * @return
         * @throws Exception
         */
    public static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance(KEY_ALGORITHM);
        //for android
        SecureRandom sr = null;
        sr = SecureRandom.getInstance(SHA1PRNG);
        sr.setSeed(seed);
        kgen.init(128, sr); //256 bits or 128 bits,192bits
        //AES中128位密钥版本有10个加密循环，192比特密钥版本有12个加密循环，256比特密钥版本则有14个加密循环。
        SecretKey sKey = kgen.generateKey();
        byte[] raw = sKey.getEncoded();
        return raw;
    }
}

