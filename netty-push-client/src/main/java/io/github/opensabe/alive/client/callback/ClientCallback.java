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
package io.github.opensabe.alive.client.callback;

import java.util.Set;

import io.github.opensabe.alive.protobuf.Message;

/**
 * Alive 推送客户端异步操作完成回调。
 *
 * @author lone
 */
public interface ClientCallback {

    /**
     * 推送或请求完成时回调。
     *
     * @param response 服务端响应集合
     */
    void opComplete(Set<Message.Response> response);
}
