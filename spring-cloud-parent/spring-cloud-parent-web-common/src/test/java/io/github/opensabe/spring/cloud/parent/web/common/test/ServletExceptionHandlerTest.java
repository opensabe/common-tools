package io.github.opensabe.spring.cloud.parent.web.common.test;


import io.github.opensabe.base.RespUtil;
import io.github.opensabe.base.vo.BaseRsp;
import io.github.opensabe.spring.cloud.parent.common.validation.annotation.IntegerEnumedValue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@DisplayName("测试全局的ExceptionHandler跟Validation兼容问题")
@AutoConfigureObservability
@SpringBootTest(classes = ServletExceptionHandlerTest.App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "eureka.client.enabled=false",
        "spring.cloud.openfeign.jfr.enabled=false",
        "spring.servlet.jfr.enabled=false",
        "spring.cloud.config.client.profile=test"
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
    void testThrowableHandlerInjectPath () {
        BaseRsp<Void> rsp = restTemplate.getForObject("/runtime", BaseRsp.class);
        Assertions.assertEquals("this is my test exception", rsp.getInnerMsg());
    }

    @Test
    @DisplayName("测试Validation RequestBody信息")
    void testBindResult () {
        BaseRsp<Void> rsp = restTemplate.postForObject("/body", new Param(null, null), BaseRsp.class);
        System.out.println(rsp.getMessage());
        rsp = restTemplate.postForObject("/body", new Param("1", 4), BaseRsp.class);
        System.out.println(rsp.getMessage());
    }

    @Test
    @DisplayName("测试Validation RequestParam信息")
    void testConstraintViolation () {
        BaseRsp<Void> rsp = restTemplate.getForObject("/param", BaseRsp.class);
        System.out.println(rsp.getMessage());
    }
    @Test
    @DisplayName("测试header或者param缺失")
    void testMissingRequestValueException () {
        BaseRsp<Void> rsp = restTemplate.getForObject("/header", BaseRsp.class);
        System.out.println(rsp.getMessage());
    }


    @Validated
    @RestController
    public static class Controller {

        @GetMapping("/runtime")
        public BaseRsp<Void> runtimeException () {
            throw new RuntimeException("this is my test exception");
        }
        @PostMapping("/body")
        public BaseRsp<Void> bindResult (@Validated @RequestBody Param param) {
            return RespUtil.succ();
        }

        @GetMapping("/param")
        public BaseRsp<Void> constraintViolationException (@NotBlank @RequestParam(required = false) String id) {
            return RespUtil.succ();
        }
        @GetMapping("/header")
        public BaseRsp<Void> missingRequestValueException (@RequestHeader String id) {
            return RespUtil.succ();
        }
    }

    public record Param (@NotBlank String id, @IntegerEnumedValue({1,2,3}) Integer type) {}
}
