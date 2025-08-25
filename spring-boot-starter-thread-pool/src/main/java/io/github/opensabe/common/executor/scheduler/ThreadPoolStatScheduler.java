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
package io.github.opensabe.common.executor.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.opensabe.common.executor.NamedThreadPoolExecutor;
import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.jfr.ThreadPoolStatJFREvent;

@SuppressFBWarnings("EI_EXPOSE_REP2")
public class ThreadPoolStatScheduler {

    private final ThreadPoolFactory threadPoolFactory;
    private final ScheduledExecutorService executor;


    public ThreadPoolStatScheduler(ThreadPoolFactory threadPoolFactory) {
        this.threadPoolFactory = threadPoolFactory;
        executor = threadPoolFactory.createScheduledThreadPoolExecutor("JFR-Schedule", 1);
        executor.scheduleWithFixedDelay(this::recordStat, 0, 1, TimeUnit.MINUTES);
    }

    public void recordStat() {
        threadPoolFactory.getAllExecutors().forEach(executorServiceWeakReference -> {
            ExecutorService executorService = executorServiceWeakReference.get();
            if (executorService != null) {
                ThreadPoolStatJFREvent jfrEvent;
                if (executorService instanceof NamedThreadPoolExecutor) {
                    NamedThreadPoolExecutor namedThreadPoolExecutor = (NamedThreadPoolExecutor) executorService;
                    jfrEvent = new ThreadPoolStatJFREvent(namedThreadPoolExecutor.getName(), namedThreadPoolExecutor);
                    if (jfrEvent.getQueueSize() > 0) {
                        jfrEvent.begin();
                        jfrEvent.commit();
                    }
                }
            }
        });
    }
}
