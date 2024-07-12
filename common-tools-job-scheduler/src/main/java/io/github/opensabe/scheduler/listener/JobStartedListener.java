package io.github.opensabe.scheduler.listener;

import io.github.opensabe.scheduler.conf.JobStatus;
import io.github.opensabe.scheduler.job.SchedulerJob;

public interface JobStartedListener extends JobListener {

    default void jobStarted(SchedulerJob job) {
        job.setStatus(JobStatus.STARTED);
    }
}
