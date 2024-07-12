package io.github.opensabe.spring.cloud.starter.third.client.conf;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "third-party.webclient")
public class ThirdPartyWebClientConfigurationProperties {
    private Map<String, WebClientProperties> configs;
    @Data
    @NoArgsConstructor
    public static class WebClientProperties {
        /**
         * 服务地址，必须填写
         */
        private String baseUrl;
        /**
         * 服务名称，不填写就是 configs 这个 map 的 key，目前没啥用
         */
        private String serviceName;
        /**
         * 连接超时
         */
        private Duration connectTimeout = Duration.ofSeconds(3);
        /**
         * 响应超时
         */
        private Duration responseTimeout = Duration.ofSeconds(15);
    }
}
