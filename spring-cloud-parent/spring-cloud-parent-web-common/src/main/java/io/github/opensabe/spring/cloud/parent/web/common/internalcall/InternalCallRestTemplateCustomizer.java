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

import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.web.client.RestTemplate;

public class InternalCallRestTemplateCustomizer implements RestTemplateCustomizer {

    private final InternalCallRestTemplateInterceptor interceptor;

    public InternalCallRestTemplateCustomizer(InternalCallRestTemplateInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        InternalCallClients.decorate(restTemplate, interceptor);
    }
}
