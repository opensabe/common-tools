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
package io.github.opensabe.common.redisson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明基于 Redisson 分布式选主的定时任务。
 * <p>
 * 同一 {@link #name()} 在集群中仅有一个实例执行；未指定名称时使用 {@code 类名#方法名}。
 *
 * @see io.github.opensabe.common.redisson.aop.scheduled.RedissonScheduledListener
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedissonScheduled {

    /** 定时任务唯一名称；为空则取 {@code 类简单名#方法名}。 */
    String name() default "";

    /** 上次执行结束与下次开始之间的固定间隔（毫秒）。 */
    long fixedDelay() default 1000;

    /** 首次调度前的初始延迟（毫秒）。 */
    long initialDelay() default 0;

    /** 容器关闭时是否立即中断进行中的任务。 */
    boolean stopOnceShutdown() default false;
}
