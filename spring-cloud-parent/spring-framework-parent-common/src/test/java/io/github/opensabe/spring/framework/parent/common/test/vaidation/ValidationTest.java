package io.github.opensabe.spring.framework.parent.common.test.vaidation;

import io.github.opensabe.spring.cloud.parent.common.validation.annotation.IntegerEnumedValue;
import io.github.opensabe.spring.cloud.parent.common.validation.group.Insert;
import io.github.opensabe.spring.cloud.parent.common.validation.group.Update;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;


@Log4j2
@AutoConfigureObservability
@DisplayName("测试自定义的Validation注解是否生效")
@SpringBootTest(classes = ValidationTest.App.class)
public class ValidationTest {


    @Autowired
    private Validator validator;
    @Autowired
    private jakarta.validation.Validator validation;

    @SpringBootApplication
    public static class App {

    }


    @Test
    @DisplayName("测试validator bean是LocalValidatorFactoryBean实例")
    void testInstance () {
        //LocalValidatorFactoryBean实现了spring的validator和jakarta的validator
        Assertions.assertInstanceOf(LocalValidatorFactoryBean.class, validator);
        Assertions.assertInstanceOf(LocalValidatorFactoryBean.class, validation);

    }


    @Test
    @DisplayName("测试failFast,多个参数错误时，遇到一个就返回")
    void testFastFail () {
        Set<ConstraintViolation<Param>> validate = validation.validate(new Param(null, null,1, 1));
        Assertions.assertEquals(1, validate.size());
    }

    @DisplayName("测试NotBlank是否对非string生效")
    @Test
    void testNotBlankForInt () {
        Set<ConstraintViolation<Param>> validate = validation.validate(new Param("111", "333",1, null), Update.class);
        Assertions.assertEquals(1, validate.size());
        ConstraintViolation<Param> constraintViolation = validate.stream().findFirst().orElseThrow();
        Assertions.assertEquals("type", constraintViolation.getPropertyPath().toString());
    }

    @DisplayName("测试自定义注解是否生效")
    @Test
    void testIntEnum () {
        Set<ConstraintViolation<Param>> validate = validation.validate(new Param("111", "333",100, 1), Insert.class);
        Assertions.assertEquals(1, validate.size());
        ConstraintViolation<Param> constraintViolation = validate.stream().findFirst().orElseThrow();
        log.info(constraintViolation.getMessage());
        Assertions.assertEquals("age", constraintViolation.getPropertyPath().toString());
    }




    public record Param (
            @NotBlank(message = "id is null") String id,
            @NotBlank(message = "name is null") String name,
            @NotBlank(groups = Insert.class) @IntegerEnumedValue({1,2,3}) Integer age,
            @NotBlank(groups = Update.class) Integer type
    ) {

    }
}
