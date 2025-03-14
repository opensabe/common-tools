package io.github.opensabe.common.redisson.aop;

/**
 * 由于单继承的限制，抽象类改为接口，写代码时可以更灵活，
 * @since 1.2.0
 * @author hengma
 */
public interface RedissonScheduledService extends ScheduledService {
    /**
     * 定时任务名称，如果为空则取方法名加类名称
     */
    default String name() {
        return this.getClass().getSimpleName()+"#run()";
    };

    /**
     * 执行间隔
     * @return
     */
    long fixedDelay();

    /**
     * 初始延迟
     * @return
     */
    long initialDelay();

    boolean stopOnceShutdown();
}
