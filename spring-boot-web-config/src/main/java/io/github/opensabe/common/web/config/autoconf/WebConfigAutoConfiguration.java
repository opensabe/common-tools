package io.github.opensabe.common.web.config.autoconf;

import io.github.opensabe.common.web.config.ConverterConfiguration;
import io.github.opensabe.common.web.config.interceptor.ActuatorAdvice;
import io.github.opensabe.common.web.config.interceptor.CommonAop;
import io.github.opensabe.common.web.config.interceptor.ResponseAdvice;
import io.github.opensabe.common.web.config.interceptor.ValidatorAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

//@Configuration(proxyBeanMethods = false)
@AutoConfiguration
@Import({
        ConverterConfiguration.class,
        ActuatorAdvice.class,
        CommonAop.class,
        ResponseAdvice.class,
        ValidatorAdvice.class,
})
public class WebConfigAutoConfiguration {
}
