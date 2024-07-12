package io.github.opensabe.common.dynamodb.Service;

import io.github.opensabe.common.dynamodb.annotation.HashKeyName;
import io.github.opensabe.common.dynamodb.annotation.TableName;

public class KeyValueDynamoDbService extends DynamoDbBaseService<KeyValueDynamoDbService.KeyValueMap> {

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
