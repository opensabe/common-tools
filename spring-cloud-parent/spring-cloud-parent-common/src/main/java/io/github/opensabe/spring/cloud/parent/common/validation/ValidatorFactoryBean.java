package io.github.opensabe.spring.cloud.parent.common.validation;

import jakarta.annotation.Nullable;
import jakarta.validation.Configuration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public class ValidatorFactoryBean extends LocalValidatorFactoryBean {

    private final Consumer<ConstraintMapping> mappingConsumer;

    public ValidatorFactoryBean(Consumer<ConstraintMapping> mappingConsumer) {
        this.mappingConsumer = mappingConsumer;
    }

    /**
     * Post-process the given Bean Validation configuration,
     * adding to or overriding any of its settings.
     * <p>Invoked right before building the {@link jakarta.validation.ValidatorFactory}.
     * @param configuration the Configuration object, pre-populated with
     * settings driven by LocalValidatorFactoryBean's properties
     */
    protected void postProcessConfiguration(@Nullable Configuration<?> configuration) {
        if (configuration instanceof ConfigurationImpl config) {
            var mapping = config.createConstraintMapping();
            if (Objects.nonNull(mappingConsumer)) {
                mappingConsumer.accept(mapping);
            }
            config.addMapping(mapping);
            config.failFast(true);
            config.locales(Locale.ENGLISH);
        }
    }
}
