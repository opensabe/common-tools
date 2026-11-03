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
package io.github.opensabe.spring.cloud.parent.web.common.auto;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import io.github.opensabe.spring.cloud.parent.web.common.filter.RejectSemicolonServletFilter;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RejectSemicolonWebAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "opensabe.http.reject-semicolon",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public FilterRegistrationBean<RejectSemicolonServletFilter> rejectSemicolonServletFilter() {
        FilterRegistrationBean<RejectSemicolonServletFilter> bean =
                new FilterRegistrationBean<>(new RejectSemicolonServletFilter());
        bean.setOrder(RejectSemicolonServletFilter.ORDER);
        return bean;
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "opensabe.http",
            name = "keep-semicolon-in-path",
            havingValue = "true",
            matchIfMissing = true
    )
    public WebMvcConfigurer keepSemicolonInPathWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void configurePathMatch(PathMatchConfigurer configurer) {
                UrlPathHelper urlPathHelper = new UrlPathHelper();
                urlPathHelper.setRemoveSemicolonContent(false);
                configurer.setUrlPathHelper(urlPathHelper);
            }
        };
    }
}
