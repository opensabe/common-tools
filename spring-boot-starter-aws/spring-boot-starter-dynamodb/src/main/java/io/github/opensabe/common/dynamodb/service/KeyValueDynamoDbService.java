package io.github.opensabe.common.dynamodb.service;

import io.github.opensabe.common.dynamodb.annotation.HashKeyName;
import io.github.opensabe.common.dynamodb.annotation.TableName;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

public class KeyValueDynamoDbService extends DynamoDbBaseService<KeyValueDynamoDbService.KeyValueMap> {


    public KeyValueDynamoDbService( Environment environment, DynamoDbEnhancedClient client) {
        super(environment, client);
    }

    @TableName(name = "dynamodb_${aws_env}_${defaultOperId}_typehandler")
    public static class KeyValueMap {
        @HashKeyName(name = "key")
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
