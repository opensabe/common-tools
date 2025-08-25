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
