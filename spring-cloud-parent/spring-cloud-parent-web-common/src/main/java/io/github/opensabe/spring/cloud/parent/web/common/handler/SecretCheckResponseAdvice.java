package io.github.opensabe.spring.cloud.parent.web.common.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.opensabe.common.secret.FilterSecretStringResult;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.cloud.parent.web.common.undertow.HttpServletResponseImplUtil;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import io.undertow.util.HeaderMap;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.Duration;
import java.util.Collection;

@Log4j2
@ControllerAdvice
public class SecretCheckResponseAdvice implements ResponseBodyAdvice<Object> {
    private final GlobalSecretManager globalSecretManager;
    private final Cache<String, Boolean> cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(1)).build();

    public SecretCheckResponseAdvice(GlobalSecretManager globalSecretManager) {
        this.globalSecretManager = globalSecretManager;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        String path = request.getURI().getPath();
        Boolean ifPresent = cache.getIfPresent(path);
        if (ifPresent != null && !ifPresent) {
            log.debug("Path: {} cached skip current beforeBodyWrite", path);
            return body;
        } else {
            log.debug("Path: {} is not cached", path);
        }
        if (response instanceof ServletServerHttpResponse servletServerHttpResponse) {
            HttpServletResponse servletResponse = servletServerHttpResponse.getServletResponse();
            Collection<String> headerNames = servletResponse.getHeaderNames();
            for (String headerName : headerNames) {
                String header = servletResponse.getHeader(headerName);
                if (header != null) {
                    FilterSecretStringResult headerNameFilterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(headerName);
                    FilterSecretStringResult headerFilterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(header);
                    if (headerNameFilterSecretStringResult.isFoundSensitiveString() || headerFilterSecretStringResult.isFoundSensitiveString()) {
                        cache.put(path, true);
                        if (servletResponse instanceof HttpServletResponseImpl httpServletResponse) {
                            HttpServerExchange httpServerExchange = HttpServletResponseImplUtil.getExchange(httpServletResponse);
                            HeaderMap responseHeaders = httpServerExchange.getResponseHeaders();
                            responseHeaders.clear();
                        } else {
                            log.error("servletResponse is not HttpServletResponseImpl, servletResponse: {}", servletResponse);
                        }
                    }
                }
            }
        } else {
            log.error("response is not ServletServerHttpResponse, response: {}", response);
        }
        String s = JsonUtil.toJSONString(body);
        FilterSecretStringResult filterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(s);
        cache.put(path, filterSecretStringResult.isFoundSensitiveString());
        if (filterSecretStringResult.isFoundSensitiveString()) {
            return JsonUtil.parseObject(filterSecretStringResult.getFilteredContent(), body.getClass());
        }
        return body;
    }
}
