package io.github.opensabe.spring.cloud.parent.common.eureka;

import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;

/**
 * 可以在项目中添加 EurekaInstanceConfigBeanCustomizer 的实现 Bean 来修改 EurekaInstanceConfigBean 注册信息
 */
public interface EurekaInstanceConfigBeanCustomizer {
    void customize(EurekaInstanceConfigBean eurekaInstanceConfigBean);
}
