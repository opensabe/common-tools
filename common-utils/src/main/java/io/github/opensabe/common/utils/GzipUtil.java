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

import java.nio.charset.Charset;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;

import cn.hutool.core.util.ZipUtil;

/**
 * Gzip 压缩与 Base64 编解码工具类。
 * <p>
 * 适用于数据库字段过长需压缩存储的场景；默认字符集为 UTF-8。
 */
public class GzipUtil {

    /**
     * 默认字符集（UTF-8）。
     */
    private static final Charset DEFAULT_CHARSET = Charsets.UTF_8;

    /**
     * 先将字符串 Gzip 压缩，再 Base64 编码为字符串。
     *
     * @param source 原始明文
     * @return Base64 编码后的压缩字符串
     */
    public static String zipThenBase64Enc(String source) {
        byte[] bytes = ZipUtil.gzip(source, DEFAULT_CHARSET.name());
        return new String(Base64.getEncoder().encode(bytes), DEFAULT_CHARSET);
    }

    /**
     * 先 Base64 解码，再 Gzip 解压为明文字符串。
     *
     * @param source Base64 编码的压缩字符串
     * @return 解压后的明文
     */
    public static String base64DecThenUnzip(String source) {
        byte[] decode = Base64.getDecoder().decode(source);
        byte[] bytes = ZipUtil.unGzip(decode);
        return new String(bytes, DEFAULT_CHARSET);
    }

    /**
     * 若值非 JSON 字面量（不以 {@code {} 或 []} 开头），则尝试 Base64 解码并 Gzip 解压。
     * <p>
     * 字面量 {@code "null"}（忽略大小写）会转为 Java {@code null}。
     *
     * @param value 可能已压缩的字符串
     * @return 解压后的明文，或原值
     */
    public static String unzipped(String value) {
        if (StringUtils.isNotBlank(value)) {
            if ("null".equalsIgnoreCase(value)) {
                return null;
            }
            if (value.charAt(0) != '{' && value.charAt(0) != '[') {
                value = GzipUtil.base64DecThenUnzip(value);
            }
        }
        return value;
    }
}
