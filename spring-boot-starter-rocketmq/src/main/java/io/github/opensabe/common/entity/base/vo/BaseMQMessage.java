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
package io.github.opensabe.common.entity.base.vo;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

/**
 * RocketMQ 消息信封（V1 线格式）。
 * <p>
 * {@link #data} 在 JSON 中为字符串；若反序列化遇到 JSON 对象（V2 线），
 * {@link StringDeserializer} 会将其压缩为 compact 字符串以兼容遗留 {@code AbstractMQConsumer}。
 */
@Getter
@Setter
@ToString
@Log4j2
@EqualsAndHashCode(callSuper = true)
public class BaseMQMessage extends BaseMessage<String> {

    /** 业务载荷；V1 为 JSON 字符串，V2 对象经反序列化器压平后亦存于此字段。 */
    @JsonDeserialize(using = StringDeserializer.class)
/** data。 */
    private String data;

    /** 无参构造，供 Jackson 反序列化。 */
    public BaseMQMessage() {
    }

    /**
     * 仅含载荷的便捷构造。
     *
     * @param data 业务数据 JSON 字符串
     */
    public BaseMQMessage(String data) {
        super(data);
    }

    /**
     * 完整信封字段构造。
     *
     * @param traceId 链路追踪 ID
     * @param spanId Span ID
     * @param ts 时间戳
     * @param src 消息来源
     * @param action 动作标识
     * @param data 业务载荷
     */
    public BaseMQMessage(String traceId, String spanId, Long ts, String src, String action, String data) {
        super(traceId, spanId, ts, src, action, data);
    }

    /**
     * 将 {@link #data} 反序列化为 {@link String}；兼容 V2 对象形态。
     */
    public static class StringDeserializer extends StdDeserializer<String> {

        /** 注册到 Jackson 的标准构造。 */
        protected StringDeserializer() {
            super(String.class);
        }

        /**
         * 字符串直接取值；JSON 对象/数组压成 compact 字符串并记录迁移告警。
         *
         * @param p JSON 解析器
         * @param ctxt 反序列化上下文
         * @return 字符串形式的 {@code data}
         * @throws JacksonException 解析失败
         */
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            JsonToken jsonToken = p.currentToken();
            if (jsonToken.isStructStart()) {
                log.warn("BaseMQMessage.deserialize: V2 object payload auto-flattened to string; prefer AbstractConsumer over AbstractMQConsumer to avoid this warning");
                return ctxt.readTree(p).toString();
            }
            return p.getValueAsString();
        }
    }
}
