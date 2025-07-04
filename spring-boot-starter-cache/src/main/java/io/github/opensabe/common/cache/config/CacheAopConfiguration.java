package io.github.opensabe.common.cache.config;

import io.github.opensabe.common.cache.api.ExpireCacheInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.config.CacheManagementConfigUtils;
import org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;

/**
 * 为了避免默认的切面覆盖自定义的配置，
 * 项目中千万不要加 {@link org.springframework.cache.annotation.EnableCaching} 注解
 * @author heng.ma
 */
public class CacheAopConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public ExpireCachingConfigurer expireCachingConfigurer () {
        return new ExpireCachingConfigurer();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExpireCacheInterceptor expireCacheInterceptor (ExpireCachingConfigurer configurer,
                                                          CacheOperationSource cacheOperationSource) {
        ExpireCacheInterceptor interceptor = new ExpireCacheInterceptor(configurer.cacheResolver());
        interceptor.configure(configurer::errorHandler, configurer::keyGenerator, configurer::cacheResolver, configurer::cacheManager);
        interceptor.setCacheOperationSource(cacheOperationSource);
        return interceptor;
    }


    @Bean(name = CacheManagementConfigUtils.CACHE_ADVISOR_BEAN_NAME)
    public BeanFactoryCacheOperationSourceAdvisor cacheAdvisor(
            CacheOperationSource cacheOperationSource, ExpireCacheInterceptor cacheInterceptor,
            ObjectProvider<BeanFactoryTransactionAttributeSourceAdvisor> transactionAttributeSourceAdvisors
            ) {

        BeanFactoryCacheOperationSourceAdvisor advisor = new BeanFactoryCacheOperationSourceAdvisor();
        advisor.setCacheOperationSource(cacheOperationSource);
        advisor.setAdvice(cacheInterceptor);
        //要保证缓存的优先级高于事务
        transactionAttributeSourceAdvisors.ifUnique(ts -> advisor.setOrder(ts.getOrder()-1));
        return advisor;
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheOperationSource cacheOperationSource() {
        // Accept protected @Cacheable etc methods on CGLIB proxies, as of 6.0.
        return new AnnotationCacheOperationSource(false);
    }

}
