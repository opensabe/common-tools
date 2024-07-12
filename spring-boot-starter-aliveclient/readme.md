# spring-boot-starter-aliveclient 使用文档

## 使用说明

**引入依赖**：
```
<groupId>io.github.opensabe-tech</groupId>
<artifactId>spring-boot-starter-aliveclient</artifactId>
<version>去 maven 看最新版本</version>
```


**增加配置**：由于依赖了 [spring-boot-starter-rocketmq](..%2Fspring-boot-starter-rocketmq)，所以需要添加 rocketmq(参考：[readme.md](..%2Fspring-boot-starter-rocketmq%2Freadme.md)） 相关依赖，在此基础上：
```
alive:
  push:
    # 客户端数量
    client-num: 5
    # 不同国家 product 不一样，参考 push-center 数据库
    product: 1000
    # 盐加密，不同 product 的 salt 不一样
    salt: test
    # zk-url可以不配置，这样就不能用 netty 直推了
    zk-url: test1-zookeeper.opensabe.com:2181
    # rocketmq 客户端配置
    rocketmq:
      name-server: test1-mq.opensabe.com:9876
      producer:
        group: common_alive_opensabe_marketing
```
## 使用新的推送方式(rocket)

```
//首选就是这个
@Autowired
private Client client;

/**
 * 单推
 */
@Test
public void testSingle(){
    var message = new MessageVo(RECEIVE_TOPIC, JSONObject.toJSONBytes(message),
            "201201025558puid61232548", io.github.opensabe.alive.protobuf.Message.PushType.MULTI,exoiry,"201201025558puid61232548");
    try {
        ResponseFuture future = client.pushAsync(message);
        System.out.println(future.get().name());
    } catch (Exception e) {
        e.printStackTrace();
    }
}

/**
 * 群推
 */
@Test
public void testGroup(){
    var message = new MessageVo(RECEIVE_TOPIC, JSONObject.toJSONBytes(message),
            "201201025558puid61232548", io.github.opensabe.alive.protobuf.Message.PushType.GROUP,exoiry,"201201025558puid61232548");
    try {
        ResponseFuture future = client.pushAsync(message);
        System.out.println(future.get().name());
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

## 使用旧的推送方式(netty直推)，不要再使用了，因为处理不过来就会阻塞

```
@Autowired
@Qualifier(AliveProperties.NETTY_CLIENT_NAME)
private Client client;

/**
 * 单推
 */
@Test
public void testSingle(){
    var message = new MessageVo(RECEIVE_TOPIC, JSONObject.toJSONBytes(message),
            "201201025558puid61232548", io.github.opensabe.alive.protobuf.Message.PushType.MULTI,exoiry,"201201025558puid61232548");
    try {
        ResponseFuture future = client.pushAsync(message);
        System.out.println(future.get().name());
    } catch (Exception e) {
        e.printStackTrace();
    }
}

/**
 * 群推
 */
@Test
public void testGroup(){
    var message = new MessageVo(RECEIVE_TOPIC, JSONObject.toJSONBytes(message),
            "201201025558puid61232548", io.github.opensabe.alive.protobuf.Message.PushType.GROUP,exoiry,"201201025558puid61232548");
    try {
        ResponseFuture future = client.pushAsync(message);
        System.out.println(future.get().name());
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```
