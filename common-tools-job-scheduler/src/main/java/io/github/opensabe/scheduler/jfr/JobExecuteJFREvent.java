package io.github.opensabe.scheduler.jfr;

import io.github.opensabe.common.utils.BeanUtils;
import io.github.opensabe.scheduler.conf.JobStatus;

import io.github.opensabe.scheduler.observation.JobExecuteContext;
import jdk.jfr.Category;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Category({"observation", "task-center", "job-execute"})
@Label("Job Execute")
@StackTrace(false)
public class JobExecuteJFREvent extends BaseEvent {

    private String jobId;
    private String jobName;
    private String cronExpression;
    private JobStatus status;

    public JobExecuteJFREvent(JobExecuteContext context) {
        BeanUtils.copyProperties(context, this);
    }
}
