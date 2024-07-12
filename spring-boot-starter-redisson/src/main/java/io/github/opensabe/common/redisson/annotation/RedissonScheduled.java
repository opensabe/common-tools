package io.github.opensabe.common.redisson.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(
        {ElementType.METHOD}
)
public @interface RedissonScheduled {
    /**
     * 定时任务名称，如果为空则取方法名加类名称
     */
    String name() default "";

    /**
     * 执行间隔
     * @return
     */
    long fixedDelay() default 1000;

    /**
     * 初始延迟
     * @return
     */
    long initialDelay() default 0;
    
    boolean stopOnceShutdown() default false;
}
