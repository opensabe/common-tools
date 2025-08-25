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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;


import io.github.opensabe.alive.client.Response;
import io.github.opensabe.alive.protobuf.Message;
import org.apache.commons.lang3.StringUtils;

public class ClientUtils {

    public static void sleepUninterruptibly(long timeout, TimeUnit unit) {
        if (timeout == 0) {
            return;
        }
        long end = System.currentTimeMillis() + unit.toMillis(timeout);
        while (true) {
            timeout = end - System.currentTimeMillis();
            if (timeout <= 0) {
                return;
            }
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {

            }
        }

    }

    public static InetSocketAddress string2InetSocketAddress(String data) {
        try {
            String[] args = StringUtils.split(data, ":");
            return new InetSocketAddress(args[0], Integer.parseInt(args[1]));
        } catch (Exception e) {
            return null;
        }
    }

    public static Response retCode2Response(Message.RetCode retCode) {
        switch (retCode.getNumber()) {
            case Message.RetCode.SUCCESS_VALUE:
                return Response.SUCEESS;
            case Message.RetCode.FAIL_VALUE:
                return Response.FAIL;
            case Message.RetCode.CACHED_VALUE:
                return Response.CACHED;
            default:
                return null;
        }
    }

    public static int length(short type, byte[] data) {
        return Short.SIZE / Byte.SIZE + data.length;
    }

    public static ByteBuf marshall(short type, byte[] data) {
        ByteBuf buf = Unpooled.buffer(Integer.SIZE / Byte.SIZE + Short.SIZE / Byte.SIZE + data.length);
        buf.writeInt(Short.SIZE / Byte.SIZE + data.length);
        buf.writeShort(type);
        buf.writeBytes(data);
        return buf;
    }
}
