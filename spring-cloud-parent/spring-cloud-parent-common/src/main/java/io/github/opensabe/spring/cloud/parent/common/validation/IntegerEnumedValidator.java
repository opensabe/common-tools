package io.github.opensabe.spring.cloud.parent.common.validation;


import io.github.opensabe.spring.cloud.parent.common.validation.annotation.IntegerEnumedValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class IntegerEnumedValidator implements ConstraintValidator<IntegerEnumedValue,Integer> {

    private Set<Integer> set = new HashSet<>();
    @Override
    public void initialize(IntegerEnumedValue constraintAnnotation) {
        Arrays.stream(constraintAnnotation.value()).forEach(set::add);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return Objects.isNull(value)||set.contains(value);
    }
}
