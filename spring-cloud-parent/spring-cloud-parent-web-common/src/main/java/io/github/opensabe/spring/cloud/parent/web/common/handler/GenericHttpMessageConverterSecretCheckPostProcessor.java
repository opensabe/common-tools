package io.github.opensabe.spring.cloud.parent.web.common.handler;

import io.github.opensabe.common.secret.FilterSecretStringResult;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.utils.json.JsonUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class GenericHttpMessageConverterSecretCheckPostProcessor implements BeanPostProcessor {
    private final GlobalSecretManager globalSecretManager;

    public GenericHttpMessageConverterSecretCheckPostProcessor(GlobalSecretManager globalSecretManager) {
        this.globalSecretManager = globalSecretManager;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof GenericHttpMessageConverter) {
            return new GenericConverterSecretCheck((GenericHttpMessageConverter) bean, globalSecretManager);
        }
        return bean;
    }

    private static class GenericConverterSecretCheck implements GenericHttpMessageConverter {
        private final GenericHttpMessageConverter delegate;
        private final GlobalSecretManager globalSecretManager;

        public GenericConverterSecretCheck(GenericHttpMessageConverter delegate, GlobalSecretManager globalSecretManager) {
            this.delegate = delegate;
            this.globalSecretManager = globalSecretManager;
        }

        @Override
        public boolean canRead(Type type, Class contextClass, MediaType mediaType) {
            return delegate.canRead(type, contextClass, mediaType);
        }

        @Override
        public Object read(Type type, Class contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
            return delegate.read(type, contextClass, inputMessage);
        }

        @Override
        public boolean canWrite(Type type, Class clazz, MediaType mediaType) {
            return delegate.canWrite(type, clazz, mediaType);
        }

        @Override
        public void write(Object o, Type type, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
            checkSecret(o, contentType, outputMessage);
            delegate.write(o, type, contentType, outputMessage);
        }

        @Override
        public boolean canRead(Class clazz, MediaType mediaType) {
            return delegate.canRead(clazz, mediaType);
        }

        @Override
        public boolean canWrite(Class clazz, MediaType mediaType) {
            return delegate.canWrite(clazz, mediaType);
        }

        @Override
        public List<MediaType> getSupportedMediaTypes() {
            return delegate.getSupportedMediaTypes();
        }

        @Override
        public Object read(Class clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
            return delegate.read(clazz, inputMessage);
        }

        @Override
        public void write(Object o, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
            checkSecret(o, contentType, outputMessage);
            delegate.write(o, contentType, outputMessage);
        }

        private void checkSecret(Object o, MediaType contentType, HttpOutputMessage outputMessage) throws IOException {
            if (outputMessage instanceof ServletServerHttpResponse) {
                Collection<String> headerNames = ((ServletServerHttpResponse) outputMessage).getServletResponse().getHeaderNames();
                for (String headerName : headerNames) {
                    String header = ((ServletServerHttpResponse) outputMessage).getServletResponse().getHeader(headerName);
                    if (header != null) {
                        FilterSecretStringResult headerNameFilterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(headerName);
                        FilterSecretStringResult headerFilterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(header);
                        if (headerNameFilterSecretStringResult.isFoundSensitiveString() || headerFilterSecretStringResult.isFoundSensitiveString()) {
                            ((ServletServerHttpResponse) outputMessage).getServletResponse().sendError(HttpServletResponse.SC_FORBIDDEN, "Sensitive api forbidden");
                        }
                    }
                }

                if (contentType != null) {
                    String contentTypeString = contentType.toString();
                    if (
                            StringUtils.containsIgnoreCase(contentTypeString, "json")
                            || StringUtils.containsIgnoreCase(contentTypeString, "xml")
                            || StringUtils.containsIgnoreCase(contentTypeString, "text")
                            || StringUtils.containsIgnoreCase(contentTypeString, "html")
                            || StringUtils.containsIgnoreCase(contentTypeString, "form")
                            || StringUtils.containsIgnoreCase(contentTypeString, "urlencoded")
                    ) {
                        String jsonString = JsonUtil.toJSONString(o);
                        FilterSecretStringResult filterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(jsonString);
                        if (filterSecretStringResult.isFoundSensitiveString()) {
                            ((ServletServerHttpResponse) outputMessage).getServletResponse().sendError(HttpServletResponse.SC_FORBIDDEN, "Sensitive api forbidden");
                        }
                    }
                }
            }
        }
    }
}
