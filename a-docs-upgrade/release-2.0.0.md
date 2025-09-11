# 依赖改变

## httpclient 4 -> 5

4 的依赖已经从 spring-boot 中移除，改为使用 httpclient5。

```xml
<!-- https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5 -->
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.5</version>
</dependency>
```

# spring-cloud-gateway 升级

新版本的 spring-cloud-gateway 由于有 webflux 和 servlet 两种版本，咱们不直接跳到 servlet 版本，继续使用 webflux 版本。

namespace 发生了变化，原来的 `spring.cloud.gateway` 变成了 `spring.cloud.gateway.server.webflux`。例如原来的是：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: route1
          uri: http://example.com
          predicates:
            - Path=/api/**
```

现在需要改为：

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: route1
              uri: http://example.com
              predicates:
                - Path=/api/**
```

# 单元测试 mockito

```xml
 <plugin>
     <groupId>org.apache.maven.plugins</groupId>
     <artifactId>maven-dependency-plugin</artifactId>
     <executions>
         <execution>
             <goals>
                 <goal>properties</goal>
             </goals>
         </execution>
     </executions>
 </plugin>
 <plugin>
     <groupId>org.apache.maven.plugins</groupId>
     <artifactId>maven-surefire-plugin</artifactId>
     <configuration>
         <argLine>@{argLine} -javaagent:${org.mockito:mockito-core:jar}</argLine>
     </configuration>
 </plugin>
```

# 该版本不能启用虚拟线程，原因

1. spring-cloud-gateway 需要改成 servlet 实现版本。

# spring-boot-starter-mybatis 升级

目前从 `javax.persistence` 迁移到了 `jakarta.persistence`，所以需要注意。

# 全局增加了 java faker 测试依赖，用于生成测试数据

例如：

```java
Faker faker = new Faker(Locale.US); // 随机生成美国的地址，姓名，电话等
String name = faker.name().fullName(); // Miss Samanta Schmidt
String firstName = faker.name().firstName(); // Miss Samanta
String lastName = faker.name().lastName(); // Schmidt
String streetAddress = faker.address().streetAddress(); // 6000 Simone Points
String city = faker.address().city(); // New Jonathanshire
String state = faker.address().state(); // Michigan
String zipCode = faker.address().zipCode(); // 90904
String country = faker.address().country(); // United States of America
String phoneNumber = faker.phoneNumber().phoneNumber(); // 1-584-491-
String cellPhone = faker.phoneNumber().cellPhone(); // 1-584-491-8253
String email = faker.internet().emailAddress(); //
```

# spring-boot-starter-rocketmq 升级

rocketmq 消费者保留了老的方式（通过继承 `AbstractMQConsumer` 的方式，消费 string，自己做反序列化），增加了新的方式 `AbstractConsumer<T>`，可以直接消费对象。

增加了新的生产者，生产版本 v2 的消息，消息主要改变是生产 `BaseMQMessage` 的 data 字段是实际对象，而不是 json 字符串。但是新的生产者与老的消费者不兼容，升级前的消费者只能消费老的消息格式。但是对于消费者，升级后的，无论是新老消费者可以同时消费新老消息。所以，这里的升级方式是：

1. 升级依赖之后，不用加任何配置，默认还是用老的生产者。升级依赖后的新老消费者都可以消费老的消息。
2. 升级所有微服务，不用管哪个微服务生产或者消费消息，没有升级先后顺序要求
3. 等所有微服务都升级完毕之后，想要使用新的生产者，只需要在配置中加上：
   ```yaml
   rocketmq:
     extend:
       use-new-producer: true
   ```
4. 之后，所有微服务都可以使用新的生产者生产 v2 版本