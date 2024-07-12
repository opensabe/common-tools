package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.ratelimiter.RRateLimiterSetRateContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"observation", "redisson", "rRateLimiter"})
@Label("set rate")
@StackTrace(false)
public class RRateLimiterSetRateJFREvent extends Event {
    @Label("rate limiter name")
    private final String rateLimiterName;
    @Label("thread name")
    private final String mode;
    @Label("rate")
    private final long rate;
    @Label("rate interval")
    private final long rateInterval;
    @Label("rate interval unit")
    private final String rateIntervalUnit;
    @Label("is rate set successfully")
    private boolean rateSetSuccessfully;
    private String traceId;
    private String spanId;

    public RRateLimiterSetRateJFREvent(RRateLimiterSetRateContext rRateLimiterSetRateContext) {
        this.rateLimiterName = rRateLimiterSetRateContext.getRateLimiterName();
        this.mode = rRateLimiterSetRateContext.getMode() != null ? rRateLimiterSetRateContext.getMode().name() : null;
        this.rate = rRateLimiterSetRateContext.getRate();
        this.rateInterval = rRateLimiterSetRateContext.getRateInterval();
        this.rateIntervalUnit = rRateLimiterSetRateContext.getRateIntervalUnit() != null ? rRateLimiterSetRateContext.getRateIntervalUnit().name() : null;
    }
}
