# spring-boot-starter-socketio 使用文档

## 使用说明

**引入依赖**：
```
<groupId>io.github.opensabe-tech</groupId>
<artifactId>spring-boot-starter-socketio</artifactId>
<version>去 maven 看最新版本</version>
```

### 1. 配置：

需要配置提供服务的 context 地址，端口，以及相关的 netty 线程池配置，以及是否使用原生的 epoll 库：

```
server:
  socketio:
    # 前端连接地址，默认是 socket.io
	# 路径必须包含 socket.io，原因参考 2. 怎么通过 api-gateway 访问
    context: /service-app/socket.io
	# 提供服务的端口
    port: 4001
	# 心跳间隔，2s
    pingInterval: 2000
	# 心跳超时，10s
    pingTimeout: 10000
	# netty 主线程个数
    bossThreads: 32
	# netty 额外工作线程个数
    workerThreads: 32
	# 是否使用原生的 epoll 库，只有 epoll 存在才会启用
    useLinuxNativeEpoll: true
```

### 2. 怎么通过 api-gateway 访问
额外的，注册到 Eureka 的实例的端口还是微服务本身服务的端口（`server.port`指定的那个），但是在 metadata 中多了一个 key：

这个是咱们这个封装库做的事情，在 metadata 中放了一个 `socket.io` 的 key。这样，API-gateway 中，我们也会改造覆盖负载均衡器的 Filter，当**访问的路径中包含`socket.io`的时候**，替换微服务本身的 port 为这个 socket.io key 的值。所以，我们需要限制前面 context 地址必须包含 socket.io

### 3. socket.io 一些简单概念与后台开发规范

建立好 websocket 连接后，服务端与客户端可以持续的双向通信，并且不再是 request-response 那种单向阻塞模式。

socket.io 其实就是在 websocket 的基础上，加入了一些自己的封装：

**每个连接都有自己的唯一 id，其实就是会话 id，这个可以通过看创建的websocket链接里面发的信息看出来**：

socket.io 中有 room 概念，处于同一个 room 的会话，会收到来自于这个 room 的广播消息。针对我们的使用，我们抽象：
- 针对会话的单推：不用 room，直接通过连接推送
- 针对用户的单推：在创建连接之后，根据请求的 header 中的 uid，自动让这个会话加入这个 uid （名字是: `uid:用户id`）对应的 room，这样只要在这个房间广播，所有这个用户的会话都能收到。
- 订阅 topic，取消订阅 topic，topic 广播：通过 room 实现，topic 就是 room

**底层如何实现不同微服务实例的 room 推送同步**


其实就是通过 Redis，订阅同一个 topic，如果有 room 的推送需要，就在推送当前实例之后，发布到 Redis 之后，其他实例也能收到推送。

**消息结构与处理**

消息很简单，就是一个 JSON 数组，第一个代表消息类型，之后每个都代表参数，例如(这个例子其实是咱们内置的订阅某个 topic 的处理)：
这个消息类型就是 sub，参数只有一个，并且是字符串类型。对应服务端处理是：

```
/**
* 订阅，其实就是进入某个房间
* @param client
* @param request
* @param topic
*/
@OnEvent("sub")
public void sub(SocketIOClient client, AckRequest request, String topic) {
//加入这个 topic 对应的
client.joinRoom(topic);
log.info("DefaultSocketIoHandlerConfiguration-sub: client.id: {}, topic {}, allTopics: {}", client.getSessionId(), topic, JSON.toJSONString(client.getAllRooms()));
//需要发送确认，这个由客户端决定是否处理
request.sendAckData(BaseAck.builder().b(BizCodeEnum.SUCCESS.getVal()).build());
}
```

举个处理的简单例子：


注意，如果你的业务涉及 IO，**一定要使用 @Async 注解做成异步的**（并且方法返回值一定要是 void，反正这个不是请求-响应那种模式，如果想发 ACK 通过 request.sendAckData 就可以）。不能阻塞处理 socketio 请求的线程。**如何启用 @Async 注解异步**：

主类上面添加 `@EnableAsync`
```
@SpringBootApplication(scanBasePackages = {"io.github.opensabe.game.center"})
@EnableAsync
public class GameCenterApplication {
    public static void main(String[] args) throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        System.setProperty("LOCAL_IP", hostAddress);
        SpringApplication.run(GameCenterApplication.class, args);
    }
}
```

定义你的线程池：
```
@Configuration
public class TestConfig {
    @Autowired
    private ThreadPoolFactory threadPoolFactory;

    @Bean(name = "testExecutorService")
    public ExecutorService getTestExecutorService() {
        return threadPoolFactory.createNormalThreadPool("testExecutorService_", 32);
    }
}
```
使用你的线程池（可以通过名字指定线程池）
```
@OnEvent("test")
@Async("testExecutorService")
public void test(SocketIOClient client, AckRequest request, Param param, int size) {
	request.sendAckData(BaseAck.builder().b(BizCodeEnum.SUCCESS.getVal()).build());
}
```

**如何获取建立连接的时候的请求 Header**：
```
获取到：SocketIOClient client;

client.getHandshakeData().getHttpHeaders();

//获取 userId
client.getHandshakeData().getHttpHeaders().get(HttpHeaderUtil.UID);
```

**如何开发一个类似于下单的请求**：

注意，请求对应的响应对于前端也是异步发送，并且前端必须指定接收响应（也就是添加ack回调），服务端才会发送响应

1. 指定事件类型（假设是 test），设计接口参数，编写接口文档
2. 使用 `@OnEvent("test")` 处理 test 事件，方法参数：`SocketIOClient client, AckRequest request, 后面跟着你设计的参数`
3. 下单肯定涉及 IO，所以要用 @Async 注解
4. 使用 `request.sendAckData` 返回响应，响应使用 `BaseAck` 封装
5. 如果有错误，必须 catch 住，使用 `request.sendAckData` 返回响应，响应使用 `BaseAck` 封装，否则会走默认的逻辑（不会给前端任何响应，只有日志）。

**怎么主动推送到某个Topic（其实就是 Room，房间）**：

1. 设计 Topic，以及事件类型，编写文档
2. 编写订阅这个功能发送的请求的代码，参考上一节：**如何开发一个类似于下单的请求**（里面的核心逻辑其实就是 join 某个或者某些 room）
3. 编写取消订阅这个功能发送的请求的代码，参考上一节：**如何开发一个类似于下单的请求**（里面的核心逻辑其实就是 left 某个或者某些 room）
4. 在需要推送文档中的消息的地方，编写推送消息的代码：

```
@Autowired
private SocketIOServer socketIOServer;

socketIOServer.getRoomOperations("上面加入的room").send("你定义的事件类型", 事件内容);
```

**怎么实现推送给某个用户**：

1. 我们在这个 spring-boot-starter-socketio 实现了在连接建立的时候，如果 Header 里面有 uid，就自动加入 "uid:header里面的uid" 这个房间。
1. 注入 `SocketIoMessageTemplate` 这个 Bean
3. socketIoMessageTemplate.sendEventToUser(); 发送给某个用户（底层实际就是在 "uid:header里面的uid" 这个房间广播）

### 4. 如何限制登录后才能访问

和其他接口一样，在 api-gateway 限制即可

### 5. 如何测试

推荐使用 apic（注意这个工具有点小问题，如果你指定了**Transport为pooling**，这个是模拟低版本浏览器不支持websocket需要长连接轮询实现，浏览器开多个客户端其实用的也是同一个连接串，虽然实际连接不是用一个，但是使用的 session-id 是同一个，导致只有一个连接能接受到推送，如果是**Transport为websocket**就没有这个问题）：


如果想要压测， Java 客户端可以使用：
```
<dependency>
	<groupId>io.socket</groupId>
	<artifactId>socket.io-client</artifactId>
	<version>1.0.1</version>
	<scope>test</scope>
</dependency>
```

### 6.@OnEvent所在方法抛出异常的拦截


#### 6.1. 定义@OnEvent 全局异常拦截处理类上面 加 注解@EventExceptionHandlerAdvice 和 @Order


1. @EventExceptionHandlerAdvice ：表示要拦截@OnEvent标注的方法抛出的异常
2. @Order: 当有多个advice处理同一个异常时，哪个起作用，order越小，优先级越高
3. advice上的@Order 优先级高于handler上的@Order


#### 6.2.方法上加注解@EventExceptionHandler 和 @Order

1. @EventExceptionHandler：表示要拦截哪类异常
    1. @EventExceptionHandler.value 表示要拦截哪些异常
    2. 参数 必须写成`(Throwable exception,SocketIOClient client, AckRequest request, Object...objects) `格式，且顺序不能改变，objects即为@OnEvent方法的入参，否则报错
2. @Order：当同一个advice里有多个handler处理同一个异常时，哪个起作用，order越小，优先级越高

#### 6.3.使用示例

如下定义了两个advice：EventExceptionAdvice01和EventExceptionAdvice02，当@OnEvent标注的方法抛出AppException时，根据上面所说的优先级EventExceptionAdvice02.handleException02会处理异常

```
@Order(value = 2)
@EventExceptionHandlerAdvice
public class EventExceptionAdvice01 {

    @EventExceptionHandler(value = {AppException.class})
    public void handleException(AppException exception, SocketIOClient client, AckRequest request,Object...objects) {
        log.error("-=================== exception handler test");
    }
}```

```
@Order(value = 1)
@EventExceptionHandlerAdvice
public class EventExceptionAdvice02 {

    @EventExceptionHandler(value = {AppException.class})
//    @Order(2)
public void handleException01(AppException exception, SocketIOClient client, AckRequest request, Object...objects) {
log.error("-=================== exception handler test01");
}

    @Order(1)
    @EventExceptionHandler(value = {AppException.class})
    public void handleException02(AppException exception, SocketIOClient client, AckRequest request, Object...objects) {
        log.error("-=================== exception handler test02");
    }
}


####  observation

#### 建立连接事件
```
事件名称：socketio.execute.connect

触发时机：客户端建立连接成功时
```
##### HighCardinalityKeyValues

| 属性  |类型| 备注              |
| ------------ | ------------ |-----------------|
| 832e8469-9d70-4258-9b6c-4a1b983572a2   | string  | 连接建立后的sessionId |
                                  |


##### LowCardinalityKeyValues
```
同 HighCardinalityKeyValues
```

##### JFR
```
事件名称：Connect Event

事件分类（所属文件夹）：observation.Socket-Connect
```
| 属性        | 备注                    |
|-----------|-----------------------|
| headers   | 客户端所有header 参数转为 json |
| sessionId | 连接建立时的 sessionId      |

#### 连接断开事件(上报参数内容 和 connect保持一致)


```
事件名称：socketio.execute.dis-connect

触发时机：连接断开时触发
```

#### 监控客户端发送的数据

```
事件名称：socketio.execute.event

触发时机：客户端写数据到服务端时
```

##### HighCardinalityKeyValues

| 属性  |类型| 备注              |
| ------------ | ------------ |-----------------|
| 832e8469-9d70-4258-9b6c-4a1b983572a2   | string  | 连接建立后的sessionId |
|


##### LowCardinalityKeyValues
```
同 HighCardinalityKeyValues
```

##### JFR
```
事件名称：On Event

事件分类（所属文件夹）：observation.Socket-OnEvent
```
| 属性        | 备注                    |
|-----------|-----------------------|
| headers   | 客户端所有header 参数转为 json |
| sessionId | 连接建立时的 sessionId      |
| eventName | 事件名                   |

