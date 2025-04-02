package io.github.opensabe.common.dynamodb.test.common;

import io.github.opensabe.common.dynamodb.service.DynamoDbBaseService;
import io.github.opensabe.common.dynamodb.test.po.EightDataTypesPo;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@Service
public class EightDataTypesManager extends DynamoDbBaseService<EightDataTypesPo> {


    public EightDataTypesManager(Environment environment, DynamoDbEnhancedClient client) {
        super(environment, client);
    }
}
