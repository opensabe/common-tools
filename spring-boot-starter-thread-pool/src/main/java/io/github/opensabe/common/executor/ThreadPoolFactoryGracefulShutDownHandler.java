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

import io.github.opensabe.spring.cloud.parent.web.common.undertow.UndertowGracefulShutdownHandler;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ThreadPoolFactoryGracefulShutDownHandler implements UndertowGracefulShutdownHandler {

    private final ThreadPoolFactory threadPoolFactory;
    @Getter
    private volatile boolean isShuttingDown;

    public ThreadPoolFactoryGracefulShutDownHandler(ThreadPoolFactory threadPoolFactory) {
        this.threadPoolFactory = threadPoolFactory;
    }

    @Override
    public void gracefullyShutdown() {
        isShuttingDown = true;
        log.info("ThreadPoolFactoryGracefulShutDownHandler-onApplicationEvent shutdownSuccessful");
        List<ExecutorService> executorServices = this.threadPoolFactory.getAllExecutors().stream().map(Reference::get).filter(Objects::nonNull).collect(Collectors.toList());
        for (int i = 0; i < 3;) {
            //连续三次，以随机乱序检查所有的线程池都完成了，才认为是真正完成
            Collections.shuffle(executorServices);
            if (executorServices.stream().allMatch(ThreadPoolFactory::isCompleted)) {
                i++;
                log.info("all threads pools are completed, i: {}", i);
            } else {
                //连续三次
                i = 0;
                log.info("not all threads pools are completed, wait for 1s");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
