package io.github.opensabe.common.dynamodb.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"observation","Dynamodb-Execute"})
@Label("Dynamodb Execute Monitor")
@StackTrace(value = false)
public class DynamodbExecuteEvent extends Event {

    @Label("SQL Executed Method")
    private final String method;

    private String traceId;
    private String spanId;
    private String hashKey;
    private String rangeKey;
    private String expression;

    public DynamodbExecuteEvent(String method,String hashKey,String rangeKey, String expression) {
        this.method = method;
        this.hashKey = hashKey;
        this.rangeKey = rangeKey;
        this.expression = expression;
    }

}