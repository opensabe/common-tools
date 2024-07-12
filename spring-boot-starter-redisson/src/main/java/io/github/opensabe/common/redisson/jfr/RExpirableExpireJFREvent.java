package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.rexpirable.RExpirableExpireContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Category({"observation", "redisson", "rexpirable"})
@Label("expire")
@StackTrace(false)
public class RExpirableExpireJFREvent extends Event {
    @Label("expirable name")
    private final String expirableName;
    @Label("expire")
    private final String expire;
    @Label("is expire set successfully")
    private boolean expireSetSuccessfully;
    private String traceId;
    private String spanId;

    public RExpirableExpireJFREvent(RExpirableExpireContext rExpirableExpireContext) {
        this.expirableName = rExpirableExpireContext.getExpirableName();
        this.expire = rExpirableExpireContext.getExpire();
    }
}
