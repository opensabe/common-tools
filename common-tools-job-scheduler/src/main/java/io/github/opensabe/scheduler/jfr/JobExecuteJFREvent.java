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
