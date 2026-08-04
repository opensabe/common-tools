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
package io.github.opensabe.common.executor.jfr;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import jdk.jfr.Threshold;
import jdk.jfr.Timespan;
import lombok.Getter;
import lombok.Setter;

/**
 * 线程池任务执行 JFR 事件，记录 traceId、排队与运行耗时（默认阈值 100ms）。
 */
@SuppressWarnings("unused")
@Category({"Executor Service"})
@Label("Thread Task JFR (default Threshold 100ms)")
@Description("it manages to record the traceid and the start time of the task and the time-consuming of the end")
@StackTrace(false)
@Threshold(value = "100ms")
@SuppressFBWarnings("URF_UNREAD_FIELD")
public class ThreadTaskJFREvent extends Event {

    /** 链路 traceId。 */
    @Getter
    private final String traceId;

    /** 链路 spanId。 */
    @Getter
    private final String spanId;

    /** 任务提交到线程池的时间戳（毫秒）。 */
    @Getter
    @Label("taskSubmitStartTime")
    @Description("the time when the task starts to be put in the thread pool")
    private final long submitTaskStartTime;

    /** 任务开始运行的时间戳（毫秒）。 */
    @Setter
    @Getter
    @Label("taskRunStartTime")
    @Description("the time when the task starts to run")
    private long taskRunStartTime;

    /** 任务运行结束的时间戳（毫秒）。 */
    @Setter
    @Getter
    @Label("taskRunEndTime")
    @Description("the time when the task has finished")
    private long taskRunEndTime;

    /** 排队等待耗时（毫秒）。 */
    @Setter
    @Timespan(value = Timespan.MILLISECONDS)
    @Description("the time-consuming of the span of the task with the lifecycle of the running state without queuing")
    private long taskQueueTimeDuration;

    /** 实际运行耗时（毫秒）。 */
    @Setter
    @Timespan(value = Timespan.MILLISECONDS)
    @Description("the time-consuming of the span of the task with the lifecycle of queuing")
    private long taskRunTimeDuration;

    /**
     * @param submitStartTime 提交时间戳
     * @param traceId         traceId
     * @param spanId          spanId
     */
    public ThreadTaskJFREvent(long submitStartTime, String traceId, String spanId) {
        this.submitTaskStartTime = submitStartTime;
        this.traceId = traceId;
        this.spanId = spanId;
    }

}
