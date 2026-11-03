package io.github.opensabe.spring.cloud.parent.common.config;

import io.github.opensabe.spring.cloud.parent.common.system.MonitorMemoryRSS;
import io.github.opensabe.spring.cloud.parent.common.system.MonitorProcessList;
import io.github.opensabe.spring.cloud.parent.common.system.ProcessListMonitorProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProcessListMonitorProperties.class)
public class SystemConfiguration {
    @Bean
    public MonitorMemoryRSS getMonitorMemoryRSS() {
        return new MonitorMemoryRSS();
    }

    @Bean
    @ConditionalOnProperty(prefix = "opensabe.process-monitor", name = "enabled", havingValue = "true")
    public MonitorProcessList monitorProcessList(ProcessListMonitorProperties properties) {
        return new MonitorProcessList(properties);
    }
}
