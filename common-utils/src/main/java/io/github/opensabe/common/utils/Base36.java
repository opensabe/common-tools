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
/**
 *
 */
package io.github.opensabe.common.utils;

import java.math.BigInteger;

/**
 * @author wangshuli
 *
 */
public class Base36 {
    static final char[] ALPHABET =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                    'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    static final char[] ALPHABET_WITHOUT_IO =
            {'2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q',
                    'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    static final int[] INVERTED_ALPHABET;

    static final int[] INVERTED_ALPHABET_WIHTOUT_IO;

    static final String initStr = "00000000000000000000000000000000000000000000000000000000000000000000";

    static final String initStrWithoutIO = "22222222222222222222222222222222222222222222222222222222222222222222";
    static final BigInteger jinzhi = new BigInteger("36");
    static final BigInteger jinzhiWithoutIO = new BigInteger("31");

    static {
        INVERTED_ALPHABET = new int[128];
        for (int i = 0; i < 128; i++) {
            INVERTED_ALPHABET[i] = -1;
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            INVERTED_ALPHABET[i] = (i - 'A' + 10);
        }
        for (int i = '0'; i <= '9'; i++) {
            INVERTED_ALPHABET[i] = (i - '0');
        }
        //test inverted_alphabet mapping....
        //		for(int i = 0; i < 128; i ++){
        //			int k = INVERTED_ALPHABET[i];
        //			if(k == -1) continue;
        //			System.out.println(ALPHABET[k] + ":" + (char)i);
        //		}
    }

    static {
        INVERTED_ALPHABET_WIHTOUT_IO = new int[128];
        for (int i = 0; i < 128; i++) {
            INVERTED_ALPHABET_WIHTOUT_IO[i] = -1;
        }
        for (int i = 0; i < ALPHABET_WITHOUT_IO.length; i++) {
            INVERTED_ALPHABET_WIHTOUT_IO[ALPHABET_WITHOUT_IO[i]] = i;
        }
        //test inverted_alphabet mapping....
        //		for(int i = 0; i < 128; i ++){
        //			int k = INVERTED_ALPHABET[i];
        //			if(k == -1) continue;
        //			System.out.println(ALPHABET[k] + ":" + (char)i);
        //		}
    }

    public static String encode(String hexStr) {
        StringBuffer sb = new StringBuffer();
        BigInteger bi = new BigInteger(hexStr, 16);
        BigInteger d = bi;
        BigInteger m = BigInteger.ZERO;
        while (!BigInteger.ZERO.equals(d)) {
            m = d.mod(Base36.jinzhi);
            d = d.divide(Base36.jinzhi);
            sb.insert(0, (char) ALPHABET[m.intValue()]);
            //System.out.println("+" + (char) ALPHABET[m.intValue()]);
        }
        return sb.toString();
    }

    public static String encodeWithoutIO(String hexStr) {
        StringBuffer sb = new StringBuffer();
        BigInteger bi = new BigInteger(hexStr, 16);
        BigInteger d = bi;
        BigInteger m = BigInteger.ZERO;
        while (!BigInteger.ZERO.equals(d)) {
            m = d.mod(Base36.jinzhiWithoutIO);
            d = d.divide(Base36.jinzhiWithoutIO);
            sb.insert(0, (char) ALPHABET_WITHOUT_IO[m.intValue()]);
            //System.out.println("+" + (char) ALPHABET[m.intValue()]);
        }
        return sb.toString();
    }

    public static String encode(String hexStr, int length) {
        return encode(hexStr, length, true);
    }

    public static String encode(String hexStr, int length, boolean withoutIO) {
        if (length > 64) {
            throw new RuntimeException("参数错误（length不能超过64）:param length = " + length);
        }
        String str;
        if (withoutIO) {
            str = encodeWithoutIO(hexStr);
        } else {
            str = encode(hexStr);
        }
        if (length > str.length()) {
            if (withoutIO) {
                str = initStrWithoutIO.concat(str);
            } else {
                str = initStr.concat(str);
            }
            str = str.substring(str.length() - length);
        } else {
            str = str.substring(str.length() - length);
        }
        return str;
    }

    public static String decode(String base36Str) {
        if (base36Str == null || !isValidBase36Str(base36Str)) {
            throw new NumberFormatException("base36字符串格式错误:" + base36Str);
        }
        base36Str = base36Str.toUpperCase();
        BigInteger bi = BigInteger.ZERO;
        char[] chars = base36Str.toCharArray();
        for (int i = 0; i < base36Str.length(); i++) {
            char c = chars[i];
            int n = INVERTED_ALPHABET[c];
            bi = bi.multiply(jinzhi).add(new BigInteger("" + n));
        }
        return bi.toString(16);
    }

    public static String decodeWithoutIO(String base31Str) {
        if (base31Str == null || !isValidBase31Str(base31Str)) {
            throw new NumberFormatException("base36限制字符串（去IOL10）格式错误:" + base31Str);
        }
        base31Str = base31Str.toUpperCase();
        BigInteger bi = BigInteger.ZERO;
        char[] chars = base31Str.toCharArray();
        for (int i = 0; i < base31Str.length(); i++) {
            char c = chars[i];
            int n = INVERTED_ALPHABET_WIHTOUT_IO[c];
            bi = bi.multiply(jinzhiWithoutIO).add(new BigInteger("" + n));
        }
        return bi.toString(16);
    }

    public static boolean isValidBase36Str(String str) {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!isValidBase36Char(chars[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidBase36Char(char c) {
        if ((c < 0) || (c >= 128)) {
            return false;
        } else if (INVERTED_ALPHABET[c] == -1) {
            return false;
        }
        return true;
    }

    public static boolean isValidBase31Str(String str) {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!isValidBase31Char(chars[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidBase31Char(char c) {
        if ((c < 0) || (c >= 128)) {
            return false;
        } else if (INVERTED_ALPHABET_WIHTOUT_IO[c] == -1) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        String ori = "91a6631029ce0aaad";
        String test = Base36.encode(ori, 20);
        String des = Base36.decode(test);
        System.out.println(ori + " " + test + " " + des);

    }

}
