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
