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
package io.github.opensabe.spring.cloud.parent.web.common.config;

import java.util.List;

import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import io.github.opensabe.common.http.HttpPathSecurityProperties;
import io.github.opensabe.spring.cloud.parent.common.web.ServletPathResolver;

public class WebMvcConfig implements WebMvcConfigurer {

    private final HttpPathSecurityProperties httpPathSecurityProperties;

    public WebMvcConfig(HttpPathSecurityProperties httpPathSecurityProperties) {
        this.httpPathSecurityProperties = httpPathSecurityProperties;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ServletPathResolver());
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        if (httpPathSecurityProperties == null || !httpPathSecurityProperties.isKeepSemicolonInPath()) {
            return;
        }
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setRemoveSemicolonContent(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }
}
