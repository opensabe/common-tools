package io.github.opensabe.common.redisson.observation.rbucket;

import io.micrometer.observation.Observation;
import jakarta.annotation.Nullable;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

/**
 * @author heng.ma
 */
@Getter
public class RBucketContext extends Observation.Context {

    private final String threadName;

    private final String method;

    private final String bucketName;

    private final Duration ttl;

    private final Instant time;

    private RBucketContext(String bucketName, String method, @Nullable Duration ttl, @Nullable Instant time) {
        this.method = method;
        this.bucketName = bucketName;
        this.ttl = ttl;
        this.time = time;
        this.threadName = Thread.currentThread().getName();
    }
    public RBucketContext(String bucketName, String method) {
        this (bucketName, method, null, null);
    }
    public RBucketContext(String bucketName, String method, Duration ttl) {
        this (bucketName, method, ttl, null);
    }
    public RBucketContext(String bucketName, String method, Instant time) {
        this (bucketName, method, null, time);
    }
}
