package org.springframework.cache.interceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class SimpleKeyGenerator implements KeyGenerator {
    public SimpleKeyGenerator() {
    }

    public Object generate(Object target, Method method, Object... params) {
        return generateKey(method, params);
    }

    public static Object generateKey(Method method, Object... params) {

        if (params.length == 0) {
            return method.getName();
        } else {
            if (params.length == 1) {
                Object param = params[0];
                if (param != null && !param.getClass().isArray()) {
                    return param;
                }
            }
            return Arrays.stream(params).map(Objects::toString).collect(Collectors.joining(":"));
        }
    }
}
