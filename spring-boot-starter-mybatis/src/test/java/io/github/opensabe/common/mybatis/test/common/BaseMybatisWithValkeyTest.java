package io.github.opensabe.common.mybatis.test.common;

import io.github.opensabe.common.s3.properties.S3Properties;
import io.github.opensabe.common.testcontainers.integration.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.util.Optional;

@Log4j2
@AutoConfigureObservability
@JfrEventTest
@ExtendWith({
        SpringExtension.class,
        ReadWriteMySQLIntegrationTest.class,
        SingleS3IntegrationTest.class,
        SingleDynamoDbIntegrationTest.class,
        SingleValkeyIntegrationTest.class,
})
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "aws.s3.folderName="+ BaseMybatisWithValkeyTest.FOLDER_NAME,
        "aws.s3.defaultBucket=" + BaseMybatisWithValkeyTest.BUCKET_NAME
}, classes = BaseMybatisWithValkeyTest.App.class)
public abstract class BaseMybatisWithValkeyTest {
    @SpringBootApplication(scanBasePackages = "io.github.opensabe.common.mybatis.test")
    public static class App {
    }
    public static final String FOLDER_NAME = "testFolder/country";
    public static final String BUCKET_NAME = "test-bucket";

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        ReadWriteMySQLIntegrationTest.setProperties(registry);
        SingleDynamoDbIntegrationTest.setProperties(registry);
        SingleS3IntegrationTest.setProperties(registry);
        SingleValkeyIntegrationTest.setProperties(registry);
    }

    @Autowired
    private S3Client s3Client;
    @Autowired
    private S3Properties s3Properties;
    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Value("${aws_env}")
    private String aws_env;
    @Value("${defaultOperId}")
    private String defaultOperId;

    @BeforeEach
    public void initializeBucket() {
        ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
        Optional<Bucket> first = listBucketsResponse.buckets().stream().filter(bucket1 -> StringUtils.equals(bucket1.name(), s3Properties.getDefaultBucket())).findFirst();
        if(!first.isPresent()){
            s3Client.createBucket(CreateBucketRequest.builder().bucket(s3Properties.getDefaultBucket()).build());
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
