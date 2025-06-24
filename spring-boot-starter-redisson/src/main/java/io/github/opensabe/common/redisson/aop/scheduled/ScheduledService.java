package io.github.opensabe.common.redisson.aop.scheduled;


/**
 *
 * @author hengma
 */
@FunctionalInterface
public interface ScheduledService {
    void run() throws Throwable;
}
