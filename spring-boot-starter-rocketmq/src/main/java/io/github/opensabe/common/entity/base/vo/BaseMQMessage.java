package io.github.opensabe.common.entity.base.vo;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

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
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            JsonToken jsonToken = p.currentToken();
            if (jsonToken.isStructStart()) {
                log.warn("BaseMQMessage.deserialize: v2 String type auto-fixed, you can use new AbstractConsumer instead of AbstractMQConsumer to avoid this warning. message");
                return  p.getCodec().readTree(p).toString();
            }
            return p.getValueAsString();
        }
    }
}