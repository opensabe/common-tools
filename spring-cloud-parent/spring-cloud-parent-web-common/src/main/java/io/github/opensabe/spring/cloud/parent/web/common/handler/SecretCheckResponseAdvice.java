/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.spring.cloud.parent.web.common.handler;

import java.time.Duration;
import java.util.Set;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.DelegatingServerHttpResponse;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.opensabe.common.secret.FilterSecretStringResult;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletResponseWrapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

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
        if (Boolean.FALSE.equals(ifPresent)) {
            log.debug("Path: {} cached skip current beforeBodyWrite", path);
            return body;
        } else {
            log.debug("Path: {} is not cached", path);
        }
        //2025年03月18日11:02:50,兼容 response Delegate
        ServerHttpResponse r = response;
        while (r instanceof DelegatingServerHttpResponse delegate) {
            r = delegate.getDelegate();
        }
        if (r instanceof ServletServerHttpResponse servletServerHttpResponse) {
            ServletResponse servletResponse = servletServerHttpResponse.getServletResponse();
            while (servletResponse instanceof ServletResponseWrapper wrapper) {
                servletResponse = wrapper.getResponse();
            }
            if (servletResponse instanceof HttpServletResponseImpl httpServletResponse) {
                HttpServerExchange httpServerExchange = httpServletResponse.getExchange();
                HeaderMap responseHeaders = httpServerExchange.getResponseHeaders();
                //先复制，防止遍历移除的时候抛出 ConcurrentModificationException
                Set<HttpString> headerNames = Set.copyOf(responseHeaders.getHeaderNames());
                for (HttpString headerName : headerNames) {
                    FilterSecretStringResult headerNameFilterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(headerName.toString());
                    //如果 headerName 有敏感信息，则移除整个 header
                    if (headerNameFilterSecretStringResult.isFoundSensitiveString()) {
                        cache.put(path, true);
                        responseHeaders.remove(headerName);
                        continue;
                    }
                    //如果 headerName 没有敏感信息，则继续检查 headerValue
                    HeaderValues headerValues = responseHeaders.get(headerName);
                    for (String headerValue : headerValues) {
                        FilterSecretStringResult headerFilterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(headerValue);
                        if (headerFilterSecretStringResult.isFoundSensitiveString()) {
                            cache.put(path, true);
                            //如果 value 中有敏感信息，则移除整个 header，填入掩码，这里忽略了多个值的情况
                            responseHeaders.remove(headerName);
                            responseHeaders.add(headerName, headerFilterSecretStringResult.getFilteredContent());
                            break;
                        }
                    }
                }
            } else {
                log.error("wrapperResponse is not HttpServletResponseImpl, response: {}", servletResponse);
            }
        } else {
            log.error("response is not ServletServerHttpResponse, response: {}", response);
        }
        String s = JsonUtil.toJSONString(body);
        FilterSecretStringResult filterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(s);
        //为了防止body中的值将Header覆盖掉
        cache.get(path, k -> filterSecretStringResult.isFoundSensitiveString());
        if (filterSecretStringResult.isFoundSensitiveString()) {
            return JsonUtil.parseObject(filterSecretStringResult.getFilteredContent(), body.getClass());
        }
        return body;
    }
}
