package io.github.opensabe.spring.cloud.parent.web.common.test;


import io.github.opensabe.base.vo.BaseRsp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@DisplayName("测试全局的ExceptionHandler跟Validation兼容问题")
@AutoConfigureObservability
@SpringBootTest(classes = ServletExceptionHandlerTest.App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "eureka.client.enabled=false",
        "spring.cloud.openfeign.jfr.enabled=false",
        "spring.servlet.jfr.enabled=false"
})
public class ServletExceptionHandlerTest {

    @SpringBootApplication
    public static class App {

        @Bean
        public Controller controller () {
            return new Controller();
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @DisplayName("测试ThrowableHandler是否能获取到uri")
    @Test
    void testURI () {
        BaseRsp baseRsp = restTemplate.getForObject("/runtime", BaseRsp.class);
        System.out.println(baseRsp);
    }

    @RestController
    public static class Controller {

        @GetMapping("/runtime")
        public BaseRsp<Void> runtimeException () {
            throw new RuntimeException("test");
        }
    }
}
