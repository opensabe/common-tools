package io.github.opensabe.spring.boot.starter.rocketmq;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.utils.GzipUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * A utility class to encode message
 */
@Slf4j
public class MQMessageUtil {

    // fixme configurable or retrieve from rocketMQ?
    private static final int MAX_MESSAGE_SIZE = 1 * 1024 * 1024; /* aka 1MB */
    private static final String COMPRESSED_PREFIX = "compressed.";

    private static String encode(String message) {
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

    private static String decode(String message) {
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
}
