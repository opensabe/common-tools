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

**首先，特别说明下，对于 Redisson 锁，不一定需要指定过期时间，因为 Redisson Rlock 不设置超时时间，并不代表底层 Key 没有超时时间，而是这个进程定时续期的。如果进程死掉或者 JVM 卡住 1min 以上也会释放锁。**

## 新版本升级

- io.github.opensabe.common.redisson.annotation.@RedissonLock已经被io.github.opensabe.common.redisson.annotation.slock.@SLock替代
- 为了简化配置，将lockFeature封装为不同的注解：
    - io.github.opensabe.common.redisson.annotation.slock.@RedissonLock `（LockFeature.default）`
    - io.github.opensabe.common.redisson.annotation.slock.@FairLock `（LockFeature.FAIR）`
    - io.github.opensabe.common.redisson.annotation.slock.@FencedLock `（LockFeature.SPIN）`
    - io.github.opensabe.common.redisson.annotation.slock.@ReadWriteLock `（LockFeature.READ_WRITE）`
    - io.github.opensabe.common.redisson.annotation.slock.@SpinLock `（LockFeature.FENCED）`
- 原来的@RedissonLockName, @RedissonRateLimiterName , @RedissonSemaphoreName已经废弃，标为过时 @Deprecated(removal=true)，原因：
    - 可能出现业务中并不需要该参数，但为了获取redisson锁名称，必须添加一个无用的形参，代码不优雅
    - 不注意会出现@RedissonLock跟@RedissonLockName分别在子类跟父类方法上
    - 不支持多个参数参与表达式
    - 基于多个参数参与表达式，SLock支持了MultipleLock
- 旧版写法依然生效，但是为了督促尽快升级，编译会报警告，甚至红色报错

```java
import io.github.opensabe.common.redisson.annotation.slock.RedissonLock;

/**
 * 新版本可以用student和permitsName拼接作为限流器名称,旧版本做不到
 */
@RedissonLock(name = "#permitsName+#student.name")
public void testBlockAcquire(String permitsName, tudent student);

/**
 * 同时获取两个锁，根据name先后顺序，依次锁permitsName+student.name和permitsName+student.id
 * multipleLock
 */
@RedissonLock(name = {"#permitsName+#student.name", "#permitsName+#student.id"})
public void testBlockAcquire(String permitsName, tudent student);
```



**对于某个 bean 的某个方法使用 redisson 阻塞锁**：

```java
//旧版本
@Deprecated
@RedissonLock(name="#name")
public void defaultLock(String name) {
}

/**
 * 新版，注意，这里的@RedissonLock等价于原来的@RedissonLock(lockFeature = RedissonLock.LockFeature.DEFAULT)。
 * @param name lockName
 */
@io.github.opensabe.common.redisson.annotation.slock.RedissonLock(name = "#name")
public void defaultLock(String name) {
}
```
**fairLock**

```java
import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.annotation.RedissonLockName;
import io.github.opensabe.common.redisson.annotation.slock.SLock;

//旧版本
@Deprecated
@RedissonLock(lockFeature = RedissonLock.LockFeature.FAIR)
public void fairLock(@RedissonLockName String name) throws InterruptedException {
}

//新版
@SLock(name = "#name", lockFeature = SLock.LockFeature.FAIR)
public void fairLock(String name) throws InterruptedException {
}
//或者
@FiarLock(name = "#name")
public void fairLock(String name) throws InterruptedException {
}

```
**FencedLock**

```java
import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.annotation.RedissonLockName;
import io.github.opensabe.common.redisson.annotation.slock.FencedLock;
import io.github.opensabe.common.redisson.annotation.slock.SLock;

//旧版本

@Deprecated
@RedissonLock(lockFeature = RedissonLock.LockFeature.FENCED)
public void fencedLock(@RedissonLockName String name) throws InterruptedException {
}

//新版
@SLock(name = "#name", lockFeature = SLock.LockFeature.FENCED)
public void fencedLock(String name) throws InterruptedException {
}

//或者
@FencedLock(name = "#name")
public void fencedLock(String name) throws InterruptedException {
}

```
**SpinLock**

```java
import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.annotation.RedissonLockName;
import io.github.opensabe.common.redisson.annotation.slock.SLock;
import io.github.opensabe.common.redisson.annotation.slock.SpinLock;

//旧版本

@Deprecated
@RedissonLock(lockFeature = RedissonLock.LockFeature.SPIN, backOffType = RedissonLock.BackOffType.CONSTANT)
public void spinLock(@RedissonLockName String name) throws InterruptedException {
}

//新版
@SLock(name = "#name", lockFeature = SLock.LockFeature.SPIN, backOffType = SLock.BackOffType.CONSTANT)
public void spinLock(String name) throws InterruptedException {
}

//或者
@SpinLock(name = "#name", backOffType = SLock.BackOffType.CONSTANT)
public void spinLock(String name) throws InterruptedException {
}

```
**ReadWriteLock**

```java
import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.annotation.RedissonLockName;
import io.github.opensabe.common.redisson.annotation.slock.ReadWriteLock;
import io.github.opensabe.common.redisson.annotation.slock.SLock;
import io.github.opensabe.common.redisson.annotation.slock.SpinLock;

//旧版本

@Deprecated
@RedissonLock(lockFeature = RedissonLock.LockFeature.READ_WRITE, readOrWrite = RedissonLock.ReadOrWrite.WRITE)
public void readWriteLock(@RedissonLockName String name) throws InterruptedException {
}

//新版
@SLock(name = "#name", lockFeature = SLock.LockFeature.READ_WRITE, readOrWrite = SLock.ReadOrWrite.WRITE)
public void readWriteLock(String name) throws InterruptedException {
}

//或者
@ReadWriteLock(name = "#name", readOrWrite = SLock.ReadOrWrite.WRITE)
public void readWriteLock(String name) throws InterruptedException {
}

```

之后，通过 bean 调用 `**Lock` 方法，就会根据参数 name 的值，**默认的锁前缀名称为**:`redisson:lock:`，获取 redisson 锁并阻塞锁住。之后才会执行方法体，执行完之后，释放锁。

**对于某个 bean 的某个方法使用 redisson 非阻塞锁**

```java
import io.github.opensabe.common.redisson.annotation.RedissonLockName;
import io.github.opensabe.common.redisson.annotation.slock.*;

//旧版本
@Deprecated
@io.github.opensabe.common.redisson.annotation.RedissonLock(lockType = RedissonLock.TRY_LOCK, waitTime = 10000, timeUnit = TimeUnit.MILLISECONDS)
public void testTryLock(@RedissonLockName String name) throws InterruptedException {
}

/**
 * 新版本
 */
//@SLock(name = "#name", lockType = SLock.TRY_LOCK, waitTime = 10000, timeUnit = TimeUnit.MILLISECONDS)
//@FairLock(name = "#name", lockType = SLock.TRY_LOCK, waitTime = 10000, timeUnit = TimeUnit.MILLISECONDS)
//@FencedLock(name = "#name", lockType = SLock.TRY_LOCK, waitTime = 10000, timeUnit = TimeUnit.MILLISECONDS)
//@ReadWriteLock(name = "#name", lockType = SLock.TRY_LOCK, waitTime = 10000, timeUnit = TimeUnit.MILLISECONDS)
@RedissonLock(name = "#name", lockType = SLock.TRY_LOCK, waitTime = 10000, timeUnit = TimeUnit.MILLISECONDS)
public void testTryLock(String name) throws InterruptedException {
}
```
需要指定尝试获取的等待时间。超过这个时间没有获取锁则会抛出 `RedissonClientException`，不会执行方法体。

**对于某个 bean 的某个方法使用 redisson 非阻塞锁，获取失败立刻返回**

```java

import io.github.opensabe.common.redisson.annotation.RedissonLockName;
import io.github.opensabe.common.redisson.annotation.slock.*;

//旧版本

@Deprecated
@io.github.opensabe.common.redisson.annotation.RedissonLock(name = "#name", lockType = RedissonLock.TRY_LOCK_NOWAIT)
public void testTryLockNoWait(@RedissonLockName String name) throws InterruptedException {
}

//新版
@RedissonLock(name = "#name", lockType = SLock.TRY_LOCK_NOWAIT)
//@FencedLock(name = "#name", lockType = SLock.TRY_LOCK_NOWAIT)
//@ReadWriteLock(name = "#name", lockType = SLock.TRY_LOCK_NOWAIT)
//@SpinLock(name = "#name", lockType = SLock.TRY_LOCK_NOWAIT)
//@FairLock(name = "#name", lockType = SLock.TRY_LOCK_NOWAIT)
public void testTryLockNoWait(String name) throws InterruptedException {
}
```
尝试获取锁，获取失败立刻抛出 `RedissonLockException`，不会执行方法体。

**通过参数 leaseTime，限制获取锁的最长时间**

```java

import io.github.opensabe.common.redisson.annotation.RedissonLockName;
import io.github.opensabe.common.redisson.annotation.slock.RedissonLock;

/**
 * 旧版本
 */
@Deprecated
@io.github.opensabe.common.redisson.annotation.RedissonLock(leaseTime = 1000L)
public void testLockTime(@RedissonLockName String name) throws InterruptedException {
}

/**
 * 新版本
 */
@RedissonLock(name = "#name", leaseTime = 1000L)
public void testLockTime(String name) throws InterruptedException {
}
```
**指定锁名称的前缀，通过 SPEL 表达式从对象中获取锁属性名**

```java

import io.github.opensabe.common.redisson.annotation.RedissonLockName;
import io.github.opensabe.common.redisson.annotation.slock.RedissonLock;

/**
 * 旧版本
 */
@Deprecated
@io.github.opensabe.common.redisson.annotation.RedissonLock
public void testRedissonLockNameProperty(@RedissonLockName(prefix = "test:", expression = "#{name==null?id:name}") Student student, String params) throws InterruptedException {
}

/**
 * 新版本
 */
@RedissonLock(prefix = "test:", expression = "#student.name==null?#student.id+#params:#student.name+#params")
public void testRedissonLockNameProperty(Student student, String params) throws InterruptedException {
}
```

使用可以参考单元测试：`io.github.opensabe.common.redisson.test.RedissonLockTest`以及注解本身的注释

## Redisson RateLimiter 注解

使用可以参考单元测试：`io.github.opensabe.common.redisson.test.RedissonRateLimiterTest`以及注解本身的注释


**通过指定 RedissonRateLimiter 的 name 来指定名称，但是这样所有调用这个方法的限流器都是同一个限流器。下面就是我们针对这个 testBlockAcquire 方法整体做限流，无论参数是什么都是同一个限流器，限流为每秒一个**
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

**RedissonRateLimiterName跟 RedissonLockName 已经废弃，相关功能移到 @RedissonRateLimiter和@RedissonLock里express用name代替**

~~**下面这个就是通过 RedissonRateLimiterName 指定某个参数为限流器名称，根据参数不同使用不同的限流器（就算指定了 name 参数，但是还是以 RedissonRateLimiterName 注解优先）**~~

```java

import io.github.opensabe.common.redisson.annotation.RedissonRateLimiterName;

/**
 * 旧版
 */
@Deprecated
@RedissonRateLimiter(
        type = RedissonRateLimiter.Type.BLOCK,
        rate = 1,
        rateInterval = 1,
        rateType = RateType.OVERALL,
        rateIntervalUnit = RateIntervalUnit.SECONDS
)
void testBlockAcquire(@RedissonRateLimiterName String permitsName);

@Deprecated
@RedissonRateLimiter
public void testBlockAcquire(String permitsName, @RedissonRateLimiterName(expression = "#{name}") Student student);
/**
 * 新版
 */
@RedissonRateLimiter(
        name = "#permitsName",
        type = RedissonRateLimiter.Type.BLOCK,
        rate = 1,
        rateInterval = 1,
        rateType = RateType.OVERALL,
        rateIntervalUnit = RateIntervalUnit.SECONDS
)
void testBlockAcquire(String permitsName);

/**
 * 新版本可以用student和permitsName拼接作为限流器名称,旧版本做不到
 */
@RedissonRateLimiter(name = "#permitsName+#student.name")
public void testBlockAcquire(String permitsName, Student student);
```

## Redisson Semaphore 注解（底层实现基于 RPermitExpirableSemaphore 而不是不严谨的 RSemaphore，这里更注重严谨而不是性能）

使用可以参考单元测试：`io.github.opensabe.common.redisson.test.1` 以及注解本身的注释

RSemaphore 简单基于 Redis 的 add 命令，有程序异常退出，导致 permit 一直不恢复或者最大 permit 被加到比限制更多的值的情况，所以使用 RPermitExpirableSemaphore 通过多个 key 并带有过期时间判断准确的限流，并且程序退出，不会影响释放。
