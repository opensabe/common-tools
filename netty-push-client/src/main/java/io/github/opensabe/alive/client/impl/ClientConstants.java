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
package io.github.opensabe.alive.client.impl;


public class ClientConstants {

    /**
     * zk启动延迟最大时间，默认1000ms
     */
    public static final int ZK_MAX_DELAY = 1000;
    /**
     * zk调用失败重试次数，默认3
     */
    public static final int ZK_RETRY_MAX = 3;
    public static final int ZK_TIMEOUT = 1000;
    public static final String ZK_PATH = "/nettypush_clientServer/host";
    public static final long DEFAULT_TIMEOUT = 5000L;
    /**
     * zk调用失败重试等待时间，默认100ms
     */
    public static final int ZK_RETRY_INTERVAL = 500;

}
