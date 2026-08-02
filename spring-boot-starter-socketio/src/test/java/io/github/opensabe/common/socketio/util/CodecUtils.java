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
package io.github.opensabe.common.socketio.util;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Socket.IO 集成测试消息编解码工具（注册/鉴权 JSON 构造）。
 */
public class CodecUtils {

    /** Base64 编解码器。 */
    public static Base64 base64 = new Base64();

    /**
     * 构造设备注册 JSON。
     *
     * @param reqId       请求 ID
     * @param productCode 产品码
     * @param deviceId    设备 ID
     * @return 注册消息 JSON
     */
    public static JSONObject getRegJSON(int reqId, int productCode, String deviceId) {
        try {
            JSONObject data = new JSONObject();
            data.put("requestId", reqId);
            data.put("deviceId", deviceId);
            data.put("productCode", productCode);
            data.put("devType", "WEB");
            String dataStr = data.toString();
            JSONObject ret = new JSONObject();
            ret.put("type", "reg");
            ret.put("data", dataStr);
            return ret;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构造聊天鉴权请求 JSON。
     *
     * @param reqId    请求 ID
     * @param deviceId 设备 ID
     * @param to       目标用户
     * @param authStr  鉴权串
     * @param sign     签名
     * @return 鉴权请求 JSON
     */
    public static JSONObject getChatRequestJSON(int reqId, String deviceId, String to, String authStr, String sign) {
        try {
            JSONObject data = new JSONObject();
            data.put("requestId", reqId);
            data.put("deviceId", deviceId);
            data.put("to", to);
            data.put("authStr", authStr);
            data.put("sign", sign);
            String dataStr = data.toString();
            JSONObject ret = new JSONObject();
            ret.put("type", "authReq");
            ret.put("data", dataStr);
            return ret;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构造默认 productCode=1 的注册 JSON。
     *
     * @param reqId    请求 ID
     * @param deviceId 设备 ID
     * @return 注册消息 JSON
     */
    public static JSONObject getRegJSON(int reqId, String deviceId) {
        return getRegJSON(reqId, 1, deviceId);
    }


}
