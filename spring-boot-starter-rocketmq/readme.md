# spring-boot-starter-rocketmq 使用文档

## 使用说明

**引入依赖**：
```
<groupId>io.github.opensabe-tech</groupId>
<artifactId>spring-boot-starter-rocketmq</artifactId>
<version>去 maven 看最新版本</version>
```

**增加配置**：由于依赖了 [common-id-generator](..%2Fcommon-id-generator)，所以需要增加这个模块需要的数据库以及 redis 配置。
并且重试是依赖 [spring-boot-starter-mybatis](..%2Fspring-boot-starter-mybatis) 的基础上，需要增加扫描 `io.github.opensabe.common.config.dal.db.dao` 的 maaper 配置，在这些配置基础上:

**增加 mq 配置**：

生产组的配置每个国家是一样的，都是以微服务名称作为生产组名称的，所以放入：`bootstrap.yml`：
```
rocketmq:
  producer:
    group: ${spring.application.name}
```
nameserver配置每个国家不一样，所以放入：`application.yml`
```
rocketmq:
  name-server: mqserver1.rocketmq:9876;mqserver2.rocketmq:9876;mqserver3.rocketmq:9876
```

其他配置，请参考：`org.apache.rocketmq.spring.autoconfigure.RocketMQProperties` 这个类

## 生产消息
```
@Autowired
private MQProducer mqProducer;
```
**1. 同步发送消息**
```
/**
 * 同步发送消息，不能保持消费有序
 * @param topic 主题
 * @param o 消息体
 * 如果消息体为BaseMQMessage，则会填充其中的除了业务traceId还有action的所有字段
 * 不为BaseMQMessage会封装成为BaseMQMessage
 * @see io.github.opensabe.common.entity.base.vo.BaseMQMessage
 */
void send(String topic, Object o);
```
**2. 异步发送消息，不等待结果，提交给异步线程立刻返回**
```
/**
 * 异步发送，
 * @param topic
 * @param o
 */
void sendAsync(String topic, Object o);
/**
 * 可以通过isAsync是否同步发送消息
 * @param topic
 * @param o
 * @param isAsync
 */
void send(String topic, Object o, boolean isAsync);
```

**3. 异步发送，带回调**
```
/**
 * 异步发送，设置回调
 * @param topic
 * @param o
 * @param sendCallback
 */
void sendAsync(String topic, Object o, SendCallback sendCallback);
```
例如：
```
mqProducer.sendAsync("rocketmq-test-topic", POJO.builder().text("今天天气不错").timestamp(timestamp).build(), new SendCallback() {
    @Override
    public void onSuccess(SendResult sendResult) {
        
    }

    @Override
    public void onException(Throwable throwable) {

    }
});
```
**4. 为了保证消息有序，需要指定 hashKey，让同一个 hashKey 的消息发送到同一个 Queue**
```
/**
 * 如果指定了hashKey，则会通过这个key进行分片发送到固定的queue上面，如果消费者是有序消费模式，则能保证同一个key下的消息有序
 * @param topic
 * @param o
 * @param hashKey
 */
void send(String topic, Object o, String hashKey);
/**
 * 如果指定了hashKey，则会通过这个key进行分片发送到固定的queue上面，如果消费者是有序消费模式，则能保证同一个key下的消息有序
 * @param topic
 * @param o
 * @param hashKey
 */
void sendAsync(String topic, Object o, String hashKey, SendCallback sendCallback);

/**
 *
 * @param topic
 * @param o
 * @param hashKey
 * @param isAsync
 */
void send(String topic, Object o, String hashKey, boolean isAsync);
```

**5. 带重试的 API**
```
/**
 * 同步发送消息，不能保持消费有序
 * @param topic 主题
 * @param o 消息体
 * 如果消息体为BaseMQMessage，则会填充其中的除了业务traceId还有action的所有字段
 * 不为BaseMQMessage会封装成为BaseMQMessage
 * @see io.github.opensabe.common.entity.base.vo.BaseMQMessage
 */
void send(String topic, Object o, MQSendConfig mqSendConfig);
```

例如：
```
mqProducer.send("rocketmq-test-topic", POJO.builder().text("今天天气不错").timestamp(timestamp).build(), MQSendConfig.builder()
    //重试3次失败后，存入数据库靠定时任务继续重试
    .persistence(true)
    //重试3次
    .retryTimes(3).build());
```
**6. 所有特性的 API**
```
void send(String topic, Object o, String hashKey, boolean isAsync, SendCallback sendCallback, MQSendConfig mqSendConfig);
```

## 消费消息

需要继承 AbstractMQConsumer 类，至少指定 topic 和 consumerGroup。consumerGroup 的格式为 `微服务名称_topic(如果是顺序消费则加上 _orderly 后缀)`，微服务名称可以通过 `${spring.application.name}` 占位符指定，这样，通过消费组我们就知道是哪个微服务消费哪个 topic，是否是有序的。
```
@RocketMQMessageListener(
        consumerGroup = "${spring.application.name}_rocketmq-test-topic",
        topic = "rocketmq-test-topic"
)
public static class TestConsumer extends AbstractMQConsumer {

    @Override
    protected void onBaseMQMessage(BaseMQMessage baseMQMessage) {
        POJO pojo = JSON.parseObject(baseMQMessage.getData(), POJO.class);
        System.out.println(pojo);
        if (pojo.timestamp != null && pojo.timestamp.equals(timestamp)) {
            received = true;
        }
    }
}
```

## 保证消息有序
如果要保证消费有序：
- 生产者对于每条消息指定合适的 hashKey，例如我们想对于所有用户的订单消费有序，先产生的订单先消费，那么考虑保证同一用户下先产生的订单先消费是否符合要求，如果符合就以 userId 为 key
- 如果必须全部有序，那么不用指定 hashkey，则配置这个 topic 只有一个 queue 即可。**不推荐这么做**，一般业务也没有这样的需求
- 消费者需要指定有序消费，例如：
```
@RocketMQMessageListener(
        consumerGroup = "${spring.application.name}_rocketmq-test-topic",
        consumeMode = ConsumeMode.ORDERLY,
        topic = "rocketmq-test-topic"
)
```
## 线上创建新的 topic

我们配置 MQ 是自动创建 topic 的，但是**自动创建的，并不会分布在每个 broker 上**，需要我们手动修改下

## Observation

针对所有消息的生产和消费，增加了 Observation

### Produce

#### HighCardinalityKeyValues

| 属性                          | 类型     | 备注               |
|-----------------------------|--------|------------------|
| message.produce.topic       | string | 主题               |
| message.produce.msg.length  | long   | 消息体封装对象 data 的长度 |
| message.produce.send.result         | string | 发送结果            |
| message.produce.send.throwable      | string | 异常信息            |

#### LowCardinalityKeyValues


| 属性                          | 类型     | 备注               |
|-----------------------------|--------|------------------|
| message.produce.topic       | string | 主题               |
| message.produce.send.result         | string | 发送结果            |

## Consume

### HighCardinalityKeyValues

| 属性                          | 类型     | 备注             |
|-----------------------------|--------|----------------|
| message.consume.origin.trace.id       | string | 发消息的原始 traceId |
| message.consume.topic  | long   | 主题             |
| message.consume.successful         | Boolean | 是否消费成功         |
| message.consume.throwable      | string | 异常信息           |

### LowCardinalityKeyValues

| 属性                          | 类型      | 备注             |
|-----------------------------|---------|----------------|
| message.consume.topic  | long    | 主题             |
| message.consume.successful         | Boolean | 是否消费成功         |
