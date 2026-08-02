/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.redisson.aop.lock;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.aop.AbstractRedissonProperties;
import io.github.opensabe.common.redisson.exceptions.RedissonLockException;
import lombok.extern.log4j.Log4j2;

/**
 * 旧版 {@link io.github.opensabe.common.redisson.annotation.RedissonLock} 方法拦截器，负责加锁与释放。
 */
@Log4j2
public class RedissonLockInterceptor implements MethodInterceptor {

    /** Redisson 客户端。 */
    private final RedissonClient redissonClient;

    /** 锁属性切点。 */
    private final RedissonLockCachedPointcut redissonLockCachedPointcut;


    /**
     * @param redissonClient Redisson 客户端
     * @param redissonLockCachedPointcut 锁切点
     */
    public RedissonLockInterceptor(RedissonClient redissonClient, RedissonLockCachedPointcut redissonLockCachedPointcut) {
        this.redissonClient = redissonClient;
        this.redissonLockCachedPointcut = redissonLockCachedPointcut;
    }

    /** {@inheritDoc} — 解析锁名、加锁、执行业务方法并在 finally 中释放。 */
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
        RLock lock = redissonLock.lockFeature().getLock(lockName, redissonLock, redissonClient);
        try {
            boolean getLock = RedissonLock.LockType.lockType(redissonLock.lockType()).lock(redissonLock, lock);
            if (!getLock) {
                throw new RedissonLockException("can not get redisson lock,method:" + method.getName() + ", params: " + Arrays.toString(invocation.getArguments()));
            } else {
                log.info("RedissonLockInterceptor-invoke successfully locked lockName {}, method: {}, threadId: {}",
                        lockName, method.getName(), Thread.currentThread().threadId());
            }
            return invocation.proceed();
        } finally {
            release(lock, method);
        }

    }

    /**
     * 释放锁；若 Redisson 内部线程池拒绝则退避重试。
     *
     * @param lock 待释放锁
     * @param method 被拦截方法（仅用于日志）
     */
    private void release(RLock lock, Method method) {
        boolean locked = lock.isLocked() && lock.isHeldByCurrentThread();
        int count = 0;
        while (locked) {
            try {
                lock.unlock();
                log.debug("RedissonLockInterceptor-release redisson lock {} released, method: {}, threadId: {}", lock.getName(), method.getName(), Thread.currentThread().threadId());
                break;
            } catch (Throwable e) {
                log.fatal("error during release redisson lock {}, {}, count: {}", lock.getName(), e.getMessage(), count, e);
                // Retry on thread-pool rejection (Redisson may use a saturated common ForkJoinPool)
                if (e instanceof RejectedExecutionException) {
                    locked = lock.isLocked() && lock.isHeldByCurrentThread();
                    count++;
                    log.debug("release redisson failed because of rejected, retry unlock {}", lock.getName());
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException ignore) {
                    }
                } else {
                    break;
                }
            }
        }
    }
}
