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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.opensabe.alive.client.ResponseFuture;
import io.github.opensabe.alive.client.callback.CallbackManager;
import io.github.opensabe.alive.client.callback.ClientCallback;
import io.github.opensabe.alive.client.exception.AliveClientException;
import io.github.opensabe.alive.client.impl.AliveServerList.AliveServerListListener;
import io.github.opensabe.alive.client.impl.future.BaseResponseFutureMultiImpl;
import io.github.opensabe.alive.client.impl.future.ResponseFutureImpl;
import io.github.opensabe.alive.client.task.ZkTasker;
import io.github.opensabe.alive.client.vo.MessageVo;
import io.github.opensabe.alive.client.vo.PushVo;
import io.github.opensabe.alive.client.vo.QueryVo;
import io.github.opensabe.alive.codec.Int32FrameDecoder;
import io.github.opensabe.alive.codec.Int32FrameEncoder;
import io.github.opensabe.alive.codec.ProtoBufDecoder;
import io.github.opensabe.alive.codec.ProtoBufEncoder;
import io.github.opensabe.alive.protobuf.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class ClientImpl extends AbstractClient implements AliveServerListListener {

    private Logger logger = LoggerFactory.getLogger(ClientImpl.class);

    private Bootstrap bootstrap;

    private AliveServerList aliveServerList;

    private volatile ClientConnectionGetter clientConnectionGetter = new ClientConnectionGetter(new ArrayList<ClientConnection>());

    private volatile List<ClientConnection> clientConnectionList = new LinkedList<ClientConnection>();

    private volatile Map<InetSocketAddress, ClientConnection> clientConnectionMap = new HashMap<InetSocketAddress, ClientConnection>();

    private long heartInterval;
    private AtomicInteger loopCount = new AtomicInteger(0);

    public ClientImpl(int productCode, String authToken,
                      String zkString, String zkPath, int zkRetryInterval, int zkRetryMax, int zkMaxDelay,
                      long connectTimeout, long authTimeout, long heartTimeout, long heartInterval, ZkTasker zkTasker) {

        super(productCode, authToken, connectTimeout, authTimeout);

        this.heartInterval = heartInterval;

        // 初始化netty
        Bootstrap bootstrap = new Bootstrap();

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("IdleStateHandler",
                        new IdleStateHandler(ClientImpl.this.heartInterval * 2, ClientImpl.this.heartInterval, 0L, TimeUnit.MILLISECONDS));
                ch.pipeline().addLast("Int32FrameEncoder", new Int32FrameEncoder());
                ch.pipeline().addLast("ProtoBufEncoder", new ProtoBufEncoder());
                ch.pipeline().addLast("Int32FrameDecoder", new Int32FrameDecoder());
                ch.pipeline().addLast("ProtoBufDecoder", new ProtoBufDecoder());
                ch.pipeline().addLast("ClientHandler", new ClientHandler());

            }
        });
        this.bootstrap = bootstrap;

        // 初始获取zk
        this.aliveServerList = new AliveServerList(zkString, zkPath, zkRetryInterval, zkRetryMax, zkMaxDelay, this);
        this.aliveServerList.start();
        zkTasker.getServerLists().add(aliveServerList);
    }

    @Override
    public int queryAsync(QueryVo queryVo, ClientCallback callback) throws AliveClientException {
        List<ClientConnection> currentClientConnectionList = getPushConnections(queryVo);
        if (currentClientConnectionList == null || currentClientConnectionList.isEmpty()) {
            throw new AliveClientException("no alive server discovered.");
        }
        AtomicInteger integer = new AtomicInteger(0);
        // 查询只需要查询一个机器就可以了
        ClientConnection clientConnection = getConnection();
        int requestId = clientConnection.queryAsync(queryVo, callback);
        CallbackManager.addTask(requestId, integer, callback);
        return requestId;
    }

    @Override
    public synchronized ResponseFuture pushAsync(MessageVo messageVo) throws AliveClientException {
        //根据推送类型 获取可用服务连接地址
        List<ClientConnection> currentClientConnectionList = getPushConnections(messageVo);
        if (currentClientConnectionList == null || currentClientConnectionList.isEmpty()) {
            throw new AliveClientException("no alive server discovered.");
        }
        BaseResponseFutureMultiImpl baseFutureImpl = new BaseResponseFutureMultiImpl();
        //循环推送
        for (ClientConnection clientConnection : currentClientConnectionList) {
            try {
                baseFutureImpl.add(clientConnection.pushAsync(messageVo));
            } catch (AliveClientException e) {
                logger.error("client push to connection " + clientConnection.getAddress() + " exception.", e);
            }
        }
        return new ResponseFutureImpl(baseFutureImpl);
    }

    @Override
    public synchronized int pushAsync(MessageVo messageVo, ClientCallback callback) throws AliveClientException {
        List<ClientConnection> currentClientConnectionList = getPushConnections(messageVo);
        if (currentClientConnectionList == null || currentClientConnectionList.isEmpty()) {
            throw new AliveClientException("no alive server discovered.");
        }
        AtomicInteger integer = new AtomicInteger(0);
        for (ClientConnection clientConnection : currentClientConnectionList) {
            try {
                int requestId = clientConnection.pushAsync(messageVo, callback);
                CallbackManager.addTask(requestId, integer, callback);
            } catch (AliveClientException e) {
                logger.error("client push to connection " + clientConnection.getAddress() + " exception.", e);
            }
        }
        return -1;
    }

    @Override
    public synchronized void close() {
        for (ClientConnection clientConnection : clientConnectionList) {
            clientConnection.close();
        }

        clientConnectionList = new LinkedList<ClientConnection>();
        bootstrap.group().shutdownGracefully();
        aliveServerList.close();
    }

    @Override
    public void serverListChanged() {
        rebuildClientConnection();
    }

    private synchronized void rebuildClientConnection() {
        InetSocketAddress[] socketAddresses = aliveServerList.getServerList();
        List<ClientConnection> newClientConnectionList = new LinkedList<>();
        Map<InetSocketAddress, ClientConnection> newClientConnectionMap = new HashMap<>();

        // 创建新的列表
        for (InetSocketAddress address : socketAddresses) {
            ClientConnection clientConnection;
            if (clientConnectionMap.containsKey(address)) {
                clientConnection = clientConnectionMap.get(address);
            } else {
                clientConnection = new ClientConnection(bootstrap, address, productCode, authToken, connectTimeout, authTimeout);
            }

            newClientConnectionList.add(clientConnection);
            newClientConnectionMap.put(address, clientConnection);
        }

        // 关闭无用连接
        for (InetSocketAddress address : clientConnectionMap.keySet()) {
            if (!newClientConnectionMap.containsKey(address)) {
                clientConnectionMap.get(address).close();
            }
        }
        ClientConnectionGetter newClientGetter = new ClientConnectionGetter(newClientConnectionList);
        this.clientConnectionGetter = newClientGetter;
        this.clientConnectionList = newClientConnectionList;
        this.clientConnectionMap = newClientConnectionMap;
    }

    private ClientConnection getConnection() {
        List<ClientConnection> connList = clientConnectionList;
        return connList.get((loopCount.getAndIncrement() % connList.size() + connList.size()) % connList.size());
    }

    private List<ClientConnection> getPushConnections(PushVo pushVo) {
        if (pushVo == null) {
            throw new NullPointerException();
        }
        if (pushVo.pushType == Message.PushType.GROUP) {
            return clientConnectionList;
        }
        if (pushVo.pushType == Message.PushType.SPECIAL) {
            ClientConnection conn = clientConnectionGetter.getConnection(pushVo.deviceId);
            if (conn != null) {
                return Collections.singletonList(conn);
            } else {
                return Collections.emptyList();
            }
        }
        if (pushVo.pushType == Message.PushType.MULTI) {
            ClientConnection conn = null;
            conn = clientConnectionGetter.getConnection(pushVo.deviceId);
            if (conn != null) {
                return Collections.singletonList(conn);
            } else {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

}
