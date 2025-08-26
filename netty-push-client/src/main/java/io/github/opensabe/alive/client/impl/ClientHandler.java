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

import java.net.ConnectException;

import io.github.opensabe.alive.client.callback.CallbackManager;
import io.github.opensabe.alive.client.exception.AliveClientException;
import io.github.opensabe.alive.protobuf.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private Channel channel = null;

    private ClientConnection connection;

    public void write(Object msg) {
        if (channel != null) {
            channel.writeAndFlush(msg);
        }
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (connection != null) {
            connection.error(this, new ConnectException("Connect inactive."));
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // TODO : 异常处理
        if (msg instanceof Message.Response) {
            if (connection != null) {
                Message.Response response = (Message.Response) msg;
                CallbackManager.finishTask(response);
                connection.recieve(this, response);
            }
        } else if (msg instanceof Message.HeartBeat) {
            if (connection != null) {
                connection.recieve(this, (Message.HeartBeat) msg);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.WRITER_IDLE)) {
                connection.heartbeat(this);
            } else if (event.state().equals(IdleState.READER_IDLE)) {
                connection.error(this, new AliveClientException("No heart beat response"));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
        if (connection != null) {
            connection.error(this, t);
        }
    }

    public void setConnection(ClientConnection connection) {
        this.connection = connection;
    }

}
