package io.github.opensabe.common.testcontainers.integration;

import io.github.opensabe.common.testcontainers.CustomizedMySQLContainer;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * 注意使用这个类的单元测试，用的是同一个 MySQL，不同单元测试注意隔离不同的 key
 */
@Log4j2
public class SingleWriteMySQLIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    public static final CustomizedMySQLContainer MYSQL_WRITE = new CustomizedMySQLContainer();


    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        //由于单元测试并发执行，这个只能启动一次，所以加锁
        if (!MYSQL_WRITE.isRunning()) {
            synchronized (SingleWriteMySQLIntegrationTest.class) {
                if (!MYSQL_WRITE.isRunning()) {
                    MYSQL_WRITE.start();
                }
            }
        }
    }

    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("country.map.0", () -> "public");
        registry.add("defaultOperId", () -> 0);
        registry.add("jdbc.config.user.base-packages[0]", () -> "io.github.opensabe.common.mybatis.test.mapper.user");
        registry.add("jdbc.config.user.base-packages[1]", () -> "io.github.opensabe.common.config.dal.db.dao");
        registry.add("jdbc.config.user.data-source[0].cluster-name", () -> "public");
        registry.add("jdbc.config.user.data-source[0].driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("jdbc.config.user.data-source[0].is-write-allowed", () -> true);
        registry.add("jdbc.config.user.data-source[0].name", () -> "user-1");
        registry.add("jdbc.config.user.data-source[0].min-idle", () -> 1);
        registry.add("jdbc.config.user.data-source[0].initial-size", () -> 1);
        registry.add("jdbc.config.user.data-source[0].max-active", () -> 2);
        registry.add("jdbc.config.user.data-source[0].username", () -> "root");
        registry.add("jdbc.config.user.data-source[0].password", () -> CustomizedMySQLContainer.MYSQL_ROOT_PASSWORD);
        registry.add("jdbc.config.user.data-source[0].url", () -> "jdbc:mysql://" + MYSQL_WRITE.getHost() +":" + MYSQL_WRITE.getMysqlPort() + "/test");
        registry.add("jdbc.config.user.default-cluster-name", () -> "public");

        registry.add("mapper.mappers[0]", () -> "io.github.opensabe.common.mybatis.base.BaseMapper");
        registry.add("mybatis.configuration.map-underscore-to-camel-case", () -> true);
        registry.add("pagehelper.offset-as-page-num", () -> true);
        registry.add("pagehelper.support-methods-arguments", () -> true);
    }

    @Override
    public void close() throws Throwable {
        MYSQL_WRITE.stop();
    }
}
