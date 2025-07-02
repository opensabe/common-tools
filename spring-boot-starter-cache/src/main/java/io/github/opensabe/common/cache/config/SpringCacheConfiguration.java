package io.github.opensabe.common.cache.config;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.github.opensabe.common.cache.api.CaffeineExpireCacheManager;
import io.github.opensabe.common.cache.api.CompositeCacheManager;
import io.github.opensabe.common.cache.api.ExpireCacheInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.config.CacheManagementConfigUtils;
import org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;

import java.util.List;

/**
 * @author heng.ma
 */
public class SpringCacheConfiguration {



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
        transactionAttributeSourceAdvisors.ifUnique(ts -> advisor.setOrder(ts.getOrder()-1));
        return advisor;
    }

    @Bean
    public CacheOperationSource cacheOperationSource() {
        // Accept protected @Cacheable etc methods on CGLIB proxies, as of 6.0.
        return new AnnotationCacheOperationSource(false);
    }

    @Bean
    public CompositeCacheManager compositeCacheManager () {
        CaffeineExpireCacheManager cacheManager = new CaffeineExpireCacheManager();
        cacheManager.setCaffeineSpec(CaffeineSpec.parse("maximumSize=10000"));
        return new CompositeCacheManager(List.of(cacheManager));
    }
}
