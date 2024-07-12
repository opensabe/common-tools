package io.github.opensabe.common.mybatis.test;

import io.github.opensabe.common.mybatis.test.manager.UserManager;
import io.github.opensabe.common.mybatis.test.mapper.user.OrderMapperService;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.Statement;


@Log4j2
public class TestObservation extends BaseDataSourceTest{

    @SuppressWarnings("unused")
    public JfrEvents jfrEvents = new JfrEvents();

    @Autowired
    private UserManager userManager;

    @Autowired
    private OrderMapperService orderMapperService;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;
    @BeforeEach
    public void initDataBase() {
        dynamicRoutingDataSource.getResolvedDataSources().values().forEach(dataSource -> {
            try (
                    Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement()
            ) {
                statement.execute("create table if not exists t_user(" +
                        "id varchar(64) primary key, first_name varchar(128), last_name varchar(128), create_time timestamp(3), properties varchar(128)" +
                        ");");
                statement.execute("delete from t_user;");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void testMapperProxy () {
        unifiedObservationFactory.getCurrentOrCreateEmptyObservation()
                        .observe(() -> {
                            log.info("test traceId out of proxy");
                            userManager.queryMuiltple("111");
                        });
    }

    @Test
    void testTransaction () {
        unifiedObservationFactory.getCurrentOrCreateEmptyObservation()
                .observe(() -> {
                    log.info("test traceId out of proxy");
                    userManager.testCommit("aaa");
                });
    }

}
