package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.ratelimiter.RRateLimiterAcquireContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"observation", "redisson", "rRateLimiter"})
@Label("acquire")
@StackTrace(false)
public class RRateLimiterAcquireJFREvent extends Event {
    @Label("rate limiter name")
    private final String rateLimiterName;
    @Label("permits")
    private final long permits;
    @Label("timeout")
    private final long timeout;
    @Label("time unit")
    private final String timeUnit;
    @Label("is acquire successfully")
    private boolean acquireSuccessfully;
    private String traceId;
    private String spanId;

    public RRateLimiterAcquireJFREvent(RRateLimiterAcquireContext rRateLimiterAcquireContext) {
        this.rateLimiterName = rRateLimiterAcquireContext.getRateLimiterName();
        this.permits = rRateLimiterAcquireContext.getPermits();
        this.timeout = rRateLimiterAcquireContext.getTimeout();
        this.timeUnit = rRateLimiterAcquireContext.getTimeUnit() != null ? rRateLimiterAcquireContext.getTimeUnit().name() : null;
    }
}
