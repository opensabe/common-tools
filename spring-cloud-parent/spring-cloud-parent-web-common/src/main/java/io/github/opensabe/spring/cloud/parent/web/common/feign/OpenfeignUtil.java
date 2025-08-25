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
package io.github.opensabe.spring.cloud.parent.web.common.feign;

import feign.Request;
import feign.Response;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.cloud.openfeign.FeignClientFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Supplier;

import static feign.FeignException.errorStatus;

public class OpenfeignUtil {
    /**
     * 判断一个 OpenFeign 的请求是否是可以重试类型的请求
     * 根据方法是否为 GET，以及方法和方法所在类上面是否有 RetryableMethod 注解来判定
     * @param request
     * @return
     */
    public static boolean isRetryableRequest(Request request) {
        Request.HttpMethod httpMethod = request.httpMethod();
        if (Objects.equals(httpMethod, Request.HttpMethod.GET)) {
            return true;
        }
        Method method = request.requestTemplate().methodMetadata().method();
        RetryableMethod annotation = method.getAnnotation(RetryableMethod.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(RetryableMethod.class);
        }
        //如果类上面或者方法上面有注解，则为查询类型的请求，是可以重试的
        return annotation != null;
    }

    /**
     * 针对 OpenFeign 的 circuitBreaker 封装，根据响应进行断路
     * @param circuitBreaker
     * @param supplier
     * @return
     */
    public static Supplier<Response> decorateSupplier(CircuitBreaker circuitBreaker, Supplier<Response> supplier) {
        return () -> {
            circuitBreaker.acquirePermission();
            long start = circuitBreaker.getCurrentTimestamp();

            long duration;
            try {
                Response result = supplier.get();
                HttpStatus httpStatus = HttpStatus.valueOf(result.status());
                duration = circuitBreaker.getCurrentTimestamp() - start;
                //这里的修改只是为了断路器针对 500 响应正常断路，因为这里还没走到 Feign 的 ErrorDecoder，所以无法抛出异常
                if (httpStatus.is2xxSuccessful()) {
                    circuitBreaker.onResult(duration, circuitBreaker.getTimestampUnit(), result);
                } else {
                    circuitBreaker.onError(duration, circuitBreaker.getTimestampUnit(), errorStatus("not useful", result));
                }
                return result;
            } catch (Exception var7) {
                duration = circuitBreaker.getCurrentTimestamp() - start;
                circuitBreaker.onError(duration, circuitBreaker.getTimestampUnit(), var7);
                throw var7;
            }
        };
    }

    private static final String CLIENT_NAME_PROPERTY_KEY;

    //这里是为了获取 FeignClientFactory 中的 propertyName 字段用于获取 clientName
    static {
        try {
            VarHandle propertyName = MethodHandles
                    .privateLookupIn(NamedContextFactory.class, MethodHandles.lookup())
                    .findVarHandle(NamedContextFactory.class, "propertyName",
                            String.class);
            FeignClientFactory feignClientFactory = new FeignClientFactory();
            Object o = propertyName.get(feignClientFactory);
            CLIENT_NAME_PROPERTY_KEY = o.toString();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取 OpenFeign 的 clientName
     * @param environment
     * @return
     */
    public static String getClientNamePropertyKey(Environment environment) {
        return environment.getProperty(CLIENT_NAME_PROPERTY_KEY);
    }
}
