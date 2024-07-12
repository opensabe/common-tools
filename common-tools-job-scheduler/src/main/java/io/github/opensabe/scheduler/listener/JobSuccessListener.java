package io.github.opensabe.scheduler.listener;

import io.github.opensabe.scheduler.conf.JobStatus;
import io.github.opensabe.scheduler.job.SchedulerJob;

public interface JobSuccessListener extends JobListener {

    default void jobSuccess(SchedulerJob job) {
        job.setStatus(JobStatus.SUCCESS);
    }
}
