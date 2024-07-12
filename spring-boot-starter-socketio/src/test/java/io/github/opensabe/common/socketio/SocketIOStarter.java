package io.github.opensabe.common.socketio;

import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

@Log4j2
@JfrEventTest
@AutoConfigureObservability
@SpringBootTest(properties = {
        "logging.pattern.io.github.opensabe=%d{yyyy-MM-dd HH:mm:ss.SSS} %5p [test,%X{traceId},%X{spanId}] [%t][%C:%L]: %m%n",
        "spring.main.allow-circular-references=true",
        "appId=test",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "mapper.mappers[0]=io.github.opensabe.common.mybatis.base.BaseMapper",
        "mybatis.configuration.map-underscore-to-camel-case=true",
        "pagehelper.offset-as-page-num=true",
        "pagehelper.support-methods-arguments=true",
        "country.map.0=public",
        "defaultOperId=0",
        "jdbc.config.common.base-packages[0]=io.github.opensabe.common.config.dal.db.dao",
        "jdbc.config.common.data-source[0].cluster-name=public",
        "jdbc.config.common.data-source[0].driver-class-name=com.mysql.jdbc.Driver",
        "jdbc.config.common.data-source[0].is-write-allowed=true",
        "jdbc.config.common.data-source[0].name=user-1",
        "jdbc.config.common.data-source[0].username=root",
        "jdbc.config.common.data-source[0].password=123456",
        "jdbc.config.common.data-source[0].url=jdbc:mysql://127.0.0.1:3307/sys",
        //使用 sqlite 不支持 prepared statement
        "jdbc.config.common.data-source[0].pool_prepared_statements=false",
        "jdbc.config.common.default-cluster-name=public",
        "jdbc.config.common.transaction-service-packages=io.github.opensabe.common",
        "spring.application.name=socketio",
        "rocketmq.producer.group=socketio",
        "rocketmq.name-server=localhost:9876",
}, classes = SocketIOStarter.App.class)
public class SocketIOStarter {
    @SpringBootApplication(scanBasePackages = {"io.github.opensabe.spring.boot.starter.socketio"})
    public static class App {
        public static void main(String[] args) {
            SpringApplication.run(App.class, args);
        }
    }

    public JfrEvents jfrEvents = new JfrEvents();
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    @ClassRule
    @SuppressWarnings({"deprecation", "resource"})
    public static GenericContainer<?> redisServer = new FixedHostPortGenericContainer<>("redis")
            .withFixedExposedPort(6379,6379)
            .withExposedPorts(6379);
    @ClassRule
    @SuppressWarnings({"deprecation", "resource"})
    public static GenericContainer<?> mysql = new FixedHostPortGenericContainer<>("mysql")
            .withFixedExposedPort(3307,3306)
            .withExposedPorts(3306)
            .withEnv("MYSQL_ROOT_PASSWORD", "123456");
    @ClassRule
    @SuppressWarnings({"deprecation", "resource"})
    public static GenericContainer<?> rocketmq = new FixedHostPortGenericContainer<>("maheng157/rocketmq-local:5.1.4")
            .withFixedExposedPort(9876,9876)
            .withFixedExposedPort(10911,10911)
            .withExposedPorts(9876,10911)
            ;
    @BeforeAll
    static void setup() {
        redisServer.start();
        mysql.start();
        rocketmq.start();
    }
    @AfterAll
    static void destroy() {
        redisServer.stop();
        mysql.stop();
        rocketmq.stop();
    }

    @BeforeEach
    void createTable() {
        rocketMQTemplate.getProducer().setSendMsgTimeout(10000);
    }

    @AfterEach
    void dropTable () {
    }
}
