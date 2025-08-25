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

import io.github.opensabe.common.redisson.annotation.RedissonRateLimiter;
import io.github.opensabe.common.redisson.annotation.RedissonRateLimiterName;
import io.github.opensabe.common.redisson.aop.old.ExtraNameProperties;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import lombok.Getter;

public class RedissonRateLimiterProperties extends ExtraNameProperties {
    @Getter
    private final RedissonRateLimiter redissonRateLimiter;

    public RedissonRateLimiterProperties(RedissonRateLimiter redissonRateLimiter, RedissonRateLimiterName redissonRateLimiterName, int parameterIndex) {
        super(redissonRateLimiterName.prefix(), redissonRateLimiter.name(), parameterIndex, redissonRateLimiterName.expression());
        this.redissonRateLimiter = redissonRateLimiter;
    }
    public RedissonRateLimiterProperties(MethodArgumentsExpressEvaluator evaluator, RedissonRateLimiter redissonRateLimiter) {
        super(evaluator, redissonRateLimiter.prefix(), redissonRateLimiter.name());
        this.redissonRateLimiter = redissonRateLimiter;
    }

}
