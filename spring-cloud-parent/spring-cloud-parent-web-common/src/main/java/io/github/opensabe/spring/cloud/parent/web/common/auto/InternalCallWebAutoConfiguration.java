package io.github.opensabe.spring.cloud.parent.web.common.auto;

import io.github.opensabe.common.internalcall.CallerIdentityProvider;
import io.github.opensabe.common.internalcall.InternalCallFailureHandler;
import io.github.opensabe.common.internalcall.InternalCallPathMatcher;
import io.github.opensabe.common.internalcall.InternalCallProperties;
import io.github.opensabe.common.internalcall.InternalCallSigner;
import io.github.opensabe.common.internalcall.InternalCallVerifier;
import io.github.opensabe.common.internalcall.InternalHostMatcher;
import io.github.opensabe.spring.cloud.parent.web.common.internalcall.InternalCallFeignSupport;
import io.github.opensabe.spring.cloud.parent.web.common.internalcall.InternalCallInboundFilter;
import io.github.opensabe.spring.cloud.parent.web.common.internalcall.InternalCallRestTemplateCustomizer;
import io.github.opensabe.spring.cloud.parent.web.common.internalcall.InternalCallRestTemplateInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
@ConditionalOnClass(name = "jakarta.servlet.Filter")
public class InternalCallWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InternalCallFeignSupport internalCallFeignSupport(
            InternalCallProperties properties,
            InternalCallSigner signer,
            CallerIdentityProvider callerIdentityProvider,
            InternalHostMatcher hostMatcher
    ) {
        return new InternalCallFeignSupport(properties, signer, callerIdentityProvider, hostMatcher);
    }

    @Bean
    @ConditionalOnMissingBean
    public InternalCallRestTemplateInterceptor internalCallRestTemplateInterceptor(
            InternalCallProperties properties,
            InternalCallSigner signer,
            CallerIdentityProvider callerIdentityProvider,
            InternalHostMatcher hostMatcher
    ) {
        return new InternalCallRestTemplateInterceptor(properties, signer, callerIdentityProvider, hostMatcher);
    }

    @Bean
    @ConditionalOnMissingBean
    public InternalCallRestTemplateCustomizer internalCallRestTemplateCustomizer(
            InternalCallRestTemplateInterceptor interceptor
    ) {
        return new InternalCallRestTemplateCustomizer(interceptor);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public FilterRegistrationBean<InternalCallInboundFilter> internalCallInboundFilter(
            InternalCallProperties properties,
            InternalCallVerifier verifier,
            InternalCallFailureHandler failureHandler,
            InternalCallPathMatcher pathMatcher
    ) {
        InternalCallInboundFilter filter = new InternalCallInboundFilter(
                properties, verifier, failureHandler, pathMatcher
        );
        FilterRegistrationBean<InternalCallInboundFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 50);
        return bean;
    }
}
