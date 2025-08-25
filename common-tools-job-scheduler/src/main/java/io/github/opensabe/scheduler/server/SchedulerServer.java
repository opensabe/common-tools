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
package io.github.opensabe.scheduler.server;

import io.github.opensabe.scheduler.conf.Commander;
import io.github.opensabe.scheduler.conf.SchedulerProperties;
import io.github.opensabe.scheduler.conf.SimpleTask;
import io.github.opensabe.scheduler.job.JobExecutor;
import io.github.opensabe.scheduler.job.SchedulerJob;
import io.github.opensabe.scheduler.job.SimpleJob;
import io.github.opensabe.scheduler.listener.DefaultJobListener;
import io.github.opensabe.scheduler.listener.JobListener;
import io.github.opensabe.scheduler.listener.JobListeners;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
public class SchedulerServer {

    //private MonitorServer monitorServer;
    //private MisfireJobServer misfireJobServer;

    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    private final MeterRegistry meterRegistry;
    private final UnifiedObservationFactory unifiedObservationFactory;
    private final UUID schedulerServerId;
    private final JobListeners jobListeners;
    //private final SchedulerProperties schedulerProperties;
    private final Commander commander;

//    private volatile Instant firstHeartbeat;
//    private volatile ScheduledExecutorService monitorServerExecutorService;
//    private volatile ScheduledExecutorService misfireJobServerExecutorService;
    private volatile Map<String, SchedulerJob> jobs;
    private volatile Map<String, ThreadPoolTaskScheduler> processingJobs;
    private volatile boolean running;


    public SchedulerServer(ApplicationContext applicationContext, Environment environment,
                           List<JobListener> listeners, RedissonClient redissonClient, StringRedisTemplate stringRedisTemplate,
                           SchedulerProperties schedulerProperties, MeterRegistry meterRegistry, Commander commander) {
        this.meterRegistry = meterRegistry;
        this.schedulerServerId = UUID.randomUUID();
        this.commander = commander;
        this.unifiedObservationFactory = applicationContext.getBean(UnifiedObservationFactory.class);
        //this.schedulerProperties = schedulerProperties;
        this.applicationContext = applicationContext;
        this.environment = environment;
        this.redissonClient = redissonClient;
        this.stringRedisTemplate = stringRedisTemplate;
        listeners.add(new DefaultJobListener(redissonClient, stringRedisTemplate, schedulerProperties.getExpiredTime()));
        this.jobListeners = new JobListeners(listeners);
    }

    public UUID getSchedulerServerId() {
        return schedulerServerId;
    }


    public void start() {
        if (this.isRunning()) {
            return;
        }

        log.info("Scheduler server {} is starting...", this.getSchedulerServerId());
        //firstHeartbeat = Instant.now();
        serverIsRunning();
        this.jobs = loadAllJobs();
        processSchedulerJob();
    }

    public void stop() {
        log.info("Scheduler server {} is shutting down...", this.getSchedulerServerId());
        this.running = false;
        this.processingJobs.values().forEach(threadPoolTaskScheduler -> {
            try {
                threadPoolTaskScheduler.shutdown();
            } catch (Throwable ex) {
                log.error("Scheduler job thread shutdown exception", ex);
            }
        });
        //stopMonitorServer();
        //stopMisfireServer();
    }

    public boolean isRunning() {
        return this.running;
    }

    public Map<String, SchedulerJob> getJobs() {
        return jobs;
    }

    private void serverIsRunning() {
        this.running = true;
    }

//    private void createMisfireJobServer() {
//        this.misfireJobServer = new MisfireJobServer(redissonClient, stringRedisTemplate, jobListeners, schedulerProperties.getExpiredTime());
//    }
//
//    private void startMisfireJobServer() {
//        misfireJobServerExecutorService = Executors.newSingleThreadScheduledExecutor();
//        misfireJobServerExecutorService.scheduleAtFixedRate(this.misfireJobServer, 0l, SchedulerServerConfiguration.DEFAULT_MISFIRE_JOB_SERVER_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
//    }
//
//    private void createMonitorServer() {
//        this.monitorServer = new MonitorServer(redissonClient, stringRedisTemplate, this.jobs);
//    }
//
//    private void startMonitorServer() {
//        monitorServerExecutorService = Executors.newSingleThreadScheduledExecutor();
//        monitorServerExecutorService.scheduleAtFixedRate(this.monitorServer, 0l, SchedulerServerConfiguration.DEFAULT_MONITOR_SERVER_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
//    }

//    private void stopMonitorServer() {
//        if (monitorServerExecutorService == null) {
//            return;
//        }
//        monitorServerExecutorService.shutdown();
//        try {
//            if (!monitorServerExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
//                log.info("Scheduler Monitor Server shutdown requested - waiting for jobs to finish (at most 10 seconds)");
//                monitorServerExecutorService.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            monitorServerExecutorService.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
//    }

//    private void stopMisfireServer() {
//        if (misfireJobServerExecutorService == null) {
//            return;
//        }
//        misfireJobServerExecutorService.shutdown();
//        try {
//            if (!misfireJobServerExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
//                log.info("Scheduler Misfire Server shutdown requested - waiting for jobs to finish (at most 10 seconds)");
//                misfireJobServerExecutorService.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            misfireJobServerExecutorService.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
//    }

    private void processSchedulerJob() {
        this.processingJobs = this.jobs.values().stream().collect(Collectors.toMap(SchedulerJob::getJobId, schedulerJob -> {
            CronTrigger cronTrigger = new CronTrigger(schedulerJob.getCronExpression());
            ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
            threadPoolTaskScheduler.setThreadNamePrefix(schedulerJob.getJobName());
            threadPoolTaskScheduler.setAwaitTerminationSeconds(15);
            threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
            threadPoolTaskScheduler.initialize();
            threadPoolTaskScheduler.schedule(new JobExecutor(schedulerJob, jobListeners, redissonClient, stringRedisTemplate, commander, this, unifiedObservationFactory, meterRegistry), cronTrigger);
            return threadPoolTaskScheduler;
        }));
    }

    private Map<String, SchedulerJob> loadAllJobs() {
        return applicationContext.getBeansWithAnnotation(SimpleTask.class).values().stream().map(bean -> {
            SimpleTask annotation = AnnotatedElementUtils.getMergedAnnotation(ClassUtils.getUserClass(bean), SimpleTask.class);
            String cron = environment.resolvePlaceholders(Objects.requireNonNull(annotation).cron());
            String jobName = annotation.jobName();
            if (StringUtils.isBlank(jobName)) {
                jobName = bean.getClass().getName();
            }
            log.info("SchedulerServer-loadAllJobs: {} -> {}", jobName, cron);
            return SchedulerJob.builder().jobId(UUID.randomUUID().toString()).jobName(jobName).simpleJob((SimpleJob) bean).cronExpression(cron).misfire(annotation.misfire()).build();
        }).collect(Collectors.toMap(SchedulerJob::getJobId, job -> job));
    }

}
