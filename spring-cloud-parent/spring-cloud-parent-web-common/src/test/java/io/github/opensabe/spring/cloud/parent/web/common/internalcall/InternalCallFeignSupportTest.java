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

import feign.Request;
import feign.RequestTemplate;
import io.github.opensabe.common.internalcall.CallerIdentityProvider;
import io.github.opensabe.common.internalcall.InternalCallProperties;
import io.github.opensabe.common.internalcall.InternalCallSigner;
import io.github.opensabe.common.internalcall.InternalHostMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalCallFeignSupportTest {

    @Test
    @DisplayName("enabled=false 时返回原 Request")
    void disabledReturnsSame() {
        InternalCallProperties props = new InternalCallProperties();
        props.setEnabled(false);
        InternalCallFeignSupport support = support(props);
        RequestTemplate template = new RequestTemplate();
        Request request = Request.create(Request.HttpMethod.GET, "http://order-service/api",
                Collections.emptyMap(), null, StandardCharsets.UTF_8, template);
        assertSame(request, support.prepare(request));
    }

    @Test
    @DisplayName("开启后模板头不追加，Request 上为单值 sign")
    void signsWithoutMutatingTemplate() {
        InternalCallProperties props = new InternalCallProperties();
        props.setEnabled(true);
        props.setSharedSecret("secret");
        InternalCallFeignSupport support = support(props);

        RequestTemplate template = new RequestTemplate();
        Map<String, Collection<String>> headers = new LinkedHashMap<>();
        headers.put(props.getHeaders().getSign(), List.of("forged"));
        Request request = Request.create(Request.HttpMethod.GET, "http://10.0.1.5/api",
                headers, null, StandardCharsets.UTF_8, template);

        Request prepared = support.prepare(request);
        assertNotNull(prepared.headers().get(props.getHeaders().getSign()));
        assertEquals(1, prepared.headers().get(props.getHeaders().getSign()).size());
        assertTrue(template.headers().isEmpty() || !template.headers().containsKey(props.getHeaders().getSign()));
        assertNull(template.headers().get(props.getHeaders().getSign()));
    }

    @Test
    @DisplayName("公网 host 只剥伪造头不加签")
    void publicHostStripOnly() {
        InternalCallProperties props = new InternalCallProperties();
        props.setEnabled(true);
        props.setSharedSecret("secret");
        InternalCallFeignSupport support = support(props);
        RequestTemplate template = new RequestTemplate();
        Map<String, Collection<String>> headers = new LinkedHashMap<>();
        headers.put(props.getHeaders().getSign(), List.of("forged"));
        Request request = Request.create(Request.HttpMethod.GET, "https://api.example.com/x",
                headers, null, StandardCharsets.UTF_8, template);
        Request prepared = support.prepare(request);
        assertNull(prepared.headers().get(props.getHeaders().getSign()));
        assertNull(prepared.headers().get(props.getHeaders().getNonce()));
    }

    private static InternalCallFeignSupport support(InternalCallProperties props) {
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000L), ZoneOffset.UTC);
        return new InternalCallFeignSupport(
                props,
                new InternalCallSigner(props, clock),
                new CallerIdentityProvider(new MockEnvironment().withProperty("spring.application.name", "svc")),
                new InternalHostMatcher(List.of("*.svc.cluster.local"))
        );
    }
}
