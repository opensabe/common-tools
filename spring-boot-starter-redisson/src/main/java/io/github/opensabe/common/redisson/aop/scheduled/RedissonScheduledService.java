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
package io.github.opensabe.common.redisson.aop.scheduled;

/**
 * 基于 Redisson 选主的分布式定时任务服务接口。
 * <p>
 * 相比抽象类，接口形式便于与 Spring 代理及多继承场景组合。
 *
 * @since 1.2.0
 */
public interface RedissonScheduledService extends ScheduledService {

    /**
     * 定时任务唯一名称；默认 {@code 类简单名#run()}。
     *
     * @return 任务名称
     */
    default String name() {
        return this.getClass().getSimpleName() + "#run()";
    }

    /**
     * 上次执行结束与下次开始之间的固定间隔（毫秒）。
     *
     * @return 间隔毫秒数
     */
    long fixedDelay();

    /**
     * 首次调度前的初始延迟（毫秒）。
     *
     * @return 延迟毫秒数
     */
    long initialDelay();

    /**
     * 容器关闭时是否立即中断进行中的任务。
     *
     * @return {@code true} 表示 {@code shutdownNow}
     */
    boolean stopOnceShutdown();
}
