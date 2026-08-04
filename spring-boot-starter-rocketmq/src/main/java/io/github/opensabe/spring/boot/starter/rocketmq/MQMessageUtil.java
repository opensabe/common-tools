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
 * RocketMQ 消息体编解码工具：超大消息 Gzip+Base64 压缩，并提供日志截断。
 */
@Slf4j
public class MQMessageUtil {

    /** 单条消息最大字节数（1MB）。 */
    private static final int MAX_MESSAGE_SIZE = 1024 * 1024;
    /** 压缩消息前缀标记。 */
    private static final String COMPRESSED_PREFIX = "compressed.";

    /**
     * 编码字符串消息；超过 {@link #MAX_MESSAGE_SIZE} 时压缩并加前缀。
     *
     * @param message 原始消息
     * @return 编码后的消息
     */
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
     * 编码 {@link BaseMQMessage} 的 {@code data} 字段。
     *
     * @param message 待编码信封
     * @return 同一信封实例（{@code data} 已替换）
     */
    public static BaseMQMessage encode(BaseMQMessage message) {
        message.setData(encode(message.getData()));
        return message;
    }

    /**
     * 解码字符串消息；带 {@link #COMPRESSED_PREFIX} 前缀时解压。
     *
     * @param message 编码消息
     * @return 解码后的明文
     */
    public static String decode(String message) {
        if (message == null || !message.startsWith(COMPRESSED_PREFIX)) {
            return message;
        }

        return GzipUtil.base64DecThenUnzip(message.substring(COMPRESSED_PREFIX.length()));
    }

    /**
     * 解码 {@link BaseMQMessage} 的 {@code data} 字段。
     *
     * @param message 待解码信封
     * @return 同一信封实例（{@code data} 已替换）
     */
    public static BaseMQMessage decode(BaseMQMessage message) {
        message.setData(decode(message.getData()));
        return message;
    }

    /**
     * 截断过长消息体用于日志输出（保留首尾各 1000 字符）。
     *
     * @param body 原始消息体
     * @return 截断后的文本，{@code null} 输入返回 {@code null}
     */
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
