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
package io.github.opensabe.scheduler.job;

import io.github.opensabe.scheduler.conf.Commander;
import io.github.opensabe.scheduler.conf.SchedulerServerConfiguration;
import io.github.opensabe.scheduler.listener.JobFinishedListener;
import io.github.opensabe.scheduler.listener.JobListeners;
import io.github.opensabe.scheduler.listener.JobStartedListener;
import io.github.opensabe.scheduler.listener.JobSuccessListener;
import io.github.opensabe.scheduler.observation.JobExecuteContext;
import io.github.opensabe.scheduler.observation.JobExecuteObservationConvention;
import io.github.opensabe.scheduler.observation.JobExecuteObservationDocumentation;
import io.github.opensabe.scheduler.server.SchedulerServer;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Log4j2
@NoArgsConstructor
public class JobExecutor implements Runnable {

    private SchedulerJob schedulerJob;
    private JobListeners jobListeners;
    private RedissonClient redissonClient;
    private StringRedisTemplate stringRedisTemplate;
    private Commander commander;
    private SchedulerServer schedulerServer;
    private UnifiedObservationFactory unifiedObservationFactory;
    private DistributionSummary distributionSummary;

    public JobExecutor(SchedulerJob schedulerJob, JobListeners jobListeners, RedissonClient redissonClient, StringRedisTemplate stringRedisTemplate, Commander commander, SchedulerServer schedulerServer, UnifiedObservationFactory unifiedObservationFactory, MeterRegistry meterRegistry) {
        this.schedulerJob = schedulerJob;
        this.jobListeners = jobListeners;
        this.redissonClient = redissonClient;
        this.stringRedisTemplate = stringRedisTemplate;
        this.commander = commander;
        this.schedulerServer = schedulerServer;
        this.unifiedObservationFactory = unifiedObservationFactory;
        this.distributionSummary = DistributionSummary.builder("job.schedule.task." + schedulerJob.getJobName()).distributionStatisticBufferLength(20).distributionStatisticExpiry(Duration.ofDays(30)).publishPercentileHistogram(Boolean.TRUE).publishPercentiles(0.1, 0.5, 0.9).register(meterRegistry);
    }

    @Override
    public void run() {
//        Observation observation = unifiedObservationFactory.createEmptyObservation();
//        //这里新起一个span,否则所有定时任务使用一个traceId不好排查，使用了try-with-resource方式打开一个scope，不需要手动关闭，会自动调用Scope的close()方法停止的
//        try (Observation.Scope ignore = observation.openScope()){
//            if (!commander.isLeader() || !schedulerServer.isRunning()) {
//                log.info("scheduler job server is not a leader or not running, cannot process jobs");
//                return;
//            }
//            log.info("Job {} is ready to go", schedulerJob.getJobName());
//            if (isProcessing()) {
//                return;
//            }
//            jobListeners.getListeners().stream().filter(JobStartedListener.class::isInstance)
//                    .forEach(jobStartedListener -> ((JobStartedListener) jobStartedListener).jobStarted(schedulerJob));
//            long starTime = System.currentTimeMillis();
//            try {
//                schedulerJob.getSimpleJob().execute();
//            } catch (Throwable e) {
//                log.error("caught exception while execute {}, {}", schedulerJob.getJobName(), e.getMessage());
//                throw e;
//            } finally {
//                long endTime = System.currentTimeMillis();
//                long time = endTime - starTime;
//                //次数大于 10，大于最大时间的两倍，并且大于 60s
//                if (distributionSummary.count() > 10 && (time > distributionSummary.max() * 2) && time > 60000) {
//                    log.fatal("{} execute out. time used = {}ms, recent mean elapsed time is {}ms", schedulerJob.getJobName(), time, distributionSummary.mean());
//                } else {
//                    log.info("{} execute out. time used = {}ms", schedulerJob.getJobName(), time);
//                }
//                distributionSummary.record(time);
//            }
//            jobListeners.getListeners().stream().filter(JobSuccessListener.class::isInstance)
//                    .forEach(jobSuccessListener -> ((JobSuccessListener) jobSuccessListener).jobSuccess(schedulerJob));
//        } catch (Throwable ex) {
//            log.fatal("Job {} running exception ", schedulerJob.getJobName(), ex);
//            jobListeners.getListeners().stream().filter(JobFinishedListener.class::isInstance)
//                    .forEach(jobFinishedListener -> ((JobFinishedListener) jobFinishedListener).jobFinished(schedulerJob));
//            observation.error(ex);
//        } finally {
//            observation.stop();
//        }

        JobExecuteContext jobExecuteContext = new JobExecuteContext(this.schedulerJob);
        Observation observation = JobExecuteObservationDocumentation.JOB_EXECUTE.observation(
                null,
                JobExecuteObservationConvention.DEFAULT,
                () -> jobExecuteContext,
                unifiedObservationFactory.getObservationRegistry()
        );

        observation.observe(() -> {
            try {
                if (!commander.isLeader() || !schedulerServer.isRunning()) {
                    log.info("scheduler job server is not a leader or not running, cannot process jobs");
                    return;
                }
                log.info("Job {} is ready to go", schedulerJob.getJobName());
                if (isProcessing()) {
                    log.info("Job {} is processing", schedulerJob.getJobName());
                    return;
                }
                jobListeners.getListeners().stream().filter(JobStartedListener.class::isInstance).forEach(jobStartedListener -> ((JobStartedListener) jobStartedListener).jobStarted(schedulerJob));
                jobExecuteContext.setStatus(schedulerJob.getStatus());
                long starTime = System.currentTimeMillis();
                try {
                    schedulerJob.getSimpleJob().execute();
                } catch (Throwable e) {
                    log.error("caught exception while execute {}, {}", schedulerJob.getJobName(), e.getMessage());
                    throw e;
                } finally {
                    long endTime = System.currentTimeMillis();
                    long time = endTime - starTime;
                    //次数大于 10，大于最大时间的两倍，并且大于 60s
                    if (distributionSummary.count() > 10 && (time > distributionSummary.max() * 2) && time > 60000) {
                        log.fatal("{} execute out. time used = {}ms, recent mean elapsed time is {}ms", schedulerJob.getJobName(), time, distributionSummary.mean());
                    } else {
                        log.info("{} execute out. time used = {}ms", schedulerJob.getJobName(), time);
                    }
                    distributionSummary.record(time);
                }
                jobListeners.getListeners().stream().filter(JobSuccessListener.class::isInstance).forEach(jobSuccessListener -> ((JobSuccessListener) jobSuccessListener).jobSuccess(schedulerJob));
                jobExecuteContext.setStatus(schedulerJob.getStatus());
            } catch (Throwable ex) {
                log.fatal("Job {} running exception ", schedulerJob.getJobName(), ex);
                jobListeners.getListeners().stream().filter(JobFinishedListener.class::isInstance).forEach(jobFinishedListener -> ((JobFinishedListener) jobFinishedListener).jobFinished(schedulerJob));
                jobExecuteContext.setStatus(schedulerJob.getStatus());
                throw ex;
            }
        });


    }

    private boolean isProcessing() {
        RLock lock = redissonClient.getLock(SchedulerServerConfiguration.REDIS_JOB_STATUS_KEY + ":" + schedulerJob.getJobId());
        boolean isLocked = false;

        try {
            log.info("Job {} check status", schedulerJob.getJobName());
            isLocked = lock.tryLock(1L, 5L, TimeUnit.SECONDS);
            if (!isLocked) {
                log.info("Job {} cannot get lock", schedulerJob.getJobName());
                return true;
            }
            return stringRedisTemplate.opsForHash().hasKey(SchedulerServerConfiguration.REDIS_JOB_MONITOR_KEY, schedulerJob.getJobId()) || stringRedisTemplate.opsForHash().hasKey(SchedulerServerConfiguration.REDIS_JOB_MISFIRE_KEY, schedulerJob.getJobId()) || Objects.requireNonNull(stringRedisTemplate.opsForList().range(SchedulerServerConfiguration.REDIS_JOB_MISFIRE_QUEUE_KEY, 0, -1)).contains(schedulerJob.getJobId());
        } catch (InterruptedException e) {
            log.error("Job executor check job {} process status exception ", schedulerJob.getJobName(), e);
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
        return true;
    }
}
