/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.spring.cloud.parent.common.validation;


import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotBlank;

;

public class ObjectBlankValidator implements ConstraintValidator<NotBlank, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return isNotEmpty(value);
    }

    private boolean isNotEmpty(Object o) {
        if (o == null) return false;
        if (o instanceof String) {
            return StringUtils.isNotBlank((String) o);
        }
        if (o instanceof Collection) {
            return CollectionUtils.isNotEmpty((Collection) o);
        }
        if (o instanceof Map) {
            return MapUtils.isNotEmpty((Map) o);
        }
        if (o instanceof Object[]) {
            return ArrayUtils.isNotEmpty((Object[]) o);
        }
        return true;
    }
}
