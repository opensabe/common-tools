package io.github.opensabe.common.redisson.annotation.bucket;


import java.time.Duration;
import java.time.Instant;

public enum CacheOption {

    /**
     * @see org.redisson.api.RBucket#set(Object, Duration)
     */
    SET,

    /**
     * @see org.redisson.api.RBucket#getAndSet(Object, Duration)
     */
    GET_AND_SET,

    /**
     * @see org.redisson.api.RBucket#getAndDelete()
     */
    GET_AND_DELETE,

    /**
     * @see org.redisson.api.RBucket#getAndExpire(Duration)
     * @see org.redisson.api.RBucket#getAndExpire(Instant) 
     */
    GET_AND_EXPIRE,

    /**
     * @see org.redisson.api.RBucket#expire(Duration)
     * @see org.redisson.api.RBucket#expire(Instant)
     */
    EXPIRE,
    /**
     * @see org.redisson.api.RBucket#getAndClearExpire()
     */
    GET_AND_CLEAR_EXPIRE,

    /**
     * @see org.redisson.api.RBucket#setIfAbsent(Object, Duration)
     */
    SET_IF_ABSENT,

    /**
     * @see org.redisson.api.RBucket#setIfExists(Object, Duration)
     */
    SET_IF_EXISTS,

    /**
     * @see org.redisson.api.RBucket#setAndKeepTTL(Object)
     */
    KEEP_TTL,

    /**
     * @see org.redisson.api.RBucket#delete()
     */
    DELETE,

}
