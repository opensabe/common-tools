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
package io.github.opensabe.common.idgenerator.service;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.idgenerator.exception.IdGenerateException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Log4j2
public class UniqueIDImpl implements UniqueID {

    public static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyMMddHHmmss");
    public static DateTimeFormatter formatForShortId = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");

    private static final Long MAX_SEQUENCE_NUM = 100000000L;

    private static final Long MAX_SHORT_SEQUENCE_NUM = 10000L;
    /**
     * key for sequence
     */
    private static final String SEQUENCE_NUM_KEY = "sequence_num_key";
    private static final String SHORT_SEQUENCE_NUM_KEY_PREFIX = "sequence_short_num_key:";
    /**
     * lock name
     */
    private static final String SEQUENCE_NUM_LOCK = "sequnce_num_lock";
    private static final String SHORT_SEQUENCE_NUM_LOCK_PREFIX = "sequnce_short_num_lock:";

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final ExecutorService threadPoolExecutor;

    public UniqueIDImpl(StringRedisTemplate redisTemplate, RedissonClient redissonClient, ThreadPoolFactory threadPoolFactory) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        this.threadPoolExecutor = threadPoolFactory.createNormalThreadPool("uniqueId-", 20);
    }

    private Long getSequenceNumber() {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        RLock lock = redissonClient.getLock(SEQUENCE_NUM_LOCK);
        Long sequenceNumber = valueOperations.increment(SEQUENCE_NUM_KEY, ThreadLocalRandom.current().nextLong(1, 10));
        if (Objects.nonNull(sequenceNumber) && sequenceNumber < MAX_SEQUENCE_NUM) {
            return sequenceNumber;
        }
        if (lock.tryLock()) {
            sequenceNumber = valueOperations.increment(SEQUENCE_NUM_KEY, ThreadLocalRandom.current().nextLong(1, 10));
            try {
                if (Objects.nonNull(sequenceNumber) && sequenceNumber < MAX_SEQUENCE_NUM) {
                    return sequenceNumber;
                }
                valueOperations.set(SEQUENCE_NUM_KEY, "0");
                sequenceNumber = 0L;
            } finally {
                lock.unlock();
            }
            return sequenceNumber;
        }
        while (true) {
            sequenceNumber = Long.parseLong(Objects.requireNonNull(valueOperations.get(SEQUENCE_NUM_KEY)));
            if (sequenceNumber < MAX_SEQUENCE_NUM) {
                return valueOperations.increment(SEQUENCE_NUM_KEY, 1);
            }
        }
    }

    private Long getShortSequenceNumber(String bizType, String timestamp) {
        String sequenceNumberKey = SHORT_SEQUENCE_NUM_KEY_PREFIX + ":" + bizType + ":" + timestamp;
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        RLock lock = redissonClient.getLock(SHORT_SEQUENCE_NUM_LOCK_PREFIX + ":" + bizType + ":" + timestamp);
        Long sequenceNumber = valueOperations.increment(sequenceNumberKey, 1);
        if (Objects.nonNull(sequenceNumber) && sequenceNumber < MAX_SHORT_SEQUENCE_NUM) {
            return sequenceNumber;
        }
        if (lock.tryLock()) {
            sequenceNumber = valueOperations.increment(sequenceNumberKey, 1);
            try {
                if (Objects.nonNull(sequenceNumber) && sequenceNumber < MAX_SHORT_SEQUENCE_NUM) {
                    return sequenceNumber;
                }
                valueOperations.set(sequenceNumberKey, "0");
                sequenceNumber = 0L;
            } finally {
                lock.unlock();
            }
            return sequenceNumber;
        }
        while (true) {
            sequenceNumber = Long.parseLong(Objects.requireNonNull(valueOperations.get(sequenceNumberKey)));
            if (sequenceNumber < MAX_SHORT_SEQUENCE_NUM - 1) {
                return valueOperations.increment(sequenceNumberKey, 1);
            }
        }
    }

    @Override
    public String getShortUniqueId(String bizType) {
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        if (actualTransactionActive) {
            log.error("Do not use getUniqueId in transaction, because it may block the transaction because of slow redis response");
        }
        if (StringUtils.isBlank(bizType)) {
            throw new IdGenerateException("biz type is empty");
        }
        if (bizType.length() > 10) {
            throw new IdGenerateException("biz type length > 10");
        }
        String timestamp = formatForShortId.format(LocalDateTime.now());
        return timestamp + String.format("%03d", getShortSequenceNumber(bizType, timestamp));
    }

    @Override
    public String getUniqueId(String bizType) {
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        if (actualTransactionActive) {
            log.error("Do not use getUniqueId in transaction, because it may block the transaction because of slow redis response");
        }
        if (StringUtils.isBlank(bizType)) {
            throw new IdGenerateException("biz type is empty");
        }
        if (bizType.length() > 4) {
            throw new IdGenerateException("biz type lenth > 4");
        }
        String timestamp = format.format(LocalDateTime.now());
        return timestamp + bizType + String.format("%08d", getSequenceNumber());
    }

    @Override
    public String getLongUniqueId(String bizType) {
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        if (actualTransactionActive) {
            log.error("Do not use getUniqueId in transaction, because it may block the transaction because of slow redis response");
        }
        if (StringUtils.isBlank(bizType)) {
            throw new IdGenerateException("biz type is empty");
        }
        if (bizType.length() > 4) {
            throw new IdGenerateException("biz type lenth > 4");
        }
        String timestamp = formatForShortId.format(LocalDateTime.now());
        return timestamp + bizType + String.format("%08d", getSequenceNumber());
    }

    @Override
    public String getUniqueIdWithTimeOut(String bizType, long time, TimeUnit timeUnit) throws Exception {
        Future<String> submit = threadPoolExecutor.submit(() -> this.getUniqueId(bizType));
        return submit.get(time, timeUnit);
    }
}
