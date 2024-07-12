package io.github.opensabe.common.idgenerator.service;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.idgenerator.exception.IdGenerateException;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class UniqueIDWithoutBizTypeImpl implements UniqueIDWithouBizType {

    public static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyMMddHHmmss");
    public static DateTimeFormatter formatForShortId = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");

    private static final Long MAX_SEQUENCE_NUM = 10000000L;

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

    public UniqueIDWithoutBizTypeImpl(StringRedisTemplate redisTemplate, RedissonClient redissonClient, ThreadPoolFactory threadPoolFactory) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        this.threadPoolExecutor = threadPoolFactory.createNormalThreadPool("uniqueIdWithoutBizType-", 20);
    }

    private Long getSequenceNumber(String bizType) {
        String sequenceKey = SEQUENCE_NUM_KEY + ":" + bizType;
        String sequenceLock = SEQUENCE_NUM_LOCK + ":" + bizType;
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        RLock lock = redissonClient.getLock(sequenceLock);
        Long sequenceNumber = valueOperations.increment(sequenceKey + ":" + bizType, ThreadLocalRandom.current().nextLong(1, 10));
        if (Objects.nonNull(sequenceNumber) && sequenceNumber < MAX_SEQUENCE_NUM) {
            return sequenceNumber;
        }
        if (lock.tryLock()) {
            sequenceNumber = valueOperations.increment(sequenceKey + ":" + bizType, ThreadLocalRandom.current().nextLong(1, 10));
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
            sequenceNumber = Long.parseLong(Objects.requireNonNull(valueOperations.get(sequenceKey)));
            if (sequenceNumber < MAX_SEQUENCE_NUM) {
                return valueOperations.increment(sequenceKey, 1);
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
    @Transactional(propagation = Propagation.NEVER)
    public Long getShortUniqueId(String bizType) {
        if (StringUtils.isBlank(bizType)) {
            throw new IdGenerateException("biz type is empty");
        }
        if (bizType.length() > 10) {
            throw new IdGenerateException("biz type length > 10");
        }
        String timestamp = formatForShortId.format(LocalDateTime.now());
        return Long.valueOf(timestamp + String.format("%04d", getShortSequenceNumber(bizType, timestamp)));
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public Long getUniqueId(String bizType) {
        if (StringUtils.isBlank(bizType)) {
            throw new IdGenerateException("biz type is empty");
        }
        if (bizType.length() > 4) {
            throw new IdGenerateException("biz type lenth > 4");
        }
        String timestamp = format.format(LocalDateTime.now());
        return Long.valueOf(timestamp + String.format("%07d", getSequenceNumber(bizType)));
    }

    @Override
    public Long getUniqueIdWithTimeOut(String bizType, long time, TimeUnit timeUnit) throws Exception {
        Future<Long> submit = threadPoolExecutor.submit(() -> this.getUniqueId(bizType));
        return submit.get(time, timeUnit);
    }
}
