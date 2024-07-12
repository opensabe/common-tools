package io.github.opensabe.scheduler.jfr;

import jdk.jfr.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseEvent extends Event {
    private String traceId;

    private String spanId;
}
