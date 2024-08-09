package io.github.opensabe.common.web.config.interceptor;

import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Log4j2
@RestControllerAdvice
public class ActuatorAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        String name = returnType.getMethod().toString().toLowerCase();
        return name.contains("actuate.endpoint");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        String name = returnType.getMethod().toString().toLowerCase();
        log.info("name: {}, return: {}", name, body instanceof String
                ? body : JsonUtil.toJSONString(body));
        return body;
    }
}
