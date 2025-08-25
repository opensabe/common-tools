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
package io.github.opensabe.spring.cloud.starter.third.client.webclient;

import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.opensabe.spring.cloud.starter.third.client.conf.ThirdPartyWebClientDefaultConfiguration;

public class ThirdPartyWebClientNamedContextFactory extends NamedContextFactory<ThirdPartyWebClientSpecification> {
    public static final String NAMESPACE = "third-party.webclient";
    public static final String PROPERTY_NAME = NAMESPACE + ".name";

    public ThirdPartyWebClientNamedContextFactory() {
        super(ThirdPartyWebClientDefaultConfiguration.class, NAMESPACE, PROPERTY_NAME);
    }

    /**
     * 获取 WebClient
     *
     * @param name
     * @return
     */
    public WebClient getWebClient(String name) {
        return getInstance(name, WebClient.class);
    }
}
