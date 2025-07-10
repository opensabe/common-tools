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
2. spring-boot-starter-redisson 中的对于 spring-data-redis 中的 LettuceConnectionFactory 的使用是普通线程池的，参考：`org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration#redisConnectionFactoryVirtualThreads`