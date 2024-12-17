package io.github.opensabe.common.redisson.observation.ratelimiter;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;

import java.time.Duration;
import java.util.Objects;

@Getter
@Setter
public class RRateLimiterSetRateContext extends Observation.Context {
    private final String rateLimiterName;
    private final String threadName;
    private final RateType mode;
    private final long rate;
    private final long rateInterval;
    private final RateIntervalUnit rateIntervalUnit;
    private final long keepAlive;
    private boolean setRateSuccessfully;

    public RRateLimiterSetRateContext(String rateLimiterName, String threadName, RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit) {
        this.rateLimiterName = rateLimiterName;
        this.threadName = threadName;
        this.mode = mode;
        this.rate = rate;
        this.rateInterval = rateInterval;
        this.rateIntervalUnit = rateIntervalUnit;
        this.keepAlive=Duration.ZERO.toMillis();
    }

    public RRateLimiterSetRateContext(String rateLimiterName, String threadName, RateType mode, long rate, Duration rateInterval, Duration keepAlive) {
        this.rateLimiterName = rateLimiterName;
        this.threadName = threadName;
        this.mode = mode;
        this.rate = rate;
        if(Objects.isNull(rateInterval)){
            this.rateInterval=Duration.ZERO.toMillis();
        } else {
            this.rateInterval = rateInterval.toMillis();
        }
        this.rateIntervalUnit=RateIntervalUnit.MILLISECONDS;
        if(Objects.isNull(keepAlive)){
            this.keepAlive=Duration.ZERO.toMillis();
        } else {
            this.keepAlive = keepAlive.toMillis();
        }
    }
}
