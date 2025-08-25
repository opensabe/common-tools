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

import cn.hutool.core.util.ReflectUtil;
import io.github.opensabe.common.dynamodb.service.DynamoDbBaseService;
import io.github.opensabe.common.testcontainers.integration.SingleDynamoDbIntegrationTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.lang.reflect.Field;
import java.util.List;

@Log4j2
@JfrEventTest
@AutoConfigureObservability
@ExtendWith({SpringExtension.class, SingleDynamoDbIntegrationTest.class})
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.application.name=aws-dynamo-db-test"
}, classes = App.class)
public abstract class DynamicdbStarter {
    @Autowired
    private List<DynamoDbBaseService> services;

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleDynamoDbIntegrationTest.setProperties(registry);
    }

    @Value("${aws_env}")
    private String aws_env;
    @Value("${defaultOperId}")
    private String defaultOperId;


    @BeforeEach
    void createTable() {
        Field field = ReflectUtil.getField(DynamoDbBaseService.class, "table");
        services.forEach(s ->  {
            DynamoDbTable table = (DynamoDbTable) ReflectUtil.getFieldValue(s, field);
            try {
                table.createTable();
            } catch (Exception ignore) {

            }
        });
    }
}
