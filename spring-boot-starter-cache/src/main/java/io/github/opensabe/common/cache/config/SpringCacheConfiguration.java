package io.github.opensabe.common.cache.config;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.github.opensabe.common.cache.api.CaffeineExpireCacheManager;
import io.github.opensabe.common.cache.api.CompositeCacheManager;
import io.github.opensabe.common.cache.api.ExpireCacheInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * @author heng.ma
 */
public class SpringCacheConfiguration {


    @Bean
    @ConditionalOnBean(BeanFactoryCacheOperationSourceAdvisor.class)
    @ConditionalOnMissingBean
    public ExpireCacheInterceptor expireCacheInterceptor (ExpireCachingConfigurer configurer,
                                                          CacheOperationSource cacheOperationSource,
                                                          BeanFactoryCacheOperationSourceAdvisor advisor) {
        ExpireCacheInterceptor interceptor = new ExpireCacheInterceptor(configurer.cacheResolver());
        interceptor.configure(configurer::errorHandler, configurer::keyGenerator, configurer::cacheResolver, configurer::cacheManager);
        interceptor.setCacheOperationSource(cacheOperationSource);
        advisor.setAdvice(interceptor);
        return interceptor;
    }

    @Bean
    public CompositeCacheManager compositeCacheManager () {
        CaffeineExpireCacheManager cacheManager = new CaffeineExpireCacheManager();
        cacheManager.setCaffeineSpec(CaffeineSpec.parse("maximumSize=10000"));
        return new CompositeCacheManager(List.of(cacheManager));
    }

}
