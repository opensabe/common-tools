package io.github.opensabe.common.dynamodb.observation;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DynamodbExecuteContext extends Observation.Context {

    private String method;

    private String hashKey;

    private String rangeKey;
    public DynamodbExecuteContext(String method, String hashKey, String rangeKey) {
        this.method = method;
        this.hashKey = hashKey;
        this.rangeKey = rangeKey;
    }

    public DynamodbExecuteContext(String method) {
        this.method = method;
        this.hashKey = "";
        this.rangeKey = "";
    }
}
