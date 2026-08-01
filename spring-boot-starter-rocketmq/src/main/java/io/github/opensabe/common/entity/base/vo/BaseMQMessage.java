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

@Getter
@Setter
@ToString
@Log4j2
@EqualsAndHashCode(callSuper = true)
public class BaseMQMessage extends BaseMessage<String> {


    @JsonDeserialize(using = StringDeserializer.class)
    private String data;

    public BaseMQMessage() {
    }

    public BaseMQMessage(String data) {
        super(data);
    }

    public BaseMQMessage(String traceId, String spanId, Long ts, String src, String action, String data) {
        super(traceId, spanId, ts, src, action, data);
    }


    public static class StringDeserializer extends StdDeserializer<String> {

        protected StringDeserializer() {
            super(String.class);
        }

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            JsonToken jsonToken = p.currentToken();
            if (jsonToken.isStructStart()) {
                log.warn("BaseMQMessage.deserialize: v2 String type auto-fixed, you can use new AbstractConsumer instead of AbstractMQConsumer to avoid this warning. message");
                return ctxt.readTree(p).toString();
            }
            return p.getValueAsString();
        }
    }
}