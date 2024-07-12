package io.github.opensabe.spring.cloud.parent.gateway.config;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.event.EnableBodyCachingEvent;
import org.springframework.cloud.gateway.filter.AdaptCachedBodyGlobalFilter;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GatewayJFRProperties.class)
public class CommonGatewayConfiguration {

    @Autowired
    private AdaptCachedBodyGlobalFilter adaptCachedBodyGlobalFilter;
    @Autowired
    private GatewayProperties gatewayProperties;

    /**
     * 针对每个路径都自动启用针对 RequestBody 的缓存
     * 因为每个路径都可能重试，只要有重试则必须将 RequestBody 缓存起来，因为 FluxRead 是一次性的
     */
    @PostConstruct
    public void init() {

        gatewayProperties.getRoutes().forEach(routeDefinition -> {
            EnableBodyCachingEvent enableBodyCachingEvent = new EnableBodyCachingEvent(new Object(), routeDefinition.getId());
            adaptCachedBodyGlobalFilter.onApplicationEvent(enableBodyCachingEvent);
        });
    }
}
