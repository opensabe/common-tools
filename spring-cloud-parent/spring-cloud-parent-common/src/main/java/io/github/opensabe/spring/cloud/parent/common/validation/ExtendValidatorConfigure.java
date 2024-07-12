package io.github.opensabe.spring.cloud.parent.common.validation;

import io.github.opensabe.spring.cloud.parent.common.validation.annotation.IntegerEnumedValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.annotation.*;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.function.Consumer;

/**
 * 补充notBlank，强化notNull
 * <p>
 * 项目中有
 * {@link org.springframework.web.servlet.config.annotation.EnableWebMvc}
 * <p>
 *     又或者项目中有自定义的
 *     {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport}
 *     子类
 *  需要覆盖getValidator方法，否则会用默认的validator
 *  <p>
 *      为了解决这个问题，可以实现{@link org.springframework.web.servlet.config.annotation.WebMvcConfigurer}
 *      接口来替代父类
 *   <p>
 *  <b>Webflux同理</b>
 * @author heng.ma
 */
@Import(ExtendValidatorConfigure.ConsumerConfig.class)
@AutoConfigureBefore(ValidationAutoConfiguration.class)
@AutoConfigureAfter(ExtendValidatorConfigure.ConsumerConfig.class)
@Configuration(proxyBeanMethods = false)
public class ExtendValidatorConfigure {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Primary
    public LocalValidatorFactoryBean validatorBean (ObjectProvider<List<Consumer<ConstraintMapping>>> consumerProvider) {
        var consumer = consumerProvider.getIfAvailable().stream().reduce(a -> {},Consumer::andThen);
        var validation = new ValidatorFactoryBean(consumer);
        MessageInterpolatorFactory interpolatorFactory = new MessageInterpolatorFactory();
        validation.setMessageInterpolator(interpolatorFactory.getObject());
        return validation;
    }

    @AutoConfigureBefore(ExtendValidatorConfigure.class)
    @Configuration(proxyBeanMethods = false)
    public static class ConsumerConfig {
        @Bean
        public Consumer<ConstraintMapping> extendConstraint () {
            return mapping -> {
                mapping.constraintDefinition(NotBlank.class).validatedBy(ObjectBlankValidator.class);
                mapping.constraintDefinition(NotNull.class).validatedBy(StringNotNullValidator.class);
                mapping.constraintDefinition(IntegerEnumedValue.class).validatedBy(IntegerEnumedValidator.class);
            };
        }
    }


}
