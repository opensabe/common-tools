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

/**
 * MessageType 类。
 * <p>消息类型。</p>
 */
public class MessageType {
/** HEART_BEAT 字段。 */
    public static final short HEART_BEAT = 0;
/** REG_DEVICE 字段。 */
    public static final short REG_DEVICE = 1;
/** SUB_CHANNEL 字段。 */
    public static final short SUB_CHANNEL = 2;
/** RESPONSE 字段。 */
    public static final short RESPONSE = 257;
/** RET_MESSAGE 字段。 */
    public static final short RET_MESSAGE = 258;
/** AUTH_BACKEND 字段。 */
    public static final short AUTH_BACKEND = 513;
/** PUBLISH_MESSAGE 字段。 */
    public static final short PUBLISH_MESSAGE = 514;
/** QUERY_CACHE 字段。 */
    public static final short QUERY_CACHE = 515;
/** AUTH_REQUEST 字段。 */
    public static final short AUTH_REQUEST = 3;
/** ACK 字段。 */
    public static final short ACK = 4;
/** CHAT_MSG 字段。 */
    public static final short CHAT_MSG = 259;
/** SEND_SET_CMD 字段。 */
    public static final short SEND_SET_CMD = 516;
/** CHAT_AUTH 字段。 */
    public static final short CHAT_AUTH = 517;
/** USTATE_QUERY 字段。 */
    public static final short USTATE_QUERY = 518;
/** USTATE_RESPONSE 字段。 */
    public static final short USTATE_RESPONSE = 519;
/** UACTION_NOTICE 字段。 */
    public static final short UACTION_NOTICE = 520;
}