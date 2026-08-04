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
package io.github.opensabe.spring.cloud.parent.web.common.feign.preheating;

import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign 客户端预热基础接口。
 * <p>
 * 预热 FeignClient 可继承此接口并在应用就绪后调用 {@link #heartbeat()} 触发首次请求。
 */
public interface FeignPreheatingBase {
    /**
     * 调用健康检查端点完成预热。
     *
     * @return 健康检查响应体
     */
    @GetMapping("/actuator/health")
    String heartbeat();
}
