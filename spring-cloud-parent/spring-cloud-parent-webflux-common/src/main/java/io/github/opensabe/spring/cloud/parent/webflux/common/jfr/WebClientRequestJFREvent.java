package io.github.opensabe.spring.cloud.parent.webflux.common.jfr;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Category({"observation", "webclient"})
@Label("WebClient Request")
@StackTrace(false)
public class WebClientRequestJFREvent extends Event {
    @Label("httpMethod")
    @Description("the httpMethod of the request")
    private String httpMethod;

    @Label("url")
    @Description("the url of the request")
    private String url;

    @Label("requestHeaders")
    @Description("the headers of the request")
    private String requestHeaders;

    @Label("throwable")
    @Description("the error of the request")
    private String throwable;

    @Label("traceId")
    @Description("the traceId of the request")
    private String traceId;

    @Label("spanId")
    @Description("the spanId of the request")
    private String spanId;

    @Label("status")
    @Description("the status of the response")
    private int status;

    @Label("responseHeaders")
    @Description("the headers of the response")
    private String responseHeaders;
}
