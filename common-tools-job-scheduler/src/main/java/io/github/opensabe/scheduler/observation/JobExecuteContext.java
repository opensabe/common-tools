package io.github.opensabe.scheduler.observation;

import io.github.opensabe.common.utils.BeanUtils;
import io.github.opensabe.scheduler.conf.JobStatus;
import io.github.opensabe.scheduler.job.SchedulerJob;
import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
public class JobExecuteContext extends Observation.Context {

    private String jobId;
    private String jobName;
    private String cronExpression;
    private JobStatus status;

    public JobExecuteContext(SchedulerJob schedulerJob) {
        BeanUtils.copyProperties(schedulerJob, this);
        if (Objects.isNull(this.status)) {
            this.status = JobStatus.READY;
        }
    }
}
