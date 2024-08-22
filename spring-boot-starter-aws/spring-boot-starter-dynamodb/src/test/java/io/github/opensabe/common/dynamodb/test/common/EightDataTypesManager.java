package io.github.opensabe.common.dynamodb.test.common;

import io.github.opensabe.common.dynamodb.Service.DynamoDbBaseService;
import io.github.opensabe.common.dynamodb.test.po.EightDataTypesPo;
import org.springframework.stereotype.Service;

@Service
public class EightDataTypesManager extends DynamoDbBaseService<EightDataTypesPo> {

}
