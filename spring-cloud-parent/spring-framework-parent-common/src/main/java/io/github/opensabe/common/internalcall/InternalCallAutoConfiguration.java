package io.github.opensabe.common.internalcall;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.time.Clock;

@AutoConfiguration
@EnableConfigurationProperties(InternalCallProperties.class)
public class InternalCallAutoConfiguration {

    @Bean
    public Ipv4CidrSet internalCallTrustedCidrSet(InternalCallProperties properties) {
        // Fail-fast even when enabled=false so misconfig is visible before rollout.
        return Ipv4CidrSet.parse(properties.getEdge() == null ? null : properties.getEdge().getTrustedCidrs());
    }

    @Bean
    public InternalCallSigner internalCallSigner(InternalCallProperties properties) {
        return new InternalCallSigner(properties, Clock.systemUTC());
    }

    @Bean
    public InternalCallVerifier internalCallVerifier(
            InternalCallProperties properties,
            InternalCallSigner internalCallSigner
    ) {
        return new InternalCallVerifier(properties, internalCallSigner, Clock.systemUTC());
    }

    @Bean
    public InternalCallFailureHandler internalCallFailureHandler(InternalCallProperties properties) {
        return new InternalCallFailureHandler(properties);
    }

    @Bean
    public CallerIdentityProvider callerIdentityProvider(Environment environment) {
        return new CallerIdentityProvider(environment);
    }

    @Bean
    public InternalHostMatcher internalHostMatcher(InternalCallProperties properties) {
        return new InternalHostMatcher(
                properties.getOutbound() == null ? null : properties.getOutbound().getInternalHostPatterns()
        );
    }

    @Bean
    public InternalCallPathMatcher internalCallPathMatcher(InternalCallProperties properties) {
        return new InternalCallPathMatcher(properties.getExcludePathPatterns());
    }
}
