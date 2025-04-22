package io.github.opensabe.spring.boot.starter.rocketmq;

import io.github.opensabe.common.entity.base.vo.BaseMessage;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * @author heng.ma
 */
public class BaseMessageTest {

    public record User (String name, Integer age) {}

    @Test
    void testSimpleV2 () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(SimpleMessageListener.jsonWrapper.getBytes());
        new SimpleMessageListener().onMessage(ext);


    }

    static class SimpleMessageListener extends AbstractConsumer<User> {
        static final String jsonWrapper = """
                {
                    "data" : {
                        "name" : "zhangsan",
                        "age": 10
                    }
                }
                """;
        static final String json = """
                {
                    "name" : "zhangsan",
                    "age": 10
                }
                """;

        @Override
        public void onMessage(MessageExt ext) {
            onBaseMessage(convert(ext));
        }

        @Override
        protected void onBaseMessage(BaseMessage<User> baseMQMessage) {
            System.out.println(baseMQMessage.getData().name());
            System.out.println(baseMQMessage.getData().age());
        }
    }

    static class ListMessageListener extends AbstractConsumer<List<User>> {
        static final String jsonWrapper = """
                {
                    "data" : [
                        {
                            "name" : "zhangsan",
                            "age": 10
                        },
                        {
                            "name" : "lisi",
                            "age": 20
                        }
                    ]
                }
                """;
        static final String json = """
                [
                    {
                        "name" : "zhangsan",
                        "age": 10
                    },
                    {
                        "name" : "lisi",
                        "age": 20
                    }
                ]
                """;

        @Override
        public void onMessage(MessageExt ext) {
            onBaseMessage(convert(ext));
        }

        @Override
        protected void onBaseMessage(BaseMessage<List<User>> baseMQMessage) {

        }
    }

    static class MapMessageListener extends AbstractConsumer<Map<String, List<User>>> {
        static final String jsonWrapper = """
                {
                    "data" : {
                        "male" : [
                            {
                                "name" : "zhangsan",
                                "age": 10
                            },
                            {
                                "name" : "lisi",
                                "age": 20
                            }
                        ],
                       "female": [
                            {
                                "name" : "lily",
                                "age": 11
                            },
                            {
                                "name" : "lucy",
                                "age": 21
                            }
                       ]
                    }
                }
                """;
        static final String json = """
                {
                        "male" : [
                            {
                                "name" : "zhangsan",
                                "age": 10
                            },
                            {
                                "name" : "lisi",
                                "age": 20
                            }
                        ],
                       "female": [
                            {
                                "name" : "lily",
                                "age": 11
                            },
                            {
                                "name" : "lucy",
                                "age": 21
                            }
                       ]
                }
                """;

        @Override
        public void onMessage(MessageExt ext) {
            onBaseMessage(convert(ext));
        }
        @Override
        protected void onBaseMessage(BaseMessage<Map<String, List<User>>> baseMQMessage) {

        }
    }

    static class StringMessageListener extends AbstractConsumer<String> {

        @Override
        public void onMessage(MessageExt ext) {
            onBaseMessage(convert(ext));
        }
        @Override
        protected void onBaseMessage(BaseMessage<String> baseMQMessage) {

        }
    }
}
