package io.github.opensabe.common.dynamodb.config;

import io.github.opensabe.common.dynamodb.Service.KeyValueDynamoDbService;
import io.github.opensabe.common.dynamodb.typehandler.DynamoDbOBService;
import io.github.opensabe.common.dynamodb.typehandler.DynamodbConverter;
import io.github.opensabe.common.typehandler.OBSService;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Author: duchaoqun
 * Date: 2019/10/10 0010 14:11
 */
@ConditionalOnProperty("aws_access_key_id")
@Log4j2
@Configuration(proxyBeanMethods = false)
public class DynamoDBConfiguration {

    @Value("${aws_access_key_id}")
    private String accessKey;
    @Value("${aws_secret_access_key}")
    private String secretKey;
    @Value("${aws_region:}")
    private String awsRegion;
    @Value("${dynamolLocalUrl:}")
    private String dynamolLocalUrl;
    private DynamoDbClient dynamoDbClient;

    @Bean
    public DynamoDbClient s3Client() {
        Region region;
        if (StringUtils.isBlank(awsRegion)) {
            region = Region.EU_CENTRAL_1;
        } else {
            List<Region> regions = Region.regions();
            Optional<Region> optional = regions.stream().filter(v -> StringUtils.equals(v.id(), awsRegion)).findAny();
            region = optional.orElse(Region.EU_CENTRAL_1);
        }
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(
                accessKey,
                secretKey);
        DynamoDbClientBuilder builder = DynamoDbClient.builder();
        builder.credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials)).region(region);
        if(StringUtils.isNotEmpty(dynamolLocalUrl)){
            log.fatal("dynamolDB will use local url {}", dynamolLocalUrl);
            builder.endpointOverride(URI.create(dynamolLocalUrl));
        }
        dynamoDbClient = builder.build();
        return dynamoDbClient;
    }

    @PreDestroy
    public void shutdown() {
        log.info("aws dynamoDbClient shutdown...");
        dynamoDbClient.close();
    }

    @Bean
    public KeyValueDynamoDbService keyValueDynamoDbService() {
        return new KeyValueDynamoDbService();
    }

    @Bean
    @ConditionalOnClass(KeyValueDynamoDbService.class)
    public OBSService dynamoDbOBSService(KeyValueDynamoDbService dynamoDbBaseService) {
        return new DynamoDbOBService(dynamoDbBaseService);
    }

    @Bean
    public DynamodbConverter dynamodbConverter (DynamoDbClient client, @Value("${aws_env}") String env) {
        return new DynamodbConverter(client, env);
    }
}
