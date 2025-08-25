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

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.opensabe.alive.client.Response;
import io.github.opensabe.alive.client.ResponseFuture;
import io.github.opensabe.alive.client.callback.ClientCallback;
import io.github.opensabe.alive.client.exception.AliveClientException;
import io.github.opensabe.alive.client.exception.AliveClientExecutionException;
import io.github.opensabe.alive.client.exception.AliveClientTimeoutException;
import io.github.opensabe.alive.client.impl.future.BaseResponseFutureImpl;
import io.github.opensabe.alive.client.impl.future.ResponseFutureImpl;
import io.github.opensabe.alive.client.vo.MessageVo;
import io.github.opensabe.alive.client.vo.QueryVo;
import io.github.opensabe.alive.protobuf.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;

public class ClientConnection extends AbstractClient {

    private static AtomicInteger requestId = new AtomicInteger(1);
    private final SocketAddress address;

    private final Bootstrap bootstrap;
    private Logger logger = LoggerFactory.getLogger(ClientConnection.class);
    private ClientHandler handler;
    private boolean closed = false;
    private Map<Integer, BaseResponseFutureImpl> baseFutureMap = new HashMap<Integer, BaseResponseFutureImpl>();

    public ClientConnection(Bootstrap bootstrap, SocketAddress socketAddress, int productCode, String authToken,
                            long connectTimeout, long authTimeout) {
        super(productCode, authToken, connectTimeout, authTimeout);

        this.address = socketAddress;
        this.bootstrap = bootstrap;
    }

    public static int getReqeustId() {
        return requestId.getAndIncrement();
    }

    private ClientHandler createOrGetHandler() throws AliveClientException {
        if (closed) {
            throw new AliveClientException("Connection closed");
        }
        if (handler == null) {
            logger.info("connection to " + address + " try to create channel.");
            ChannelFuture future = bootstrap.connect(address);
            if (!future.awaitUninterruptibly(connectTimeout, TimeUnit.MILLISECONDS)) {
                throw new AliveClientException("Create and init connection connect error.",
                        new AliveClientTimeoutException("Connect timeout"));
            }

            logger.info("connection to " + address + " successfully connect.");

            handler = (ClientHandler) future.channel().pipeline().get("ClientHandler");
            handler.setConnection(this);
            //先进行服务授权，授权通过后才能进行推送
            ResponseFuture authFuture = authAsync(productCode, authToken);

            try {
                if (Response.FAIL == authFuture.getUninterruptibly(authTimeout, TimeUnit.MILLISECONDS)) {
                    throw new AliveClientException("Create and init connection auth fail.");
                }
            } catch (AliveClientExecutionException | AliveClientTimeoutException e) {
                throw new AliveClientException("Create and init connection auth error.", e);
            }

            logger.info("connection to " + address + " successfully create and auth new channel handler " + handler + ".");
        }
        return handler;
    }

    private BaseResponseFutureImpl createOrGetFuture(int requestId) {
        BaseResponseFutureImpl baseFuture;
        synchronized (baseFutureMap) {
            if (baseFutureMap.containsKey(requestId)) {
                baseFuture = baseFutureMap.get(requestId);
            } else {
                baseFuture = new BaseResponseFutureImpl();
                baseFutureMap.put(requestId, baseFuture);
            }
        }
        return baseFuture;
    }

    private ResponseFuture authAsync(int productCode, String authToken) throws AliveClientException {
        int myRequestId = getReqeustId();

        // 创建future
        BaseResponseFutureImpl baseFuture = createOrGetFuture(myRequestId);

        // 创建消息
        Message.AuthBackend msg = Message.AuthBackend.newBuilder()
                .setRequestId(myRequestId)
                .setProductCode(productCode)
                .setAuthToken(authToken)
                .build();

        // 发送消息
        ClientHandler h = createOrGetHandler();
        h.write(msg);

        return new ResponseFutureImpl(baseFuture);
    }

    @Override
    public int queryAsync(QueryVo queryVo, ClientCallback callback) throws AliveClientException {
        ClientHandler h = createOrGetHandler();
        int myRequestId = getReqeustId();
        // 发送消息
        h.write(queryVo.build(myRequestId, productCode));
        return myRequestId;
    }

    @Override
    public synchronized ResponseFuture pushAsync(MessageVo messageVo) throws AliveClientException {
        //先进行服务授权，授权通过后获取Handler
        ClientHandler h = createOrGetHandler();

        int myRequestId = messageVo.requestId == 0 ? getReqeustId() : messageVo.requestId;

        // 创建相关future
        BaseResponseFutureImpl baseFuture = createOrGetFuture(myRequestId);

        // 发送消息
        h.write(messageVo.buildPublush(myRequestId, productCode));

        return new ResponseFutureImpl(baseFuture);
    }

    @Override
    public synchronized int pushAsync(MessageVo messageVo, ClientCallback callback) throws AliveClientException {
        ClientHandler h = createOrGetHandler();
        int myRequestId = messageVo.requestId == 0 ? getReqeustId() : messageVo.requestId;
        // 发送消息
        h.write(messageVo.buildPublush(myRequestId, productCode));
        return myRequestId;
    }

    @Override
    public synchronized void close() {
        logger.info("connection to " + address + " try to close.");
        if (!closed) {
            closed = true;
            if (handler != null) {
                handler.close();
            }
        }
    }

    public synchronized void error(ClientHandler handler, Throwable t) {
        if (closed) {
            return;
        }
        logger.error("connection to " + address + " catch exception use channel " + handler + ".", t);
        if (this.handler != handler) {
            logger.error("connection to " + address + " close unexcepted channel " + handler + ".");
            handler.close();
        } else {
            // 关闭handler
            logger.error("connection to " + address + " try to close channel " + handler + ".");
            this.handler.close();
            this.handler = null;

            synchronized (baseFutureMap) {
                for (Map.Entry<Integer, BaseResponseFutureImpl> entry : baseFutureMap.entrySet()) {
                    entry.getValue().setException(new AliveClientExecutionException("connection catch exception.", t));
                }

                baseFutureMap.clear();
            }
        }
    }

    public synchronized void heartbeat(ClientHandler handler) {
        logger.debug("connection to " + address + " try to send heartbeat for channel " + handler + ".");
        if (this.handler == handler) {
            logger.debug("connection to " + address + " send heartbeat for channel " + handler + ".");
            int myRequestId = getReqeustId();

            Message.HeartBeat msg = Message.HeartBeat.newBuilder()
                    .setRequestId(myRequestId)
                    .build();

            handler.write(msg);
        } else {
            logger.debug("connection to " + address + " use a different channel " + this.handler + ".");
        }
    }

    public void recieve(ClientHandler handler, Message.Response resp) {
        setResponse(resp.getRequestId(), ClientUtils.retCode2Response(resp.getRetCode()));
    }

    public void recieve(ClientHandler handler, Message.HeartBeat hb) {
        setResponse(hb.getRequestId(), Response.SUCEESS);
    }

    private void setResponse(int reqeustId, Response resp) {
        synchronized (baseFutureMap) {
            if (baseFutureMap.containsKey(reqeustId)) {
                BaseResponseFutureImpl baseFuture = baseFutureMap.get(reqeustId);
                baseFuture.set(resp);

                baseFutureMap.remove(reqeustId);
            }
        }
    }

    public SocketAddress getAddress() {
        return address;
    }

}
