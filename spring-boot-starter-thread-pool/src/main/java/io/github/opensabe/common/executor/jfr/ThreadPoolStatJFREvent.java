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
package io.github.opensabe.common.executor.jfr;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import lombok.Getter;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;

@Category({"Thread Pool"})
@Label("Thead Pool Stat")
@SuppressFBWarnings("URF_UNREAD_FIELD")
public class ThreadPoolStatJFREvent extends Event {

    private String name;
    private String type;
    private int corePoolSize;
    private int largestPoolSize;
    private int maximumPoolSize;
    private int activeCount;
    private int poolSize;
    @Getter
    private int queueSize;

    public ThreadPoolStatJFREvent(String name, ThreadPoolExecutor executor){
        this.name = name;
        this.type = "BasePool";
        this.corePoolSize = executor.getCorePoolSize();
        this.largestPoolSize = executor.getLargestPoolSize();
        this.maximumPoolSize = executor.getMaximumPoolSize();
        this.activeCount = executor.getActiveCount();
        this.poolSize = executor.getPoolSize();
        this.queueSize = executor.getQueue().size();
    }

    public ThreadPoolStatJFREvent(String name, ForkJoinPool executor){
        this.name = name;
        this.type = "ForkJoinPool";
        this.corePoolSize = -1;
        this.largestPoolSize = -1;
        this.maximumPoolSize = -1;
        this.activeCount = executor.getActiveThreadCount();
        this.poolSize = executor.getPoolSize();
        this.queueSize = executor.getQueuedSubmissionCount();
    }
}
