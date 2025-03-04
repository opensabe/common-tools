package io.github.opensabe.common.redisson.aop;

import io.github.opensabe.common.redisson.annotation.SLock;
import io.github.opensabe.common.redisson.exceptions.RedissonLockException;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RExpirable;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisResponseTimeoutException;
import org.springframework.expression.EvaluationException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author heng.ma
 */
@Log4j2
@RequiredArgsConstructor
public class SLockInterceptor implements MethodInterceptor {

    private final RedissonClient redissonClient;
    private final MethodArgumentsExpressEvaluator evaluator;

    private final SLockPointcut pointcut;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Class<?> clazz = invocation.getThis().getClass();
        SLock lock = pointcut.findSLock(method, clazz);
        if (Objects.isNull(lock)) {
            log.error("RedissonLockInterceptor-invoke error! Cannot find corresponding LockProperties, method {} run without lock", method.getName());
            return invocation.proceed();
        }
        RLock[] locks = Arrays.stream(lock.name())
                .map(name -> {
                    try {
                        String resolved = evaluator.resolve(method, invocation.getThis(), invocation.getArguments(), name);
                        if (StringUtils.isBlank(resolved)) {
                            //如果expression写了表达式，解析失败直接抛出异常
                            if (StringUtils.contains(name, "#")) {
                                throw new RedissonLockException("can not resolved redisson lock name , expression: "+name+"method:" + method.getName() + ", params: " + Arrays.toString(invocation.getArguments()));
                            }
                            //如果expression不是一个表达式，只是一个常量，直接用这个常量当锁名称
                            return lock.prefix() + name;
                        }
                        return lock.prefix() + resolved;
                    }catch (EvaluationException e) {
                        //如果expression写了表达式，解析失败直接抛出异常
                        if (StringUtils.contains(name, "#")) {
                            throw new RedissonLockException("can not resolved redisson lock name , expression: "+name+" method:" + method.getName() + ", params: " + Arrays.toString(invocation.getArguments()));
                        }
                        //如果expression不是一个表达式，只是一个常量，直接用这个常量当锁名称
                        return lock.prefix() + name;
                    }
                })
                .map(name -> lock.lockFeature().getLock(name, lock, redissonClient))
                .toArray(RLock[]::new);

        RLock rLock = new MLock(locks);

        boolean locked = lock.lockType().lock(lock, rLock);
        if (!locked) {
            throw new RedissonLockException("can not get redisson lock,method:" + method.getName() + ", params: " + Arrays.toString(invocation.getArguments()));
        }else {
            if (log.isDebugEnabled()) {
                log.debug("RedissonLockInterceptor-invoke successfully locked lockName {}, method: {}, threadId: {}",
                        Arrays.stream(locks).map(RLock::getName).collect(Collectors.joining(",")), method.getName(), Thread.currentThread().getId());
            }
        }
        try {
            return invocation.proceed();
        }finally {
            release(rLock, method);
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
            return locks.stream().map(RLock::isLocked).reduce(true, (a,b) -> a && b);
        }

        @Override
        public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
            long newLeaseTime = -1;
            if (leaseTime > 0) {
                if (waitTime > 0) {
                    newLeaseTime = unit.toMillis(waitTime)*2;
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
            for (ListIterator<RLock> iterator = locks.listIterator(); iterator.hasNext();) {
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
                    unlockInner(Arrays.asList(lock));
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
                        .map(l -> l.expireAsync(unit.toMillis(leaseTime), TimeUnit.MILLISECONDS))
                        .forEach(f -> f.toCompletableFuture().join());
            }

            return true;
        }
    }
}

