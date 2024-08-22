package io.github.opensabe.common.dynamodb.test.common;

import com.alibaba.fastjson.JSON;
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
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

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
    private DynamoDbClient dynamoDbClient;

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
        try {
            CreateTableResponse table = dynamoDbClient.createTable((builder -> {

                builder.tableName("dynamodb_" + aws_env + "_eight_data_types");
                builder.provisionedThroughput((p) -> {
                            p.readCapacityUnits(500l);
                            p.writeCapacityUnits(500l);
                        }
                )
                ;
                builder.keySchema(KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build(),
                        KeySchemaElement.builder().attributeName("order").keyType(KeyType.RANGE).build());

                builder.attributeDefinitions(
                        AttributeDefinition.builder().attributeName("id").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("order").attributeType(ScalarAttributeType.N).build());

            }));
            log.info("DynamicDBTest-createTable result {}", JSON.toJSONString(table));
        }  catch (Exception e) {
            log.warn("create dynamodb table error: {}", e.getMessage());
        }

        try {
            dynamoDbClient.createTable(builder ->
                    builder.tableName("dynamodb_" + aws_env + "_" + defaultOperId + "_typehandler")
                            .provisionedThroughput((p) -> {
                                p.readCapacityUnits(500l);
                                p.writeCapacityUnits(500l);
                            })
                            .keySchema(
                                    KeySchemaElement.builder().attributeName("key").keyType(KeyType.HASH).build()
                            )
                            .attributeDefinitions(
                                    AttributeDefinition.builder().attributeName("key").attributeType(ScalarAttributeType.S).build()
                            )

            );
        } catch (Exception e) {
            log.warn("create dynamodb table error: {}", e.getMessage());
        }
    }
}
