package io.github.opensabe.common.redisson.aop.lock;

import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.aop.AbstractRedissonProperties;
import io.github.opensabe.common.redisson.exceptions.RedissonLockException;
import lombok.extern.log4j.Log4j2;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * redisson 锁核心实现类
 */
@Log4j2
public class RedissonLockInterceptor implements MethodInterceptor {
    private final RedissonClient redissonClient;
    private final RedissonLockCachedPointcut redissonLockCachedPointcut;


    public RedissonLockInterceptor(RedissonClient redissonClient, RedissonLockCachedPointcut redissonLockCachedPointcut) {
        this.redissonClient = redissonClient;
        this.redissonLockCachedPointcut = redissonLockCachedPointcut;
    }

    /**
     * 新的 local name支持前缀及el表达式
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object target = invocation.getThis();
        RedissonLockProperties redissonLockProperties = redissonLockCachedPointcut.getRedissonProperties(method, target.getClass());
        if (redissonLockProperties == null || redissonLockProperties == AbstractRedissonProperties.NONE) {
            log.error("RedissonLockInterceptor-invoke error! Cannot find corresponding LockProperties, method {} run without lock", method.getName());
            return invocation.proceed();
        }
        String lockName = redissonLockProperties.resolve(method, invocation.getThis(), invocation.getArguments());
        RedissonLock redissonLock = redissonLockProperties.getRedissonLock();
        log.debug("RedissonLockInterceptor-invoke begin to try redisson lockName {}, method: {}, thread: {}", lockName, method.getName(), Thread.currentThread().getName());
        //创建锁
        RLock lock = redissonLock.lockFeature().getLock(lockName, redissonLock, redissonClient);
        try {
            boolean getLock = RedissonLock.LockType.lockType(redissonLock.lockType()).lock(redissonLock, lock);
            if (!getLock) {
                throw new RedissonLockException("can not get redisson lock,method:" + method.getName() + ", params: " + Arrays.toString(invocation.getArguments()));
            } else {
                log.info("RedissonLockInterceptor-invoke successfully locked lockName {}, method: {}, threadId: {}",
                        lockName, method.getName(), Thread.currentThread().getId());
            }
            //执行方法
            return invocation.proceed();
        } finally {
            //释放锁
            release(lock, method);
        }

    }

    /**
     * 释放锁，如果释放失败，重试
     * @param lock
     * @param method
     */
    private void release(RLock lock, Method method) {
        boolean locked = lock.isLocked() && lock.isHeldByCurrentThread();
        int count = 0;
        while (locked) {
            try {
                lock.unlock();
                log.debug("RedissonLockInterceptor-release redisson lock {} released, method: {}, threadId: {}", lock.getName(), method.getName(), Thread.currentThread().getId());
                break;
            } catch (Throwable e) {
                log.fatal("error during release redisson lock {}, {}, count: {}", lock.getName(), e.getMessage(), count, e);
                //如果是线程池拒绝（Redisson 的线程池可能会满，可能用的是 common ForkJoinPool，而你又刚好使用 common ForkJoinPool 提交了很多 io 任务，例如 parallelStream 里面有 io），则重试
                if (e instanceof RejectedExecutionException) {
                    locked = lock.isLocked() && lock.isHeldByCurrentThread();
                    count++;
                    log.debug("release redisson failed because of rejected, retry unlock {}", lock.getName());
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException ex) {
                    }
                } else {
                    break;
                }
            }
        }
    }
}
