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
package io.github.opensabe.common.executor.resilience4j;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.ThreadPoolBulkHeadDecorator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 因为spring-cloud-parent-common依赖是provided,如果像common-utils这样简单的项目依赖
 * spring-boot-stater-thread-pool，又不需要cloud环境，会报错，因此加上conditional
 * 这个{@link ConditionalOnClass} 只能放在类上才生效
 * @author hengma
 * @time 2023/10/19 17:39
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ThreadPoolBulkHeadDecorator.class)
public class BulkheadThreadPoolConfig {
    /**
     * @param threadPoolFactory
     * @return
     * @author hengma
     * @time 2023/10/19 17:25
     */
    @Bean
    public ThreadPoolBulkheadCustomizedDecorator getThreadPoolBulkheadCustomizedDecorator(ThreadPoolFactory threadPoolFactory) {
        return new ThreadPoolBulkheadCustomizedDecorator(threadPoolFactory);
    }
}
