package io.github.opensabe.common.mybatis.test;

import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import io.github.opensabe.common.s3.properties.S3Properties;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;

@Log4j2
@AutoConfigureObservability
@JfrEventTest
@SpringBootTest(properties = {
        //spring-boot 2.6.x 开始，禁止循环依赖（A -> B, B -> A），字段注入一般会导致这种循环依赖，但是我们字段注入太多了，挨个检查太多了
        "spring.main.allow-circular-references=true",
        "spring.application.name=test",
        "mapper.mappers[0]=io.github.opensabe.common.mybatis.base.BaseMapper",
        "mybatis.configuration.map-underscore-to-camel-case=true",
        "pagehelper.offset-as-page-num=true",
        "pagehelper.support-methods-arguments=true",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "country.map.0=public",
        "defaultOperId=0",
        "jdbc.config.user.base-packages=io.github.opensabe.common.mybatis.test.mapper.user",
        "jdbc.config.user.data-source[0].cluster-name=public",
        "jdbc.config.user.data-source[0].driver-class-name=com.mysql.jdbc.Driver",
        "jdbc.config.user.data-source[0].is-write-allowed=true",
        "jdbc.config.user.data-source[0].name=user-1",
        "jdbc.config.user.data-source[0].min-idle=1",
        "jdbc.config.user.data-source[0].initial-size=1",
        "jdbc.config.user.data-source[0].username=root",
        "jdbc.config.user.data-source[0].password=123456",
        "jdbc.config.user.data-source[0].url=jdbc:mysql://localhost:3307/sys",
        "jdbc.config.user.data-source[1].cluster-name=public",
        "jdbc.config.user.data-source[1].driver-class-name=com.mysql.jdbc.Driver",
        "jdbc.config.user.data-source[1].is-write-allowed=false",
        "jdbc.config.user.data-source[1].name=user-2",
        "jdbc.config.user.data-source[1].min-idle=1",
        "jdbc.config.user.data-source[1].initial-size=1",
        "jdbc.config.user.data-source[1].username=root",
        "jdbc.config.user.data-source[1].password=123456",
        "jdbc.config.user.data-source[1].url=jdbc:mysql://localhost:3308/sys",
        "jdbc.config.user.default-cluster-name=public",
        "management.auditevents.enabled=false",
        "aws.s3.region=us-east-1",
        "aws.s3.default-bucket=test",
        "aws.s3.profile=test",
        "dynamolLocalUrl=http://localhost:8000",
        "awsS3LocalUrl=http://s3.localhost.localstack.cloud:4566",
        "aws.s3.accessKeyId=fake",
        "aws.s3.accessKey=fake",
        "aws_access_key_id=fake",
        "aws_secret_access_key=fake",
        "aws_env=test",
        "spring.data.redis.host=127.0.0.1",
}, classes = App.class)
public class BaseDataSourceTest {

    @Autowired
    @Qualifier("user.dataSource")
    protected DynamicRoutingDataSource dynamicRoutingDataSource;

    public static GenericContainer<?> REDIS = new FixedHostPortGenericContainer<>("redis")
            .withFixedExposedPort(6379, 6379)
            .withExposedPorts(6379);
    public static GenericContainer<?> MYSQL_WRITE = new FixedHostPortGenericContainer<>("mysql")
            .withFixedExposedPort(3307, 3306)
            .withExposedPorts(3306)
            .withEnv("MYSQL_ROOT_PASSWORD", "123456");

    public static GenericContainer<?> MYSQL_READ = new FixedHostPortGenericContainer<>("mysql")
            .withFixedExposedPort(3308, 3306)
            .withExposedPorts(3306)
            .withEnv("MYSQL_ROOT_PASSWORD", "123456");
    public static GenericContainer dynamodb = new FixedHostPortGenericContainer("amazon/dynamodb-local")
            .withFixedExposedPort(8000, 8000)
            .withExposedPorts(8000);
    public static GenericContainer awsS3 = new FixedHostPortGenericContainer("localstack/localstack")
            .withFixedExposedPort(4566, 4566)
            .withExposedPorts(4566);
    @Autowired
    private S3Client s3Client;
    @Autowired
    private S3Properties s3Properties;
    @BeforeEach
    public void init() {
        log.info("initialize s3 and database");
        ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
        Optional<Bucket> first = listBucketsResponse.buckets().stream().filter(bucket1 -> StringUtils.equals(bucket1.name(), s3Properties.getDefaultBucket())).findFirst();
        if(!first.isPresent()){
            s3Client.createBucket(CreateBucketRequest.builder().bucket(s3Properties.getDefaultBucket()).build());
        }
        dynamicRoutingDataSource.getResolvedDataSources().values().forEach(dataSource -> {
            boolean success = false;
            while (!success) {
                try (
                        Connection connection = dataSource.getConnection();
                        Statement statement = connection.createStatement()
                ) {
                    statement.execute("create table if not exists t_user(" +
                            "id varchar(64) primary key, first_name varchar(128), last_name varchar(128), create_time timestamp(3), properties varchar(128)" +
                            ");");
                    statement.execute("delete from t_user;");
                    log.info("t_user is initialized");
                    statement.execute("create table if not exists t_activity(activity_id varchar(64) primary key, display_setting varchar(1280), biz_type varchar(128), config_setting varchar(1280));");
                    statement.execute("delete from t_activity;");
                    log.info("t_activity is initialized");
                    statement.execute("create table if not exists t_order(id varchar(64) primary key, order_info varchar(1280));");
                    statement.execute("delete from t_order;");
                    log.info("t_order is initialized");
                    success = true;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @BeforeAll
    static void setup () {
        MYSQL_WRITE.start();
        MYSQL_READ.start();
        REDIS.start();
        dynamodb.start();
        awsS3.start();
    }

    @AfterAll
    static void end () {
        MYSQL_WRITE.stop();
        MYSQL_READ.stop();
        REDIS.stop();
        dynamodb.stop();
        awsS3.stop();
    }
}
