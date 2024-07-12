package io.github.opensabe.common.executor.autoconfig;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.config.UndertowThreadConfiguration;
import io.github.opensabe.common.executor.forkjoin.ForkJoinPoolFactory;
import io.github.opensabe.common.executor.forkjoin.ForkjoinTaskFactory;
import io.github.opensabe.common.executor.resilience4j.BulkheadThreadPoolConfig;
import io.github.opensabe.common.executor.scheduler.ThreadPoolStatScheduler;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({UndertowThreadConfiguration.class, BulkheadThreadPoolConfig.class})
public class ThreadPoolCommonConfiguration {
    @Bean
    public ThreadPoolFactory getThreadPoolFactory() {
        return new ThreadPoolFactory();
    }

    @Bean
    public ThreadPoolStatScheduler threadPoolStatScheduler(ThreadPoolFactory threadPoolFactory){
        return new ThreadPoolStatScheduler(threadPoolFactory);
    }

    @Bean
    public ForkJoinPoolFactory forkJoinPoolFactory (UnifiedObservationFactory unifiedObservationFactory, ThreadPoolFactory threadPoolFactory) {
        return new ForkJoinPoolFactory(threadPoolFactory,unifiedObservationFactory);
    }

    @Bean
    public ForkjoinTaskFactory forkjoinTaskFactory (UnifiedObservationFactory unifiedObservationFactory) {
        return new ForkjoinTaskFactory(unifiedObservationFactory);
    }
}
