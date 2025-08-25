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
package io.github.opensabe.common.dynamodb.test.common;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import io.github.opensabe.common.dynamodb.service.DynamoDbBaseService;
import io.github.opensabe.common.dynamodb.test.po.EightDataTypesPo;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@Service
public class EightDataTypesManager extends DynamoDbBaseService<EightDataTypesPo> {


    public EightDataTypesManager(Environment environment, DynamoDbEnhancedClient client) {
        super(environment, client);
    }
}
