package io.github.opensabe.scheduler.listener;

import io.github.opensabe.scheduler.conf.JobStatus;
import io.github.opensabe.scheduler.job.SchedulerJob;

public interface JobFinishedListener extends JobListener {

    default void jobFinished(SchedulerJob job) {
        job.setStatus(JobStatus.FINISHED);
    }

}
