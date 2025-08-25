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
