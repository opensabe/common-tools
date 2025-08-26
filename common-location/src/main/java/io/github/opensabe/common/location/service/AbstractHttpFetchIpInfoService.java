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
package io.github.opensabe.common.location.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.github.opensabe.common.location.vo.IpLocation;

public abstract class AbstractHttpFetchIpInfoService<T> {
    protected final RestTemplate restTemplate = new RestTemplate();

    protected abstract Class<T> clazz();

    protected abstract String url(String ip);

    protected abstract HttpHeaders httpHeaders();

    protected abstract HttpMethod httpMethod();

    protected abstract IpLocation transfer(T t);

    public IpLocation getIpLocation(String ip) {
        String url = url(ip);
        HttpHeaders httpHeaders = httpHeaders();
        HttpMethod httpMethod = httpMethod();
        HttpEntity<Void> httpEntity = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<T> exchange = restTemplate.exchange(url, httpMethod, httpEntity, clazz());
        return transfer(exchange.getBody());
    }
}
