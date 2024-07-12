package io.github.opensabe.spring.cloud.parent.common.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;

;

public class ObjectBlankValidator implements ConstraintValidator<NotBlank,Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return isNotEmpty(value);
    }

    private boolean isNotEmpty (Object o) {
        if(o == null)return  false;
        if(o instanceof String){
            return StringUtils.isNotBlank((String)o);
        }
        if(o instanceof Collection){
            return CollectionUtils.isNotEmpty((Collection)o);
        }
        if(o instanceof Map){
            return MapUtils.isNotEmpty((Map)o);
        }
        if(o instanceof Object[]){
            return ArrayUtils.isNotEmpty((Object[])o);
        }
        return true;
    }
}
