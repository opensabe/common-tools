package io.github.opensabe.scheduler.utils;

import io.github.opensabe.scheduler.job.JobBriefInfo;
import io.github.opensabe.scheduler.server.SchedulerServer;

import java.util.Collection;
import java.util.stream.Collectors;

public class JobStatisticsAPI {

    private final SchedulerServer schedulerServer;

    public JobStatisticsAPI(SchedulerServer schedulerServer) {
        this.schedulerServer = schedulerServer;
    }

    public Collection<JobBriefInfo> getAllJobsInfo() {
        return this.schedulerServer.getJobs().values().stream().map(schedulerJob -> JobBriefInfo.builder().jobId(schedulerJob.getJobId()).jobName(schedulerJob.getJobName()).cronExpression(schedulerJob.getCronExpression()).status(schedulerJob.getStatus()).build()).collect(Collectors.toList());
    }
}
