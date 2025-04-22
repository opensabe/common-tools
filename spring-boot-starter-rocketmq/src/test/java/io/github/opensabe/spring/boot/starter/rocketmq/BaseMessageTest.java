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
    void testSimple () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(SimpleMessageListener.jsonWrapper.getBytes());
        new SimpleMessageListener().onMessage(ext);
    }
    @Test
    void testSimpleWithoutWrapper () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(SimpleMessageListener.json.getBytes());
        new SimpleMessageListener().onMessage(ext);
    }

    @Test
    void testList () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(ListMessageListener.jsonWrapper.getBytes());
        new ListMessageListener().onMessage(ext);
    }
    @Test
    void testListWithoutWrapper () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(ListMessageListener.json.getBytes());
        new ListMessageListener().onMessage(ext);
    }

    @Test
    void testMap () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(MapMessageListener.jsonWrapper.getBytes());
        new MapMessageListener().onMessage(ext);
    }
    @Test
    void testMapWithoutWrapper () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(MapMessageListener.json.getBytes());
        new MapMessageListener().onMessage(ext);
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
            System.out.println(baseMQMessage.getData().size());
            baseMQMessage.getData().forEach(user -> {
                System.out.println(user.name());
                System.out.println(user.age());
            });
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
            System.out.println(baseMQMessage.getData().size());
            System.out.println(baseMQMessage.getData().get("male"));
            System.out.println(baseMQMessage.getData().get("female"));
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
