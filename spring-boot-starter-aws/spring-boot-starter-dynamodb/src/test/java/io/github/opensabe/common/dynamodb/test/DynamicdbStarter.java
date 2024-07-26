package io.github.opensabe.common.dynamodb.test;

import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@Log4j2
@JfrEventTest
@AutoConfigureObservability
@SpringBootTest(properties = {
        "aws_access_key_id=fake",
        "aws_secret_access_key=fake",
        "aws_env=test",
        "dynamolLocalUrl=http://localhost:8000",
        "management.tracing.sampling.probability=1",
        "defaultOperId=2",
        "spring.application.name=aws"
}, classes = App.class)
public class DynamicdbStarter {
    public JfrEvents jfrEvents = new JfrEvents();
    @ClassRule
    public static GenericContainer dynamodb = new FixedHostPortGenericContainer("amazon/dynamodb-local")
            .withFixedExposedPort(8000, 8000)
            .withExposedPorts(8000);

    @BeforeAll
    static void setup() {
        dynamodb.start();
        System.out.println("-------");
    }
    @AfterAll
    static void destroy() {
        dynamodb.stop();
    }

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Value("${aws_env:test}")
    private String aws_env;
    @Value("${defaultOperId:2}")
    private String defaultOperId;


    @BeforeEach
    void createTable() {
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

        dynamoDbClient.createTable(builder ->
                builder.tableName("dynamodb_"+aws_env+"_"+defaultOperId+"_typehandler")
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
    }

    @AfterEach
    void dropTable () {
        dynamoDbClient.deleteTable(builder -> builder.tableName("dynamodb_" + aws_env + "_eight_data_types"));
        dynamoDbClient.deleteTable(builder -> builder.tableName("dynamodb_"+aws_env+"_"+defaultOperId+"_typehandler"));
    }
}
