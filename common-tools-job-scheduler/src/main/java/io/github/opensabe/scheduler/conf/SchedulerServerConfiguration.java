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
package io.github.opensabe.scheduler.conf;

public class SchedulerServerConfiguration {

    public static final long DEFAULT_MONITOR_SERVER_INTERVAL_IN_SECONDS = 5L;
    public static final long DEFAULT_MISFIRE_JOB_SERVER_INTERVAL_IN_SECONDS = 10L;
    public static final String REDIS_JOB_MONITOR_KEY = "Scheduler_Job:monitor";
    public static final String REDIS_JOB_STATUS_KEY = "Scheduler_Job:status";
    public static final String REDIS_JOB_MISFIRE_KEY = "Scheduler_Job:misfire";
    public static final String REDIS_JOB_MISFIRE_QUEUE_KEY = "Scheduler_Job:misfire_queue";

}
