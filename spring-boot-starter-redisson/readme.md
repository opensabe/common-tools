# Spring boot starter redisson 使用文档

## 使用说明

**引入依赖**：
```
<groupId>io.github.opensabe-tech</groupId>
<artifactId>spring-boot-starter-redisson</artifactId>
<version>去 maven 看最新版本</version>
```

**增加配置**：
application.yml; 创建 redisson 客户端使用的配置就是 spring-data-redis 的配置，即：
```
# 我们项目中涉及到的配置：
spring:
  redis:
    # 链接地址
    host: test1.s.opensabe
    # 端口
    port: 6379
    # 命令超时 Duration
    timeout: 60000
    # 链接超时 Duration
    connectTimeout: 500
    # 我们用的是 lettuce 连接池
    lettuce:
      pool:
        # 最大连接数
        max-active: 500
```


其中有一点特别指出下，我们使用最大连接数作为 redisson 客户端的命令连接池大小以及订阅连接池大小，订阅连接池的每一个连接上面最多订阅的数量为写死的 50 (Redisson 的锁实现是 key-value + hash + subpub,参考：https://zhanghaoxin.blog.csdn.net/article/details/82426013)

## Redisson 锁注解

**首先，特别说明下，对于 Redisson 锁，不一定需要指定过期时间，因为 Redisson Rlock 不设置超时时间，并不代表底层 Key 没有超时时间，而是这个进程定时续期的。如果进程死掉或者 JVM 卡住 1min 以上也会释放锁。
**

**对于某个 bean 的某个方法使用 redisson 阻塞锁**：
```
@RedissonLock
public void testBlockLock(@RedissonLockName String name) throws InterruptedException {
}
```
之后，通过 bean 调用 `testBlockLock` 方法，就会根据参数 name 的值，**默认的锁前缀名称为**:`redisson:lock:`，获取 redisson 锁并阻塞锁住。之后才会执行方法体，执行完之后，释放锁。

**对于某个 bean 的某个方法使用 redisson 非阻塞锁**
```
@RedissonLock(lockType = RedissonLock.TRY_LOCK, waitTime = 10000, timeUnit = TimeUnit.MILLISECONDS)
public void testTryLock(@RedissonLockName String name) throws InterruptedException {
}
```
需要指定尝试获取的等待时间。超过这个时间没有获取锁则会抛出 `RedissonClientException`，不会执行方法体。

**对于某个 bean 的某个方法使用 redisson 非阻塞锁，获取失败立刻返回**
```
@RedissonLock(lockType = RedissonLock.TRY_LOCK_NOWAIT)
public void testTryLockNoWait(@RedissonLockName String name) throws InterruptedException {
}
```
尝试获取锁，获取失败立刻抛出 `RedissonClientException`，不会执行方法体。

**通过参数 leaseTime，限制获取锁的最长时间**
```
@RedissonLock(leaseTime = 1000L)
public void testLockTime(@RedissonLockName String name) throws InterruptedException {
}
```
**指定锁名称的前缀，通过 SPEL 表达式从对象中获取锁属性名**

```
@RedissonLock
public void testRedissonLockNameProperty(@RedissonLockName(prefix = "test:", expression = "#{id==null?name:id}") Student student, String params) throws InterruptedException {
}
```

使用可以参考单元测试：`io.github.opensabe.common.redisson.test.RedissonLockTest`以及注解本身的注释

## Redisson RateLimiter 注解

使用可以参考单元测试：`io.github.opensabe.common.redisson.test.RedissonRateLimiterTest`以及注解本身的注释

**@RedissonRateLimiterName 与前面 @RedissonLockName 的用法一样，用来根据方法参数指定名称。如果没有这个注解，可以通过指定 RedissonRateLimiter 的 name 来指定名称，但是这样所有调用这个方法的限流器都是同一个限流器。下面就是我们针对这个 testBlockAcquire 方法整体做限流，无论参数是什么都是同一个限流器，限流为每秒一个**
```
		@RedissonRateLimiter(
                name = "testBlockAcquire",
                type = RedissonRateLimiter.Type.BLOCK,
                rate = 1,
                rateInterval = 1,
                rateType = RateType.OVERALL,
                rateIntervalUnit = RateIntervalUnit.SECONDS
        )
		public void testBlockAcquire(String test) 
```

**下面这个就是通过 RedissonRateLimiterName 指定某个参数为限流器名称，根据参数不同使用不同的限流器（就算指定了 name 参数，但是还是以 RedissonRateLimiterName 注解优先）**
```
		@RedissonRateLimiter(
                name = "testBlockAcquire",
                type = RedissonRateLimiter.Type.BLOCK,
                rate = 1,
                rateInterval = 1,
                rateType = RateType.OVERALL,
                rateIntervalUnit = RateIntervalUnit.SECONDS
        )
		public void testBlockAcquire(@RedissonRateLimiterName String permitsName) 
```

## Redisson Semaphore 注解（底层实现基于 RPermitExpirableSemaphore 而不是不严谨的 RSemaphore，这里更注重严谨而不是性能）

使用可以参考单元测试：`io.github.opensabe.common.redisson.test.RedissonSemaphoreTest` 以及注解本身的注释

RSemaphore 简单基于 Redis 的 add 命令，有程序异常退出，导致 permit 一直不恢复或者最大 permit 被加到比限制更多的值的情况，所以使用 RPermitExpirableSemaphore 通过多个 key 并带有过期时间判断准确的限流，并且程序退出，不会影响释放。
