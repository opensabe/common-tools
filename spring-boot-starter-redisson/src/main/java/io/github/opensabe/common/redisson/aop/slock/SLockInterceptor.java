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
package io.github.opensabe.common.redisson.aop.slock;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RExpirable;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisResponseTimeoutException;

import io.github.opensabe.common.redisson.annotation.slock.SLock;
import io.github.opensabe.common.redisson.exceptions.RedissonLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @author heng.ma
 */
@Log4j2
@RequiredArgsConstructor
public class SLockInterceptor implements MethodInterceptor {

    private final RedissonClient redissonClient;

    private final SLockPointcut pointcut;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Class<?> clazz = invocation.getThis().getClass();
        SLockProperties properties = pointcut.findProperties(method, clazz);
        if (Objects.isNull(properties)) {
            log.error("RedissonLockInterceptor-invoke error! Cannot find corresponding LockProperties, method {} run without lock", method.getName());
            return invocation.proceed();
        }
        SLock lock = properties.getLock();
        RLock[] locks = Arrays.stream(lock.name())
                .map(name -> {
                    String resolved = properties.evaluator().resolve(method, invocation.getThis(), invocation.getArguments(), name);
                    if (StringUtils.isBlank(resolved)) {
                        throw new RedissonLockException("can not resolved redisson lock name , expression: " + name + " method:" + method.getName() + ", params: " + Arrays.toString(invocation.getArguments()));
                    }
                    return lock.prefix() + resolved;
                })
                .map(name -> lock.lockFeature().getLock(name, lock, redissonClient))
                .toArray(RLock[]::new);
        RLock rLock;
        if (locks.length == 1) {
            rLock = locks[0];
        } else {
            rLock = new MLock(locks);
        }

        boolean locked = lock.lockType().lock(lock, rLock);
        if (!locked) {
            throw new RedissonLockException("can not get redisson lock,method:" + method.getName() + ", params: " + Arrays.toString(invocation.getArguments()));
        } else {
            if (log.isDebugEnabled()) {
                log.debug("RedissonLockInterceptor-invoke successfully locked lockName {}, method: {}, threadId: {}",
                        Arrays.stream(locks).map(RLock::getName).collect(Collectors.joining(",")), method.getName(), Thread.currentThread().threadId());
            }
        }
        try {
            return invocation.proceed();
        } finally {
            release(rLock, method);
        }
    }


    /**
     * 释放锁，如果释放失败，重试
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
                //如果是线程池拒绝（Redisson 的线程池可能会满，可能用的是 common ForkJoinPool，而你又刚好使用 common ForkJoinPool 提交了很多 io 任务，例如 parallelStream 里面有 io），则重试
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

    static class MLock extends RedissonMultiLock {
        private final List<RLock> locks;

        public MLock(RLock... locks) {
            super(locks);
            this.locks = Arrays.asList(locks);
        }

        @Override
        public String getName() {
            return locks.stream().map(RLock::getName).collect(Collectors.joining(","));
        }

        @Override
        public boolean isLocked() {
            return locks.stream().map(RLock::isLocked).reduce(true, (a, b) -> a && b);
        }

        @Override
        public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
            long newLeaseTime = -1;
            if (leaseTime > 0) {
                if (waitTime > 0) {
                    newLeaseTime = unit.toMillis(waitTime) * 2;
                } else {
                    newLeaseTime = unit.toMillis(leaseTime);
                }
            }

            long time = System.currentTimeMillis();
            long remainTime = -1;
            if (waitTime > 0) {
                remainTime = unit.toMillis(waitTime);
            }
            long lockWaitTime = calcLockWaitTime(remainTime);

            int failedLocksLimit = failedLocksLimit();
            List<RLock> acquiredLocks = new ArrayList<>(locks.size());
            for (ListIterator<RLock> iterator = locks.listIterator(); iterator.hasNext(); ) {
                RLock lock = iterator.next();
                boolean lockAcquired;
                try {
                    if (waitTime <= 0 && leaseTime <= 0) {
                        lockAcquired = lock.tryLock();
                    } else {
                        long awaitTime = Math.min(lockWaitTime, remainTime);
                        lockAcquired = lock.tryLock(awaitTime, newLeaseTime, TimeUnit.MILLISECONDS);
                    }
                } catch (RedisResponseTimeoutException e) {
                    unlockInner(Collections.singletonList(lock));
                    lockAcquired = false;
                } catch (Exception e) {
                    lockAcquired = false;
                }

                if (lockAcquired) {
                    acquiredLocks.add(lock);
                } else {
                    if (locks.size() - acquiredLocks.size() == failedLocksLimit()) {
                        break;
                    }

                    if (failedLocksLimit == 0) {
                        unlockInner(acquiredLocks);
                        if (waitTime <= 0) {
                            return false;
                        }
                        failedLocksLimit = failedLocksLimit();
                        acquiredLocks.clear();
                        // reset iterator
                        while (iterator.hasPrevious()) {
                            iterator.previous();
                        }
                    } else {
                        failedLocksLimit--;
                    }
                }

                if (remainTime > 0) {
                    remainTime -= System.currentTimeMillis() - time;
                    time = System.currentTimeMillis();
                    if (remainTime <= 0) {
                        unlockInner(acquiredLocks);
                        return false;
                    }
                }
            }

            if (leaseTime > 0) {
                acquiredLocks.stream()
                        .map(l -> (RExpirable) l)
                        .map(l -> l.expireAsync(Duration.of(leaseTime, unit.toChronoUnit())))
                        .forEach(f -> f.toCompletableFuture().join());
            }

            return true;
        }
    }
}

