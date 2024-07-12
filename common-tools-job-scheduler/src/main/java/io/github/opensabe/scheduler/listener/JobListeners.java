package io.github.opensabe.scheduler.listener;

import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

public class JobListeners {

    private final List<JobListener> listeners;

    public JobListeners(JobListener... jobListeners){
        this(Arrays.asList(jobListeners));
    }

    public JobListeners(List<JobListener> listeners) {
        this.listeners = new ArrayList<>();
        addAll(listeners);
    }

    public void addAll(List<? extends JobListener> listeners) {
        if (CollectionUtils.isEmpty(listeners)) {
            return;
        }
        this.listeners.addAll(listeners);
    }

    public List<JobListener> getListeners() {
        return listeners;
    }
}
