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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.github.opensabe.scheduler.conf.JobStatus;
import io.github.opensabe.scheduler.conf.SchedulerServerConfiguration;
import io.github.opensabe.scheduler.listener.JobListeners;
import io.github.opensabe.scheduler.listener.JobStartedListener;
import io.github.opensabe.scheduler.listener.JobSuccessListener;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
@NoArgsConstructor
public class MisfireJobRunner extends Thread {

    private SchedulerJob schedulerJob;
    private JobListeners jobListeners;
    private RedissonClient redissonClient;
    private StringRedisTemplate stringRedisTemplate;
    private long expiredTime;

    @Override
    public void run() {
        boolean isJobDone = false;
        while (!isJobDone) {
            if (schedulerJob.getStatus() == JobStatus.FINISHED || schedulerJob.getStatus() == JobStatus.SUCCESS) {
                RLock lock = redissonClient.getLock(SchedulerServerConfiguration.REDIS_JOB_MISFIRE_KEY + ":" + schedulerJob.getJobId());
                boolean isLocked = false;
                try {
                    isLocked = lock.tryLock(0L, TimeUnit.SECONDS);
                    if (isLocked) {
                        log.info("Misfire job {} get lock", schedulerJob.getJobName());
                        if (!isProcessing()) {
                            try {

                                int idx = 0;
                                String jobId = null;
                                List<String> queue = Objects.requireNonNull(stringRedisTemplate.opsForList().range(SchedulerServerConfiguration.REDIS_JOB_MISFIRE_QUEUE_KEY, 0, -1));
                                for (; idx < queue.size(); idx++) {
                                    jobId = queue.get(idx);
                                    if (schedulerJob.getJobId().equals(jobId)) {
                                        break;
                                    }
                                }
                                if (!schedulerJob.getJobId().equals(jobId)) {
                                    log.error("Misfire job {} is missing", schedulerJob.getJobName());
                                    return;
                                }
                                stringRedisTemplate.opsForList().remove(SchedulerServerConfiguration.REDIS_JOB_MISFIRE_QUEUE_KEY, idx, jobId);

                                try {
                                    stringRedisTemplate.opsForHash().put(SchedulerServerConfiguration.REDIS_JOB_MISFIRE_KEY, schedulerJob.getJobId(), String.valueOf(System.currentTimeMillis() + expiredTime));
                                } catch (Throwable ex) {
                                    log.error("Misfire job {} update status exception ", schedulerJob.getJobName(), ex);
                                    continue;
                                }
                                jobListeners.getListeners().stream().filter(JobStartedListener.class::isInstance)
                                        .forEach(jobStartedListener -> ((JobStartedListener) jobStartedListener).jobStarted(schedulerJob));
                                schedulerJob.setStatus(JobStatus.PROCESSING);
                                log.info("Misfire job {} is processing", schedulerJob.getJobName());
                                schedulerJob.getSimpleJob().execute();
                                jobListeners.getListeners().stream().filter(JobSuccessListener.class::isInstance)
                                        .forEach(jobSuccessListener -> ((JobSuccessListener) jobSuccessListener).jobSuccess(schedulerJob));
                                try {
                                    stringRedisTemplate.opsForHash().delete(SchedulerServerConfiguration.REDIS_JOB_MISFIRE_KEY, schedulerJob.getJobId(), String.valueOf(System.currentTimeMillis() + expiredTime));
                                } catch (Throwable ex) {
                                    log.error("Misfire job {} delete status exception ", schedulerJob.getJobName(), ex);
                                    stringRedisTemplate.opsForHash().delete(SchedulerServerConfiguration.REDIS_JOB_MISFIRE_KEY, schedulerJob.getJobId(), String.valueOf(System.currentTimeMillis() + expiredTime));
                                }
                            } catch (Throwable ex) {
                                log.error("Misfire job {} processing exception ", schedulerJob.getJobName(), ex);
                            } finally {
                                isJobDone = true;
                            }
                        }
                    }
                } catch (Throwable e) {
                    log.error("Misfire job {} processing exception ", schedulerJob.getJobName(), e);
                } finally {
                    if (isLocked) {
                        lock.unlock();
                    }
                }
            }

            try {
                TimeUnit.SECONDS.sleep(10L);
            } catch (InterruptedException e) {
                log.error("Misfire job {} processing exception ", schedulerJob.getJobId(), e);
            }
            log.info("Misfire job {} is done [{}]", schedulerJob.getJobName(), isJobDone);
        }
    }

    private boolean isProcessing() {
        RLock lock = redissonClient.getLock(SchedulerServerConfiguration.REDIS_JOB_STATUS_KEY + ":" + schedulerJob.getJobId());
        boolean isLocked = false;
        try {
            log.info("Misfire job {} check status", schedulerJob.getJobName());
            isLocked = lock.tryLock(1L, 5L, TimeUnit.SECONDS);
            if (!isLocked) {
                log.info("Misfire job {} cannot get lock", schedulerJob.getJobName());
                return true;
            }
            return stringRedisTemplate.opsForHash().hasKey(SchedulerServerConfiguration.REDIS_JOB_MONITOR_KEY, schedulerJob.getJobId())
                    || stringRedisTemplate.opsForHash().hasKey(SchedulerServerConfiguration.REDIS_JOB_MISFIRE_KEY, schedulerJob.getJobId());
        } catch (InterruptedException e) {
            log.error("Misfire job executor process job {} exception ", schedulerJob.getJobName(), e);
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
        return true;
    }
}
