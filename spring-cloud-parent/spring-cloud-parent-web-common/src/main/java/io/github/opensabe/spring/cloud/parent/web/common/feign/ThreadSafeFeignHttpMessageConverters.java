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
package io.github.opensabe.spring.cloud.parent.web.common.feign;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * OpenFeign 5.x {@link FeignHttpMessageConverters#getConverters()} publishes an empty list
 * before filling it without synchronization. Concurrent first decode then fails with
 * {@code 'messageConverters' must not be empty}. Gate initialization so only one thread runs it.
 */
public final class ThreadSafeFeignHttpMessageConverters extends FeignHttpMessageConverters {

    private volatile List<HttpMessageConverter<?>> snapshot;

    public ThreadSafeFeignHttpMessageConverters(
            ObjectProvider<ClientHttpMessageConvertersCustomizer> customizers,
            ObjectProvider<HttpMessageConverterCustomizer> cloudCustomizers) {
        super(customizers, cloudCustomizers);
    }

    @Override
    public List<HttpMessageConverter<?>> getConverters() {
        List<HttpMessageConverter<?>> local = snapshot;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (snapshot == null) {
                snapshot = List.copyOf(super.getConverters());
            }
            return snapshot;
        }
    }
}
