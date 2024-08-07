package io.github.opensabe.spring.cloud.parent.gateway.config;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@ConfigurationProperties("spring.cloud.gateway.log")
public class GatewayLogProperties {
    /**
     * 慢调用界限配置
     */
    private List<SlowLog> slowLogs;

    /**
     * 参数检查
     */
    private List<ParamCheck> paramChecks;

    @Data
    @NoArgsConstructor
    public static class SlowLog {
        private String pattern;
        private Long threshold;
    }

    @Data
    @NoArgsConstructor
    public static class ParamCheck {
        //模式匹配过滤 uri
        private String pattern;
        //非法参数检查的参数 key 列表
        private List<String> params;
        //非法参数值列表
        private List<String> invalidParamValues;
        //如果匹配，但是不匹配 specificOperations 列表，执行的默认的操作
        private Operation defaultOperation;
        //如果匹配，并且符合 platform 以及 version，执行的操作，
        private List<SpecificOperation> specificOperations = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class SpecificOperation {
        private String platform;
        private Set<String> versions;
        private Operation operation;
    }

    public enum Operation {
        /**
         * 直接忽略
         */
        IGNORE,
        /**
         * 报警
         */
        ALARM,
        /**
         * 直接返回参数非法，不报警
         */
        INVALID
        ;
    }
}
