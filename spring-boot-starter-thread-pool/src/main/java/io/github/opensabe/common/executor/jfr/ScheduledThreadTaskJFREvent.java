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

import java.util.concurrent.TimeUnit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import jdk.jfr.Timespan;
import jdk.jfr.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * 调度线程池任务执行 JFR 事件，记录 traceId 与调度/运行耗时。
 */
@Category({"Executor Service"})
@Label("Scheduled Thread Task JFR")
@Description("it manages to record the traceid and the start time of the task and the time-consuming of the end")
@StackTrace(false)
@SuppressFBWarnings("URF_UNREAD_FIELD")
public class ScheduledThreadTaskJFREvent extends Event {

    /** 链路 traceId。 */
    @Getter
    private final String traceId;

    /** 链路 spanId。 */
    @Getter
    private final String spanId;

    /** 初始延迟。 */
    private final long initialDelay;

    /** 固定周期（固定速率调度）。 */
    private final long period;

    /** 单次延迟（一次性或 fixedDelay）。 */
    private final long delay;

    /** 时间单位。 */
    private final TimeUnit unit;

    /** 任务开始运行的时间戳（毫秒）。 */
    @Setter
    @Getter
    @Label("taskRunStartTime")
    @Timestamp(value = Timestamp.MILLISECONDS_SINCE_EPOCH)
    @Description("the time when the task starts to run")
    private long taskRunStartTime;

    /** 任务运行结束的时间戳（毫秒）。 */
    @Setter
    @Getter
    @Label("taskRunEndTime")
    @Timestamp(value = Timestamp.MILLISECONDS_SINCE_EPOCH)
    @Description("the time when the task has finished")
    private long taskRunEndTime;

    /** 实际运行耗时（毫秒）。 */
    @Setter
    @Timespan(value = Timespan.MILLISECONDS)
    @Description("the time-consuming of the span of the task with the lifecycle of queuing")
    private long taskRunTimeDuration;

    /**
     * @param traceId      traceId
     * @param spanId       spanId
     * @param initialDelay 初始延迟
     * @param period       周期
     * @param delay        延迟
     * @param unit         时间单位
     */
    public ScheduledThreadTaskJFREvent(String traceId, String spanId, long initialDelay, long period, long delay, TimeUnit unit) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.initialDelay = initialDelay;
        this.period = period;
        this.delay = delay;
        this.unit = unit;
    }
}
