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
package io.github.opensabe.spring.boot.starter.rocketmq;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.utils.GzipUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * A utility class to encode message
 */
@Slf4j
public class MQMessageUtil {

    private static final int MAX_MESSAGE_SIZE = 1024 * 1024; /* aka 1MB */
    private static final String COMPRESSED_PREFIX = "compressed.";

    public static String encode(String message) {
        if (message == null || message.getBytes().length < MAX_MESSAGE_SIZE) {
            return message;
        }

        log.info("MQMessageUtil-encode message is oversize [max: {}, msg size: {}], start compressing...", MAX_MESSAGE_SIZE, message.getBytes().length);

        String compressed = GzipUtil.zipThenBase64Enc(message);

        log.info("MQMessageUtil-encode compressed message size: {}", compressed.getBytes().length);

        return COMPRESSED_PREFIX + compressed;
    }

    /**
     * if message size < {@link #MAX_MESSAGE_SIZE} compress and base64-encode the message and add prefix {@link #COMPRESSED_PREFIX}
     * the encoded data: `{@link #COMPRESSED_PREFIX}{@param message}`
     *
     * @param message message to be encoded
     * @return encoded message
     */
    public static BaseMQMessage encode(BaseMQMessage message) {
        message.setData(encode(message.getData()));
        return message;
    }

    public static String decode(String message) {
        if (message == null || !message.startsWith(COMPRESSED_PREFIX)) {
            return message;
        }

        return GzipUtil.base64DecThenUnzip(message.substring(COMPRESSED_PREFIX.length()));
    }

    /**
     * if the message is prefixed with {@link #COMPRESSED_PREFIX}
     * remove the prefix and base64-decode and decompress the message
     *
     * @param message message to be decoded
     * @return decoded message
     */
    public static BaseMQMessage decode(BaseMQMessage message) {
        message.setData(decode(message.getData()));
        return message;
    }

    public static String trimBodyForLog(String body) {
        if (body == null) {
            return null;
        }
        if (body.length() <= 5000) {
            return body;
        }
        return body.substring(0, 1000) + "..." + body.substring(body.length() - 1000);
    }
}
