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
package io.github.opensabe.common.web.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.alibaba.fastjson.util.IOUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RevoFastJsonHttpMessageConverter extends FastJsonHttpMessageConverter {

    private final static ThreadLocal<byte[]> bytesLocal = new ThreadLocal<byte[]>();

    private static byte[] allocateBytes(int length) {
        byte[] chars = bytesLocal.get();

        if (chars == null) {
            if (length <= 1024 * 64) {
                chars = new byte[1024 * 64];
                bytesLocal.set(chars);
            } else {
                chars = new byte[length];
            }
        } else if (chars.length < length) {
            chars = new byte[length];
        }

        return chars;
    }

    @Override
    protected Object readInternal(Class<?> clazz,
                                  HttpInputMessage inputMessage
    ) throws IOException, HttpMessageNotReadableException {
        return revoReadType(getType(clazz, null), inputMessage);
    }

    @Override
    public Object read(Type type,
                       Class<?> contextClass,
                       HttpInputMessage inputMessage
    ) throws IOException, HttpMessageNotReadableException {
        return revoReadType(getType(type, contextClass), inputMessage);
    }

    /**
     * 兼容openapi返回byte数组
     *
     * @param clazz the class to test for support
     * @return
     */
    @Override
    protected boolean supports(Class<?> clazz) {
        return !(Objects.nonNull(clazz) && clazz.isArray() && clazz.componentType().isAssignableFrom(byte.class));
    }

    private Object revoReadType(Type type, HttpInputMessage inputMessage) {
        byte[] bytes = null;
        int offset = 0;
        try {
            InputStream in = inputMessage.getBody();
            Charset cs = getFastJsonConfig().getCharset();
            if (cs == null) {
                cs = IOUtils.UTF8;
            }

            bytes = allocateBytes(1024 * 64);
            for (; ; ) {
                int readCount = in.read(bytes, offset, bytes.length - offset);
                if (readCount == -1) {
                    break;
                }
                offset += readCount;
                if (offset == bytes.length) {
                    byte[] newBytes = new byte[bytes.length * 3 / 2];
                    System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                    bytes = newBytes;
                }
            }

            return JSON.parseObject(bytes, 0, offset,
                    getFastJsonConfig().getCharset(),
                    type,
                    getFastJsonConfig().getParserConfig(),
                    getFastJsonConfig().getParseProcess(),
                    JSON.DEFAULT_PARSER_FEATURE,
                    getFastJsonConfig().getFeatures());
        } catch (JSONException ex) {
            printInfo(bytes, offset);
            throw new HttpMessageNotReadableException("JSON parse error: " + ex.getMessage(), ex, inputMessage);
        } catch (IOException ex) {
            printInfo(bytes, offset);
            throw new HttpMessageNotReadableException("I/O error while reading input message", ex, inputMessage);
        }
    }

    private void printInfo(byte[] bytes, int offset) {
        try {
            log.warn("HttpInputMessage body can't be parsed to JSON: {}", bytes != null ? new String(ArrayUtils.subarray(bytes, 0, offset)) : "");
        } catch (Throwable e) {
            log.error("HttpInputMessage body can't be read!", e);
        }
    }
}
