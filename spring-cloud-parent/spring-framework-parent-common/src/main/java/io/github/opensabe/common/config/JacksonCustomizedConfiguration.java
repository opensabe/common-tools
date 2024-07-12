package io.github.opensabe.common.config;


import com.fasterxml.jackson.databind.Module;
import io.github.opensabe.common.jackson.TimestampModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JacksonCustomizedConfiguration {

    /**
     * 最终还是将module创建到spring容器，因为spi无法保证顺序，jsr310会比我们自定义的后加载，
     * 因此会覆盖掉我们自己的LocalDateTime序列化
     *
     * @return
     */
    @Bean
    public Module timstampModule () {
        return new TimestampModule();
    }
}
