package io.github.opensabe.common.utils;

import cn.hutool.core.util.ZipUtil;
import com.google.common.base.Charsets;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Gzip 工具类，如果数据库中字段过长，则需要压缩
 */
public class GzipUtil {

    private static final Charset default_charset = Charsets.UTF_8;

    /**
     * 先将字符串用gzip压缩, 然后再base64编码为字符串
     * @param source
     * @return
     */
    public static String zipThenBase64Enc(String source) {
        byte[] bytes = ZipUtil.gzip(source, default_charset.name());
        return new String(Base64.getEncoder().encode(bytes), default_charset);
    }

    /**
     * 先 Base64 解码，之后将字节码解压, 再将解压后的字节码转为字符串
     * @param source
     * @return
     */
    public static String base64DecThenUnzip(String source) {
        byte[] decode = Base64.getDecoder().decode(source);
        byte[] bytes = ZipUtil.unGzip(decode);
        return new String(bytes, default_charset);
    }

    public static String unzipped(String value) {
        if (StringUtils.isNotBlank(value)) {
            if (value.equalsIgnoreCase("null")) {
                return null;
            }
            if (value.charAt(0) != '{' && value.charAt(0) != '[') {
                value = GzipUtil.base64DecThenUnzip(value);
            }
        }
        return value;
    }
}
