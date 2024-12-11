package io.github.opensabe.common.elasticsearch.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"ElasticSearch"})
@Label("Client Request")
@StackTrace(value = false)
public class ElasticSearchClientJfrEvent extends Event {
    private final String uri;
    private final String params;

    private String traceId;
    private String spanId;

    private String response;
    private Throwable throwable;

    public ElasticSearchClientJfrEvent(String uri, String params) {
        this.uri = uri;
        this.params = params;
    }
}
