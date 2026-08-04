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
 * 线程安全的 OpenFeign HTTP 消息转换器。
 * <p>
 * OpenFeign 5.x 中 {@link FeignHttpMessageConverters#getConverters()} 在填充转换器列表前
 * 会无同步地发布空列表，并发首次解码可能触发
 * {@code 'messageConverters' must not be empty}。本类通过双重检查锁定保证初始化只执行一次，
 * 并将结果快照为不可变列表。
 */
public final class ThreadSafeFeignHttpMessageConverters extends FeignHttpMessageConverters {

    /**
     * 已初始化的转换器列表快照；{@code volatile} 保证可见性。
     */
    private volatile List<HttpMessageConverter<?>> snapshot;

    /**
     * 使用 Boot 与 OpenFeign 提供的定制器构建父类转换器。
     *
     * @param customizers      Boot HTTP 消息转换器定制器
     * @param cloudCustomizers OpenFeign 消息转换器定制器
     */
    public ThreadSafeFeignHttpMessageConverters(
            ObjectProvider<ClientHttpMessageConvertersCustomizer> customizers,
            ObjectProvider<HttpMessageConverterCustomizer> cloudCustomizers) {
        super(customizers, cloudCustomizers);
    }

    /**
     * 返回线程安全、不可变的 HTTP 消息转换器列表。
     *
     * @return 消息转换器列表快照
     */
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
