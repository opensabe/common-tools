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
package io.github.opensabe.common.elasticsearch.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import co.elastic.clients.transport.Endpoint;
import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.http.TransportHttpClient;
import co.elastic.clients.transport.instrumentation.Instrumentation;
import io.github.opensabe.common.secret.FilterSecretStringResult;
import io.github.opensabe.common.secret.GlobalSecretManager;

/**
 * 基于 ES 传输层 {@link Instrumentation} 的敏感串过滤。
 * <p>
 * Rest5/HC5 异步客户端将 body 封装为 {@code AsyncEntityProducer}，经典
 * {@code HttpRequestInterceptor} 往往无法读取实体；本类在
 * {@link Context#beforeSendingHttpRequest} 收到已物化的请求体后再过滤。
 * </p>
 */
final class SecretFilteringInstrumentation implements Instrumentation {

    /**
     * 全局敏感串检测与告警管理器。
     */
    private final GlobalSecretManager globalSecretManager;

    /**
     * @param globalSecretManager 敏感串管理器
     */
    SecretFilteringInstrumentation(GlobalSecretManager globalSecretManager) {
        this.globalSecretManager = globalSecretManager;
    }

    /**
     * 为每个 ES API 调用创建过滤上下文。
     *
     * @param request  API 请求对象
     * @param endpoint 传输端点
     * @param <TRequest> 请求类型
     * @return 在 HTTP 发出前执行 body 过滤的 {@link Context}
     */
    @Override
    public <TRequest> Context newContext(TRequest request, Endpoint<TRequest, ?, ?> endpoint) {
        return new Context() {
            /** {@inheritDoc} */
            @Override
            public ThreadScope makeCurrent() {
                return () -> {
                };
            }

            /**
             * 拼接请求体 UTF-8 文本并检测敏感串；命中则抛出异常阻断请求。
             */
            @Override
            public void beforeSendingHttpRequest(TransportHttpClient.Request httpRequest, TransportOptions options) {
                Iterable<ByteBuffer> body = httpRequest.body();
                if (body == null) {
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (ByteBuffer buffer : body) {
                    if (buffer == null) {
                        continue;
                    }
                    ByteBuffer dup = buffer.duplicate();
                    byte[] bytes = new byte[dup.remaining()];
                    dup.get(bytes);
                    sb.append(new String(bytes, StandardCharsets.UTF_8));
                }
                if (sb.isEmpty()) {
                    return;
                }
                FilterSecretStringResult result = globalSecretManager.filterSecretStringAndAlarm(sb.toString());
                if (result.isFoundSensitiveString()) {
                    throw new RuntimeException("Sensitive string found in ES request");
                }
            }

            /** {@inheritDoc} */
            @Override
            public void afterReceivingHttpResponse(TransportHttpClient.Response response) {
            }

            /** {@inheritDoc} */
            @Override
            public <TResponse> void afterDecodingApiResponse(TResponse response) {
            }

            /**
             * 密钥命中发生在 HTTP 之前，不会污染 {@link ElasticSearchConfiguration} 的 observation 缓存；
             * 无响应的中途失败仍依赖该缓存 TTL 兜底 stop。
             */
            @Override
            public void recordException(Throwable throwable) {
            }

            /** {@inheritDoc} — 关闭资源。 */
            @Override
            public void close() {
            }
        };
    }
}
