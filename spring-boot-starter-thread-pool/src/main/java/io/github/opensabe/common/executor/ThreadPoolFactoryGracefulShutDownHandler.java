package io.github.opensabe.common.executor;

import io.github.opensabe.spring.cloud.parent.web.common.undertow.UndertowGracefulShutdownHandler;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;

import java.lang.ref.Reference;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        for (int i = 0; i < 3; ) {
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
