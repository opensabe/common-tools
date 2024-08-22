package io.github.opensabe.common.mybatis.test;

import io.github.opensabe.common.mybatis.test.common.BaseMybatisTest;
import io.github.opensabe.common.mybatis.test.manager.UserManager;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


@Log4j2
public class TestObservation extends BaseMybatisTest {

    @Autowired
    private UserManager userManager;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;


    @Test
    @Transactional
    void testMapperProxy () {
        unifiedObservationFactory.getCurrentOrCreateEmptyObservation()
                        .observe(() -> {
                            log.info("test traceId out of proxy");
                            userManager.queryMuiltple("111");
                        });
    }

    @Test
    @Transactional
    void testTransaction () {
        unifiedObservationFactory.getCurrentOrCreateEmptyObservation()
                .observe(() -> {
                    log.info("test traceId out of proxy");
                    userManager.testCommit("aaa");
                });
    }

}
