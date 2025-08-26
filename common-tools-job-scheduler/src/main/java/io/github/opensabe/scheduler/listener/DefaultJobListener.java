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
package io.github.opensabe.scheduler.listener;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.github.opensabe.scheduler.conf.JobStatus;
import io.github.opensabe.scheduler.conf.SchedulerServerConfiguration;
import io.github.opensabe.scheduler.job.SchedulerJob;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DefaultJobListener implements JobStartedListener, JobSuccessListener, JobFinishedListener, JobRetryListener {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final long expiredTime;

    public DefaultJobListener(RedissonClient redissonClient, StringRedisTemplate stringRedisTemplate, long expiredTime) {
        this.redissonClient = redissonClient;
        this.stringRedisTemplate = stringRedisTemplate;
        this.expiredTime = expiredTime;
    }

    @Override
    public void jobStarted(SchedulerJob job) {
        log.info("Job {} started...", job.getJobName());
        RLock lock = redissonClient.getLock(SchedulerServerConfiguration.REDIS_JOB_STATUS_KEY + ":" + job.getJobId());
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(0L, 5L, TimeUnit.SECONDS);
            if (!isLocked) {
                return;
            }

            try {
                stringRedisTemplate.opsForHash().put(SchedulerServerConfiguration.REDIS_JOB_MONITOR_KEY, job.getJobId(), String.valueOf(System.currentTimeMillis() + expiredTime));
            } catch (Throwable ex) {
                log.error("Update job {} to running status exception ", job.getJobName(), ex);
                return;
            }

            job.setStatus(JobStatus.STARTED);
        } catch (Throwable e) {
            log.error("Job {} start listener exception ", job.getJobName(), e);
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
    }

    @Override
    public void jobSuccess(SchedulerJob job) {
        log.info("Job {} process successfully", job.getJobName());
        RLock lock = redissonClient.getLock(SchedulerServerConfiguration.REDIS_JOB_STATUS_KEY + ":s" + job.getJobId());
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(0L, 5L, TimeUnit.SECONDS);
            if (!isLocked) {
                return;
            }

            try {
                stringRedisTemplate.opsForHash().delete(SchedulerServerConfiguration.REDIS_JOB_MONITOR_KEY, job.getJobId());
            } catch (Throwable ex) {
                log.error("Update job {} to success status exception ", job.getJobName(), ex);
                return;
            }

            job.setStatus(JobStatus.SUCCESS);
        } catch (Throwable e) {
            log.error("Job {} success listener exception ", job.getJobName(), e);
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
    }

    @Override
    public void jobFinished(SchedulerJob job) {
        log.info("Job {} processing finish", job.getJobName());
        RLock lock = redissonClient.getLock(SchedulerServerConfiguration.REDIS_JOB_STATUS_KEY + ":f" + job.getJobId());
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(0L, 5L, TimeUnit.SECONDS);
            if (!isLocked) {
                return;
            }

            try {
                stringRedisTemplate.opsForHash().delete(SchedulerServerConfiguration.REDIS_JOB_MONITOR_KEY, job.getJobId());
            } catch (Throwable ex) {
                log.error("Update job {} to finish status exception ", job.getJobName(), ex);
                return;
            }

            job.setStatus(JobStatus.FINISHED);
        } catch (Throwable e) {
            log.error("Job {} finish listener exception ", job.getJobName(), e);
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
    }
}
