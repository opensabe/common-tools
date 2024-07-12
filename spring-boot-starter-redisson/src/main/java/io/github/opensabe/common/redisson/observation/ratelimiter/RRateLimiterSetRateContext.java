package io.github.opensabe.common.redisson.observation.ratelimiter;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;

@Getter
@Setter
public class RRateLimiterSetRateContext extends Observation.Context {
    private final String rateLimiterName;
    private final String threadName;
    private final RateType mode;
    private final long rate;
    private final long rateInterval;
    private final RateIntervalUnit rateIntervalUnit;
    private boolean setRateSuccessfully;

    public RRateLimiterSetRateContext(String rateLimiterName, String threadName, RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit) {
        this.rateLimiterName = rateLimiterName;
        this.threadName = threadName;
        this.mode = mode;
        this.rate = rate;
        this.rateInterval = rateInterval;
        this.rateIntervalUnit = rateIntervalUnit;
    }
}
