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
package io.github.opensabe.alive.protobuf;

import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.GeneratedMessageV3;

public class MessageType {


    public static final short HEART_BEAT = 0x0000;  //心跳
    public static final short REG_DEVICE = 0x0001;  // 客户端注册设备
    public static final short SUB_CHANNEL = 0x0002; //客户端订阅频道

    public static final short RESPONSE = 0x0101; //长连接消息响应
    public static final short RET_MESSAGE = 0x0102; //长连接转发给客户端的频道订阅消息

    public static final short AUTH_BACKEND = 0x0201; //服务器验证连接合法性
    public static final short PUBLISH_MESSAGE = 0x0202; //服务器发布的消息
    public static final short QUERY_CACHE = 0x0203; //查询消息缓存

    public static final short AUTH_REQUEST = 0x0003;
    public static final short ACK = 0x0004;
    public static final short CHAT_MSG = 0x0103;
    public static final short SEND_SET_CMD = 0x0204;
    public static final short CHAT_AUTH = 0x0205;
    public static final short USTATE_QUERY = 0x0206;
    public static final short USTATE_RESPONSE = 0x0207;
    public static final short UACTION_NOTICE = 0x0208;
    public static final Map<String, Class<? extends GeneratedMessageV3>> typeToMessageClass = new HashMap();
    public static final Map<Class<? extends GeneratedMessageV3>, String> classToMessageType = new HashMap();
    public static Map<Short, String> methodIdToRequestName = new HashMap<Short, String>();
    public static Map<String, Short> requestNameToMethodId = new HashMap<String, Short>();

    static {
        methodIdToRequestName.put(HEART_BEAT, Message.HeartBeat.class.getName());
        methodIdToRequestName.put(REG_DEVICE, Message.RegDev.class.getName());
        methodIdToRequestName.put(SUB_CHANNEL, Message.Subscribe.class.getName());
        methodIdToRequestName.put(AUTH_REQUEST, Message.AuthRequest.class.getName());
        methodIdToRequestName.put(ACK, Message.ACK.class.getName());
        methodIdToRequestName.put(RESPONSE, Message.Response.class.getName());
        methodIdToRequestName.put(RET_MESSAGE, Message.RetMsg.class.getName());
        methodIdToRequestName.put(CHAT_MSG, Message.ChatMsg.class.getName());
        methodIdToRequestName.put(AUTH_BACKEND, Message.AuthBackend.class.getName());
        methodIdToRequestName.put(PUBLISH_MESSAGE, Message.Publish.class.getName());
        methodIdToRequestName.put(QUERY_CACHE, Message.Query.class.getName());
        methodIdToRequestName.put(SEND_SET_CMD, Message.SendSetCommand.class.getName());
        methodIdToRequestName.put(CHAT_AUTH, Message.ChatAuth.class.getName());
        methodIdToRequestName.put(USTATE_QUERY, Message.UStateQuery.class.getName());
        methodIdToRequestName.put(USTATE_RESPONSE, Message.UStateResponse.class.getName());
        methodIdToRequestName.put(UACTION_NOTICE, Message.UActionNotice.class.getName());

        requestNameToMethodId.put(Message.HeartBeat.class.getName(), HEART_BEAT);
        requestNameToMethodId.put(Message.RegDev.class.getName(), REG_DEVICE);
        requestNameToMethodId.put(Message.Subscribe.class.getName(), SUB_CHANNEL);
        requestNameToMethodId.put(Message.AuthRequest.class.getName(), AUTH_REQUEST);
        requestNameToMethodId.put(Message.ACK.class.getName(), ACK);
        requestNameToMethodId.put(Message.Response.class.getName(), RESPONSE);
        requestNameToMethodId.put(Message.RetMsg.class.getName(), RET_MESSAGE);
        requestNameToMethodId.put(Message.ChatMsg.class.getName(), CHAT_MSG);
        requestNameToMethodId.put(Message.AuthBackend.class.getName(), AUTH_BACKEND);
        requestNameToMethodId.put(Message.Publish.class.getName(), PUBLISH_MESSAGE);
        requestNameToMethodId.put(Message.Query.class.getName(), QUERY_CACHE);
        requestNameToMethodId.put(Message.SendSetCommand.class.getName(), SEND_SET_CMD);
        requestNameToMethodId.put(Message.UStateQuery.class.getName(), USTATE_QUERY);
        requestNameToMethodId.put(Message.UStateResponse.class.getName(), USTATE_RESPONSE);
        requestNameToMethodId.put(Message.UActionNotice.class.getName(), UACTION_NOTICE);
        requestNameToMethodId.put(Message.ChatAuth.class.getName(), CHAT_AUTH);
        typeToMessageClass.put("reg", Message.RegDev.class);
        typeToMessageClass.put("sub", Message.Subscribe.class);
        typeToMessageClass.put("authReq", Message.AuthRequest.class);
        typeToMessageClass.put("ack", Message.ACK.class);
        typeToMessageClass.put("resp", Message.Response.class);
        typeToMessageClass.put("ret", Message.RetMsg.class);
        typeToMessageClass.put("chatMsg", Message.ChatMsg.class);
    }
}