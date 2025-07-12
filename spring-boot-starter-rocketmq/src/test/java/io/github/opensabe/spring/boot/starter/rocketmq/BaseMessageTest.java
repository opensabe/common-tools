package io.github.opensabe.spring.boot.starter.rocketmq;

import io.github.opensabe.common.entity.base.vo.BaseMessage;
import org.apache.rocketmq.common.message.MessageExt;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author heng.ma
 */
@DisplayName("RocketMQ基础消息测试")
public class BaseMessageTest {

    public record User (String name, Integer age) {}

    @Test
    @DisplayName("测试简单对象消息 - 带包装器")
    void testSimple () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(SimpleMessageListener.jsonWrapper.getBytes());
        new SimpleMessageListener().onMessage(ext);
    }
    @Test
    @DisplayName("测试简单对象消息 - 不带包装器")
    void testSimpleWithoutWrapper () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(SimpleMessageListener.json.getBytes());
        new SimpleMessageListener().onMessage(ext);
    }

    @Test
    @DisplayName("测试列表对象消息 - 带包装器")
    void testList () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(ListMessageListener.jsonWrapper.getBytes());
        new ListMessageListener().onMessage(ext);
    }
    @Test
    @DisplayName("测试列表对象消息 - 不带包装器")
    void testListWithoutWrapper () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(ListMessageListener.json.getBytes());
        new ListMessageListener().onMessage(ext);
    }

    @Test
    @DisplayName("测试Map对象消息 - 带包装器")
    void testMap () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(MapMessageListener.jsonWrapper.getBytes());
        new MapMessageListener().onMessage(ext);
    }
    @Test
    @DisplayName("测试Map对象消息 - 不带包装器")
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
            User user = baseMQMessage.getData();
            Assertions.assertNotNull(user);
            Assertions.assertEquals(10, user.age());
            Assertions.assertEquals("zhangsan", user.name());
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
            List<User> list = baseMQMessage.getData();
            assertThat(list).hasSize(2)
                    .extracting(User::name, User::age)
                    .containsExactly(Tuple.tuple("zhangsan", 10), Tuple.tuple("lisi", 20));
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
            Map<String, List<User>> map = baseMQMessage.getData();
            assertThat(map).hasSize(2)
                    .containsKeys("male", "female");
            assertThat(map.get("male")).hasSize(2)
                    .extracting(User::name, User::age)
                    .containsExactly(Tuple.tuple("zhangsan", 10), Tuple.tuple("lisi", 20));
            assertThat(map.get("female")).hasSize(2)
                    .extracting(User::name, User::age)
                    .containsExactly(Tuple.tuple("lily", 11), Tuple.tuple("lucy", 21));
        }
    }
}
