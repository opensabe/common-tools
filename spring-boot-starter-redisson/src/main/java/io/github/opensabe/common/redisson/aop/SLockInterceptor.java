package io.github.opensabe.common.redisson.aop;

import io.github.opensabe.common.redisson.annotation.SLock;
import io.github.opensabe.common.redisson.exceptions.RedissonLockException;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
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

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Class<?> clazz = invocation.getThis().getClass();
        SLock lock = findSLock(method, clazz);
        if (Objects.isNull(lock)) {
            log.error("RedissonLockInterceptor-invoke error! Cannot find corresponding LockProperties, method {} run without lock", method.getName());
            return invocation.proceed();
        }
        RLock[] locks = Arrays.stream(lock.name())
                .map(name -> evaluator.resolve(method, invocation.getThis(), invocation.getArguments(), name))
                .map(name -> lock.lockFeature().getLock(name, lock, redissonClient))
                .toArray(RLock[]::new);

        RLock rLock = redissonClient.getMultiLock(locks);

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

    SLock findSLock (Method method, Class<?> clazz) {
        SLock lock = AnnotatedElementUtils.findMergedAnnotation(method, SLock.class);

        if (Objects.isNull(lock)) {
            lock = AnnotatedElementUtils.findMergedAnnotation(clazz, SLock.class);
        }
        return lock;
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

