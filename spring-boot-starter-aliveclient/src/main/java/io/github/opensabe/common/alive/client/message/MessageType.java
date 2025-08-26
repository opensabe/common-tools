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
package io.github.opensabe.common.alive.client.message;

public class MessageType {
    public static final short HEART_BEAT = 0;
    public static final short REG_DEVICE = 1;
    public static final short SUB_CHANNEL = 2;
    public static final short RESPONSE = 257;
    public static final short RET_MESSAGE = 258;
    public static final short AUTH_BACKEND = 513;
    public static final short PUBLISH_MESSAGE = 514;
    public static final short QUERY_CACHE = 515;
    public static final short AUTH_REQUEST = 3;
    public static final short ACK = 4;
    public static final short CHAT_MSG = 259;
    public static final short SEND_SET_CMD = 516;
    public static final short CHAT_AUTH = 517;
    public static final short USTATE_QUERY = 518;
    public static final short USTATE_RESPONSE = 519;
    public static final short UACTION_NOTICE = 520;
}