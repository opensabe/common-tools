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
 * Rest5/HC5 async clients pass the body as {@code AsyncEntityProducer}, so classic
 * {@code HttpRequestInterceptor} often cannot see the entity. Filter via transport
 * instrumentation which receives the materialized request body.
 */
final class SecretFilteringInstrumentation implements Instrumentation {

    private final GlobalSecretManager globalSecretManager;

    SecretFilteringInstrumentation(GlobalSecretManager globalSecretManager) {
        this.globalSecretManager = globalSecretManager;
    }

    @Override
    public <TRequest> Context newContext(TRequest request, Endpoint<TRequest, ?, ?> endpoint) {
        return new Context() {
            @Override
            public ThreadScope makeCurrent() {
                return () -> {
                };
            }

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

            @Override
            public void afterReceivingHttpResponse(TransportHttpClient.Response response) {
            }

            @Override
            public <TResponse> void afterDecodingApiResponse(TResponse response) {
            }

            @Override
            public void recordException(Throwable throwable) {
                // HC5 observation is started in a request interceptor only after HTTP begins.
                // This filter runs in beforeSendingHttpRequest (before HTTP), so a secret hit
                // never leaves an in-flight Observation in ElasticSearchConfiguration.CACHE.
                // Mid-flight HTTP failures without a response still rely on that CACHE TTL.
            }

            @Override
            public void close() {
            }
        };
    }
}
