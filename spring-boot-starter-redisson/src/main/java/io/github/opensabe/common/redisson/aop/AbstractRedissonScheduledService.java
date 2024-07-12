package io.github.opensabe.common.redisson.aop;

public abstract class AbstractRedissonScheduledService {
    /**
     * 定时任务名称，如果为空则取方法名加类名称
     */
    abstract public String name();

    /**
     * 执行间隔
     * @return
     */
    abstract public long fixedDelay();

    /**
     * 初始延迟
     * @return
     */
    abstract public long initialDelay();

    abstract public boolean stopOnceShutdown();

    abstract public void run();
}
