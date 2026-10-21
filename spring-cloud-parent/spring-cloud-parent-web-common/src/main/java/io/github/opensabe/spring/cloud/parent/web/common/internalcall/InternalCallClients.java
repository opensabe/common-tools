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

package io.github.opensabe.spring.cloud.parent.web.common.internalcall;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Manual RestTemplate decoration for {@code new RestTemplate()} instances.
 * Idempotent: adds at most one {@link InternalCallRestTemplateInterceptor}.
 */
public final class InternalCallClients {

    private InternalCallClients() {
    }

    public static RestTemplate decorate(RestTemplate restTemplate, InternalCallRestTemplateInterceptor interceptor) {
        if (restTemplate == null || interceptor == null) {
            return restTemplate;
        }
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        for (ClientHttpRequestInterceptor existing : interceptors) {
            if (existing instanceof InternalCallRestTemplateInterceptor) {
                return restTemplate;
            }
        }
        interceptors.add(interceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}
