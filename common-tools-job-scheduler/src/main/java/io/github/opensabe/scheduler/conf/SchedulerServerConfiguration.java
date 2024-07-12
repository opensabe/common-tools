package io.github.opensabe.scheduler.conf;

public class SchedulerServerConfiguration {

    public static final long DEFAULT_MONITOR_SERVER_INTERVAL_IN_SECONDS = 5L;
    public static final long DEFAULT_MISFIRE_JOB_SERVER_INTERVAL_IN_SECONDS = 10L;
    public static final String REDIS_JOB_MONITOR_KEY = "Scheduler_Job:monitor";
    public static final String REDIS_JOB_STATUS_KEY = "Scheduler_Job:status";
    public static final String REDIS_JOB_MISFIRE_KEY = "Scheduler_Job:misfire";
    public static final String REDIS_JOB_MISFIRE_QUEUE_KEY = "Scheduler_Job:misfire_queue";

}
