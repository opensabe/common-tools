package io.github.opensabe.common.redisson.observation.rexpirable;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RExpirableExpireContext extends Observation.Context {
    private final String expirableName;
    private final String threadName;
    private final String expire;

    private boolean expireSetSuccessfully;

    public RExpirableExpireContext(String expirableName, String threadName, String expire) {
        this.expirableName = expirableName;
        this.threadName = threadName;
        this.expire = expire;
    }
}
