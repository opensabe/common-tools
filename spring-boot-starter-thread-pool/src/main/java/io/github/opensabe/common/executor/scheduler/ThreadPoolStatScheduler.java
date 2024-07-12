package io.github.opensabe.common.executor.scheduler;

import io.github.opensabe.common.executor.NamedThreadPoolExecutor;
import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.jfr.ThreadPoolStatJFREvent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
