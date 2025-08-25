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

import cn.hutool.crypto.digest.MD5;
import io.github.opensabe.common.utils.json.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Objects;

public class ThirdPartySignatureUtil {

    private static final char split = '|';

    private static String generateRequestBody(Object requestBody){
        if(Objects.isNull(requestBody)){
            return null;
        }
        if (requestBody instanceof String) {
            var str = (String)requestBody;
            if (str.trim().startsWith("[") || str.trim().startsWith("{")){
                return MD5.create().digestHex(JsonUtil.toJSONBytes(str));
            }else {
                return MD5Util.md5WithoutSalt(str);
            }
        }else if (requestBody instanceof byte[]){
            return MD5.create().digestHex((byte[]) requestBody);
        }
        return MD5.create().digestHex(JsonUtil.toJSONBytes(requestBody));
    }

    public static String generateSignature(String hashKey, String method, Object requestBody) {
        String result = generateRequestBody(requestBody);
        if (StringUtils.isEmpty(result)) {
            result = hashKey + split + method + split;
        } else {
            result = hashKey + split + method + split + result;
        }
        return MD5Util.md5WithoutSalt(result);
    }

    public static String generateThirdPartySignature(String hashKey, String method, String requestBody) {
        if (StringUtils.isEmpty(requestBody)) {
            requestBody = hashKey + split + method + split;
        } else {
            requestBody = hashKey + split + method + split + requestBody;
        }
        return MD5Util.md5WithoutSalt(requestBody);
    }

    public static boolean verifySignature(String requestSignature, String message) {
        return StringUtils.equals(message, requestSignature);
    }

    public static void main(String[] args) {
        var m1 = new HashMap<>();
        var m2 = new HashMap<>();
        m1.put("a",1);
        m1.put("b",3);
        m2.put("b",3);
        m2.put("a",1);
        m1.put("c",m2);
        m2.put("c",m1);
        System.out.println(generateRequestBody(m1));
        System.out.println(generateRequestBody(m2));
    }

}
