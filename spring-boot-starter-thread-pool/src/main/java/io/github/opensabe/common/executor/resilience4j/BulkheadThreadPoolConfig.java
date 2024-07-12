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
