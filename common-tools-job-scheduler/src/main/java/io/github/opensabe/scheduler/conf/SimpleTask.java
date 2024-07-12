package io.github.opensabe.scheduler.conf;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SimpleTask {
    /**
     * job名字，不设置默认为类名
     *
     * @return job名字，
     */
    String jobName() default "";

    /**
     * 调度cron表达式，必须设置不为空
     *
     * @return 调度cron表达式
     */
    String cron() default "";

    /**
     * 哑火是否重试，默认重试
     *
     * @return 哑火是否重试
     */
    boolean misfire() default false;

    /**
     * 分片个数，默认为1不分片
     *
     * @return 分片个数
     */
    int shardingCount() default 1;

    /**
     * 是否持久化到数据库，默认持久化
     *
     * @return 是否持久化到数据库
     */
    boolean isWrittenToDb() default true;

    /**
     * 本地配置是否可覆盖注册中心配置
     * 默认覆盖，每次启动作业都以本地配置为准
     *
     * @return 是否可覆盖注册中心配置
     */
    boolean isOverwrite() default true;
}
