package io.github.opensabe.spring.boot.starter.socketio.conf;

import io.github.opensabe.spring.cloud.parent.common.CommonConstant;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;

public class SocketIoEurekaMetadataModifier implements BeanPostProcessor {
    private final SocketIoServerProperties serverProperties;

    public SocketIoEurekaMetadataModifier(SocketIoServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof EurekaInstanceConfigBean) {
            EurekaInstanceConfigBean eurekaInstanceConfigBean = (EurekaInstanceConfigBean) bean;
            eurekaInstanceConfigBean.getMetadataMap().put(CommonConstant.SOCKET_IO_PATH, String.valueOf(serverProperties.getPort()));
        }
        return bean;
    }
}
