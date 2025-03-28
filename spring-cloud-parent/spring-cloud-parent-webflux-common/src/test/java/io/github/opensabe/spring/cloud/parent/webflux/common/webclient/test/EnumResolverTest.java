package io.github.opensabe.spring.cloud.parent.webflux.common.webclient.test;

import io.github.opensabe.base.vo.IntValueEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author heng.ma
 */
@Log4j2
@SpringJUnitConfig
@AutoConfigureObservability
@SpringBootTest(
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.openfeign.jfr.enabled=false",
                "spring.servlet.jfr.enabled=false"
        },
        classes = EnumResolverTest.App.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class EnumResolverTest {

    @SpringBootApplication
    public static class App {

        @Bean
        public TestController controller () {
            return new TestController();
        }
    }

    @RestController
    public static class TestController {

        @GetMapping("test/param")
        public Type test (Type type) {
            return type;
        }
        @PostMapping("test/body")
        public Type test (@RequestBody Param param) {
            return param.type();
        }



    }

    public record Param (Type type) {

    }

    @Getter
    @AllArgsConstructor
    public enum Type implements IntValueEnum {

        NORMAL(1),

        HUM(2)

        ;

        private final Integer value;
        @Override
        public Integer getValue() {
            return value;
        }
    }

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void testParam () {
        int type = testRestTemplate.getForObject("/test/param?type=2", int.class);
        Assertions.assertEquals(2, type);
    }

    @Test
    void testBody () {
        int type = testRestTemplate.postForObject("/test/body", Map.of("type", 1),int.class);
        Assertions.assertEquals(1, type);
    }
}
