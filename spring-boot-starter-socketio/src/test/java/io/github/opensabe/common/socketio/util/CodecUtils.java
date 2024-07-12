package io.github.opensabe.common.socketio.util;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jianing on 11/3/16.
 */
public class CodecUtils {

    public static Base64 base64 = new Base64();


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

    public static JSONObject getRegJSON(int reqId, String deviceId) {
        return getRegJSON(reqId, 1, deviceId);
    }


}
