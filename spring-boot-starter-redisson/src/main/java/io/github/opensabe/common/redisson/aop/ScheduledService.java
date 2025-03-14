package io.github.opensabe.common.redisson.aop;


/**
 *
 * @author hengma
 */
@FunctionalInterface
public interface ScheduledService {
    void run() throws Throwable;
}
