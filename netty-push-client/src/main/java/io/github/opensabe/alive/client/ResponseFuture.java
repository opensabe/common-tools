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
package io.github.opensabe.alive.client;

import java.util.concurrent.TimeUnit;

import io.github.opensabe.alive.client.exception.AliveClientExecutionException;
import io.github.opensabe.alive.client.exception.AliveClientTimeoutException;

public interface ResponseFuture {

    /**
     * 获取响应结果，直到结果返回
     *
     * @return Response
     * @throws InterruptedException          打断时抛出
     * @throws AliveClientExecutionException 执行过程中出现异常抛出
     */
    Response get() throws InterruptedException, AliveClientExecutionException;

    /**
     * 获取响应结果，可以指定超时时间
     *
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return Response
     * @throws InterruptedException          打断时抛出
     * @throws AliveClientExecutionException 执行过程中出现异常抛出
     * @throws AliveClientTimeoutException   执行超时抛出
     */
    Response get(long timeout, TimeUnit unit)
            throws InterruptedException, AliveClientExecutionException, AliveClientTimeoutException;

    /**
     * 获取响应结果，忽略打断异常，直到结果返回
     *
     * @return Response
     * @throws AliveClientExecutionException 执行过程中出现异常抛出
     */
    Response getUninterruptibly() throws AliveClientExecutionException;

    /**
     * 获取响应结果，可以指定超时时间，忽略打断异常，直到结果返回
     *
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return Response
     * @throws InterruptedException          打断时抛出
     * @throws AliveClientExecutionException 执行过程中出现异常抛出
     * @throws AliveClientTimeoutException   执行超时抛出
     */
    Response getUninterruptibly(long timeout, TimeUnit unit) throws AliveClientExecutionException, AliveClientTimeoutException;

}
