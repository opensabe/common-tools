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
package io.github.opensabe.common.redisson.aop.ratelimiter;

import java.lang.reflect.Method;

import org.apache.commons.lang3.tuple.Pair;

import io.github.opensabe.common.redisson.annotation.RedissonRateLimiter;
import io.github.opensabe.common.redisson.aop.old.ExtraNamePointcut;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;

public class RedissonRateLimiterCachedPointcut extends ExtraNamePointcut<RedissonRateLimiterProperties> {


    public RedissonRateLimiterCachedPointcut(MethodArgumentsExpressEvaluator evaluator) {
        super(evaluator);
    }

    @Override
    @SuppressWarnings("removal")
    protected RedissonRateLimiterProperties computeRedissonProperties(Method method, Class<?> clazz) {
        RedissonRateLimiter l = method.getAnnotation(RedissonRateLimiter.class);
        if (l == null) {
            l = clazz.getAnnotation(RedissonRateLimiter.class);
        }
        if (l != null) {
            Pair<io.github.opensabe.common.redisson.annotation.RedissonRateLimiterName, Integer> pair =
                    findParameterAnnotation(method, io.github.opensabe.common.redisson.annotation.RedissonRateLimiterName.class);
            if (pair != null) {
                return new RedissonRateLimiterProperties(l, pair.getKey(), pair.getValue());
            } else {
                return new RedissonRateLimiterProperties(evaluator, l);
            }
        }

        return null;
    }
}
