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
