package io.github.opensabe.spring.cloud.parent.common.eureka;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;

import java.util.List;

/**
 * 实现 EurekaInstanceConfigBeanCustomizer 在 EurekaInstanceConfigBean 初始化之后修改
 */
public class EurekaInstanceConfigBeanPostProcessor implements BeanPostProcessor {
    private final List<EurekaInstanceConfigBeanCustomizer> eurekaInstanceConfigBeanCustomizers;

    public EurekaInstanceConfigBeanPostProcessor(List<EurekaInstanceConfigBeanCustomizer> eurekaInstanceConfigBeanCustomizers) {
        this.eurekaInstanceConfigBeanCustomizers = eurekaInstanceConfigBeanCustomizers;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof EurekaInstanceConfigBean) {
            EurekaInstanceConfigBean eurekaInstanceConfigBean = (EurekaInstanceConfigBean) bean;
            eurekaInstanceConfigBeanCustomizers.forEach(eurekaInstanceConfigBeanCustomizer -> {
                eurekaInstanceConfigBeanCustomizer.customize(eurekaInstanceConfigBean);
            });
        }
        return bean;
    }
}
