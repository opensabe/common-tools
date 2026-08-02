/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.dynamodb.service;

import org.springframework.core.env.Environment;

import io.github.opensabe.common.dynamodb.annotation.HashKeyName;
import io.github.opensabe.common.dynamodb.annotation.TableName;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

/**
 * KeyValueDynamoDbService。
 */
public class KeyValueDynamoDbService extends DynamoDbBaseService<KeyValueDynamoDbService.KeyValueMap> {


    public KeyValueDynamoDbService(Environment environment, DynamoDbEnhancedClient client) {
        super(environment, client);
    }

    @TableName(name = "dynamodb_${aws_env}_${defaultOperId}_typehandler")
    public static class KeyValueMap {
        @HashKeyName(name = "key")
/** key。 */
        private String key;
/** value。 */
        private String value;

        /**
         * @return key
         */
        public String getKey() {
            return key;
        }

        /**
         * @param key 待设置值
         */
        public void setKey(String key) {
            this.key = key;
        }

        /**
         * @return value
         */
        public String getValue() {
            return value;
        }

        /**
         * @param value 待设置值
         */
        public void setValue(String value) {
            this.value = value;
        }
    }
}
