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
package io.github.opensabe.common.executor;

import java.lang.ref.Reference;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * {@link ThreadPoolFactory} 优雅关闭处理器。
 * <p>
 * 在应用关闭时轮询所有已注册线程池，连续三次确认全部任务完成后才视为关闭成功。
 */
@Log4j2
public class ThreadPoolFactoryGracefulShutDownHandler implements GracefulShutdownHandler {

    /**
     * 线程池工厂，用于获取所有已创建的线程池引用。
     */
    private final ThreadPoolFactory threadPoolFactory;

    /**
     * 是否正在执行优雅关闭。
     */
    @Getter
    private volatile boolean isShuttingDown;

    /**
     * @param threadPoolFactory 线程池工厂
     */
    public ThreadPoolFactoryGracefulShutDownHandler(ThreadPoolFactory threadPoolFactory) {
        this.threadPoolFactory = threadPoolFactory;
    }

    /**
     * 标记关闭中，并以随机顺序轮询所有线程池直至连续三次全部完成。
     */
    @Override
    public void gracefullyShutdown() {
        isShuttingDown = true;
        log.info("ThreadPoolFactoryGracefulShutDownHandler-onApplicationEvent shutdownSuccessful");
        List<ExecutorService> executorServices = this.threadPoolFactory.getAllExecutors().stream().map(Reference::get).filter(Objects::nonNull).collect(Collectors.toList());
        for (int i = 0; i < 3;) {
            Collections.shuffle(executorServices);
            if (executorServices.stream().allMatch(ThreadPoolFactory::isCompleted)) {
                i++;
                log.info("all threads pools are completed, i: {}", i);
            } else {
                i = 0;
                log.info("not all threads pools are completed, wait for 1s");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    /**
     * @return 最低优先级，确保在其他关闭逻辑之后执行
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
