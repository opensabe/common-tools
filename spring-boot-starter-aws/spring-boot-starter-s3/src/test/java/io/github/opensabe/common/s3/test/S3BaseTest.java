package io.github.opensabe.common.s3.test;

import io.github.opensabe.common.s3.properties.S3Properties;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.util.Optional;

@Log4j2
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        //spring-boot 2.6.x 开始，禁止循环依赖（A -> B, B -> A），字段注入一般会导致这种循环依赖，但是我们字段注入太多了，挨个检查太多了
        "spring.main.allow-circular-references=true",
        "aws.s3.enabled=true",
        "aws.s3.folderName="+ S3BaseTest.FOLDER_NAME,
        "aws.s3.accessKeyId=fake",
        "aws.s3.accessKey=fake",
        "aws.s3.defaultBucket=test",
        "aws.s3.region=us-east-1",
        "awsS3LocalUrl=http://s3.localhost.localstack.cloud:" + S3BaseTest.S3_PORT,
        "mapper.mappers[0]=io.github.opensabe.common.mybatis.base.BaseMapper",
        "mybatis.configuration.map-underscore-to-camel-case=true",
        "pagehelper.offset-as-page-num=true",
        "pagehelper.support-methods-arguments=true",
        "country.map.0=public",
        "defaultOperId=0",
        "jdbc.config.common.base-packages[0]=io.github.opensabe.common.config.dal.db.dao",
        "jdbc.config.common.data-source[0].cluster-name=public",
        "jdbc.config.common.data-source[0].driver-class-name=org.sqlite.JDBC",
        "jdbc.config.common.data-source[0].is-write-allowed=true",
        "jdbc.config.common.data-source[0].name=user-1",
        "jdbc.config.common.data-source[0].url=jdbc:sqlite:user1.db",
        //使用 sqlite 不支持 prepared statement
        "jdbc.config.common.data-source[0].pool_prepared_statements=false",
        "jdbc.config.common.default-cluster-name=public",
        "jdbc.config.common.transaction-service-packages=io.github.opensabe.common",
}, classes = App.class)
@AutoConfigureObservability
public abstract class S3BaseTest {
    public static final String FOLDER_NAME = "testFolder/country";
    public static final int S3_PORT = 4566;

    @ClassRule
    public static GenericContainer AWS_S3 = new FixedHostPortGenericContainer("localstack/localstack")
            .withFixedExposedPort(S3_PORT, S3_PORT)
            .withExposedPorts(S3_PORT);

    @BeforeAll
    static void setup() {
        AWS_S3.start();
        log.info("S3 started at port: {}", S3_PORT);
    }

    @AfterAll
    static void destroy() {
        AWS_S3.stop();
        log.info("S3 stopped");
    }

    @Autowired
    private S3Client s3Client;
    @Autowired
    private S3Properties s3Properties;

    @BeforeEach
    public void initializeBucket() {
        ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
        Optional<Bucket> first = listBucketsResponse.buckets().stream().filter(bucket1 -> StringUtils.equals(bucket1.name(), s3Properties.getDefaultBucket())).findFirst();
        if(!first.isPresent()){
            s3Client.createBucket(CreateBucketRequest.builder().bucket(s3Properties.getDefaultBucket()).build());
        }
    }
}
