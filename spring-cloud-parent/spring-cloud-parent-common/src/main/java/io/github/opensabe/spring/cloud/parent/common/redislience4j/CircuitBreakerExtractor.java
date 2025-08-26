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
package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import org.springframework.cloud.client.loadbalancer.RequestDataContext;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

public interface CircuitBreakerExtractor<T> {
    /**
     * 通过负载均衡请求，以及实例信息，获取对应的 CircuitBreaker
     *
     * @param circuitBreakerRegistry
     * @param requestDataContext
     * @param host
     * @param port
     * @return
     */
    CircuitBreaker getCircuitBreaker(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RequestDataContext requestDataContext,
            String host,
            int port
    );
}

