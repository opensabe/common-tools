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
package io.github.opensabe.alive.codec;

import com.google.protobuf.GeneratedMessageV3;
import io.github.opensabe.alive.protobuf.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoBufDecoder extends ByteToMessageDecoder {

    private static Logger log = LoggerFactory.getLogger(ProtoBufDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() > 2) {
            short methodId = in.readShort();
            byte[] message = new byte[in.readableBytes()];
            in.readBytes(message);
            out.add(decode(methodId, message));
        }
    }


    private static ConcurrentHashMap<String, Class<? extends GeneratedMessageV3>> name2classMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Method> name2methodMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static GeneratedMessageV3 decode(short methodId, byte[] message) {

        String classNameStr = MessageType.methodIdToRequestName.get(methodId);
        Class<? extends GeneratedMessageV3> generatedMessageClass = name2classMap.get(classNameStr);
        Method parseMethod = name2methodMap.get(classNameStr);
        try {

            if (generatedMessageClass == null) {
                generatedMessageClass = (Class<? extends GeneratedMessageV3>) Class.forName(classNameStr);
                parseMethod = generatedMessageClass.getDeclaredMethod("parseFrom", byte[].class);
                name2classMap.put(classNameStr, generatedMessageClass);
                name2methodMap.put(classNameStr, parseMethod);
            }
            GeneratedMessageV3 generatedMessage = (GeneratedMessageV3) parseMethod.invoke(generatedMessageClass, message);
            return generatedMessage;
        } catch (Exception e) {
            log.error("deocde message error.", e);
            return null;
        }
    }
}
