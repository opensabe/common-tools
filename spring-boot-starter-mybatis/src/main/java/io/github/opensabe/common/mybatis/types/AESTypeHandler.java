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

import org.springframework.util.StringUtils;

import io.github.opensabe.common.mybatis.plugins.CryptTypeHandler;
import lombok.extern.log4j.Log4j2;

/**
 * AESTypeHandler。
 */
@Log4j2
public class AESTypeHandler extends CryptTypeHandler {

    private static ThreadLocal<String> keyHolder = new ThreadLocal<String>();

    /**
     * @return key
     */
    public static String getKey() {
        return keyHolder.get();
    }

    /**
     * @param key 待设置值
     */
    public static void setKey(String key) {
        keyHolder.set(key);
    }

    /** clearKey。 */
    public static void clearKey() {
        keyHolder.remove();
    }

    /** {@inheritDoc} */
    @Override
    protected String encrypt(String origin) {
        var key = getKey();
        if (StringUtils.hasText(key)) {
            try {
                return AESUtil.encryptIv(origin, key);
            } catch (Throwable e) {
                log.error(e);
            } finally {
                clearKey();
            }
        }
        return origin;
    }

    /** {@inheritDoc} */
    @Override
    protected String decrypt(String origin) {
        var key = getKey();
        if (StringUtils.hasText(key)) {
            try {
                return AESUtil.decryptIv(origin, key);
            } catch (Throwable e) {
                log.error(e);
            } finally {
                clearKey();
            }
        }
        return origin;
    }

}
