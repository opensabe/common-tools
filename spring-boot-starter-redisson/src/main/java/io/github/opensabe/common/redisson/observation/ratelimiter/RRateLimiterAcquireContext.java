package io.github.opensabe.common.redisson.observation.ratelimiter;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class RRateLimiterAcquireContext extends Observation.Context {
    private final String rateLimiterName;
    private final String threadName;
    private final long permits;
    private final long timeout;
    private final TimeUnit timeUnit;

    private boolean rateLimiterAcquiredSuccessfully;

    public RRateLimiterAcquireContext(String rateLimiterName, String threadName, long permits, long timeout, TimeUnit timeUnit) {
        this.rateLimiterName = rateLimiterName;
        this.threadName = threadName;
        this.permits = permits;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }
}
