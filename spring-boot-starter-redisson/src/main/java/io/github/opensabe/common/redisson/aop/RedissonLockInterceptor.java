package io.github.opensabe.common.redisson.aop;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.annotation.RedissonLockName;
import io.github.opensabe.common.redisson.exceptions.RedissonLockException;
import lombok.extern.log4j.Log4j2;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

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
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParserContext context = new TemplateParserContext();
    private final UnifiedObservationFactory unifiedObservationFactory;

    public RedissonLockInterceptor(RedissonClient redissonClient, RedissonLockCachedPointcut redissonLockCachedPointcut, UnifiedObservationFactory unifiedObservationFactory) {
        this.redissonClient = redissonClient;
        this.redissonLockCachedPointcut = redissonLockCachedPointcut;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    /**
     * 新的 local name支持前缀及el表达式
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Class<?> clazz = invocation.getThis().getClass();
        RedissonLockProperties redissonLockProperties = redissonLockCachedPointcut.getRedissonProperties(method, clazz);
        if (redissonLockProperties == null || redissonLockProperties == AbstractRedissonProperties.NONE) {
            log.error("RedissonLockInterceptor-invoke error! Cannot find corresponding LockProperties, method {} run without lock", method.getName());
            return invocation.proceed();
        }
        String lockName = getLockName(redissonLockProperties, invocation.getArguments());
        redissonLockProperties.setLockName(lockName);
        RedissonLock redissonLock = redissonLockProperties.getRedissonLock();
        log.debug("RedissonLockInterceptor-invoke begin to try redisson lockName {}, method: {}, thread: {}", lockName, method.getName(), Thread.currentThread().getName());
        //创建锁
        RLock lock = redissonLock.lockFeature().getLock(lockName, redissonLock, redissonClient);
//        RedissonLock.LockFeature lockFeature = redissonLock.lockFeature();
//        if (lockFeature == RedissonLock.LockFeature.DEFAULT) {
//            lock = redissonClient.getLock(lockName);
//        } else if (lockFeature == RedissonLock.LockFeature.FAIR) {
//            lock = redissonClient.getFairLock(lockName);
//        } else if (lockFeature == RedissonLock.LockFeature.SPIN) {
//            RedissonLock.BackOffType backOffType = redissonLock.backOffType();
//            if (backOffType == RedissonLock.BackOffType.CONSTANT) {
//                lock = redissonClient.getSpinLock(lockName, new LockOptions.ConstantBackOff()
//                        .delay(redissonLock.backOffDelay()));
//            } else if (backOffType == RedissonLock.BackOffType.EXPONENTIAL) {
//                lock = redissonClient.getSpinLock(lockName, new LockOptions.ExponentialBackOff()
//                        .initialDelay(redissonLock.backOffInitialDelay())
//                        .maxDelay(redissonLock.backOffMaxDelay())
//                        .multiplier(redissonLock.backOffMultiplier()));
//            } else {
//                throw new RedissonLockException("Not implemented BackOffType: " + backOffType);
//            }
//        } else if (lockFeature == RedissonLock.LockFeature.READ_WRITE) {
//            if (redissonLock.readOrWrite() == RedissonLock.ReadOrWrite.READ) {
//                lock = redissonClient.getReadWriteLock(lockName).readLock();
//            } else {
//                lock = redissonClient.getReadWriteLock(lockName).writeLock();
//            }
//        } else {
//            throw new RedissonLockException("Not implemented LockFeature: " + lockFeature);
//        }
//        if (lock == null) {
//            log.error("RedissonLockInterceptor-invoke {} err! error during create redisson lock!", method.getName());
//            return invocation.proceed();
//        }
        try {
            boolean getLock = RedissonLock.LockType.lockType(redissonLock.lockType()).lock(redissonLock, lock);
//            switch (redissonLock.lockType()) {
//                case RedissonLock.BLOCK_LOCK:
//                    lock.lock(redissonLock.leaseTime(), redissonLock.timeUnit()); //默认为-1，永久持有直接主动释放
//                    getLock = true;
//                    break;
//                case RedissonLock.TRY_LOCK_NOWAIT:
//                    getLock = lock.tryLock();
//                    break;
//                case RedissonLock.TRY_LOCK:
//                    getLock = lock.tryLock(redissonLock.waitTime(), redissonLock.leaseTime(), redissonLock.timeUnit());
//                    break;
//            }
            if (!getLock) {
                throw new RedissonLockException("can not get redisson lock,method:" + method.getName() + ", params: " + Arrays.toString(invocation.getArguments()));
            } else {
                log.debug("RedissonLockInterceptor-invoke successfully locked lockName {}, method: {}, threadId: {}",
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

    private String getLockName(RedissonLockProperties redissonLockProperties, Object... params) {
        RedissonLockName redissonLockName = redissonLockProperties.getRedissonLockName();
        StringBuilder lockName = new StringBuilder();
        if (redissonLockName != null) {
            int parameterIndex = redissonLockProperties.getParameterIndex();
            String prefix = redissonLockName.prefix();
            String expression = redissonLockName.expression();
            if (StringUtils.isNotBlank(expression)) {
                lockName.append(prefix).append(parser.parseExpression(expression, context).getValue(params[parameterIndex]));
            } else {
                lockName.append(prefix).append(params[parameterIndex]);
            }
        } else {
            lockName.append(redissonLockProperties.getRedissonLock().name());
        }
        return lockName.toString();
    }
}
