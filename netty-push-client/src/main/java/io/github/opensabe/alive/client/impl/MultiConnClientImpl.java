package io.github.opensabe.alive.client.impl;

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
import io.github.opensabe.alive.util.ConsistentHash;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 增加同一节点 多条连接支持
 * 在进行服务器端压测过程中，发现一条tcp连接传输会有限制，虽然通过增加读写缓冲区能够缓解该问题
 * 但是最根本的方法还是用多条连接发送业务端数据
 * Created by bjzhaolianwang on 2018/1/2.
 */
public class MultiConnClientImpl extends AbstractClient implements AliveServerListListener {

    private Logger logger = LoggerFactory.getLogger(MultiConnClientImpl.class);

    private Bootstrap bootstrap;

    private AliveServerList aliveServerList;

    private volatile ClientConnectionGetter clientConnectionGetter = new ClientConnectionGetter(new ArrayList<ClientConnection>());

    private volatile List<ClientConnection> clientConnectionList = new LinkedList<ClientConnection>();

    private volatile Map<InetSocketAddress, List<ClientConnection>> clientConnectionMap = new ConcurrentHashMap<>();
    private volatile Map<Integer, List<ClientConnection>> clientNumMap = new ConcurrentHashMap<>();

    private long heartInterval;

    private int clientConnectionNum = 1;
    private static final int DEFAULT_CONNECTION_NUM = 1;//每个节点默认连接数

    public MultiConnClientImpl(int productCode, String authToken, int clientConnectionNum,
        String zkString, String zkPath, int zkRetryInterval, int zkRetryMax, int zkMaxDelay,
        long connectTimeout, long authTimeout, long heartTimeout, long heartInterval, ZkTasker zkTasker) {

        super(productCode, authToken, connectTimeout, authTimeout);

        this.heartInterval = heartInterval;
        this.clientConnectionNum = clientConnectionNum;

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
                ch.pipeline().addLast("IdleStateHandler", new IdleStateHandler(MultiConnClientImpl.this.heartInterval * 2,
                    MultiConnClientImpl.this.heartInterval, 0L, TimeUnit.MILLISECONDS));
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
        List<ClientConnection> currentClientConnectionList = getPushConnections(messageVo);
        if (currentClientConnectionList == null || currentClientConnectionList.isEmpty()) {
            throw new AliveClientException("no alive server discovered.");
        }
        BaseResponseFutureMultiImpl baseFutureImpl = new BaseResponseFutureMultiImpl();

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
        Map<InetSocketAddress, List<ClientConnection>> newClientConnectionMap = new HashMap<>();

        // 创建新的列表
        for (InetSocketAddress address : socketAddresses) {
            List<ClientConnection> tmp = new ArrayList<>();
            for (int i = 0; i < this.clientConnectionNum; i++) {
                ClientConnection clientConnection;
                if (clientConnectionMap.containsKey(address)) {
                    tmp = clientConnectionMap.get(address);
                    break;
                } else {
                    clientConnection = new ClientConnection(bootstrap, address, productCode, authToken, connectTimeout, authTimeout);
                    tmp.add(clientConnection);
                }
            }
            newClientConnectionList.addAll(tmp);
            newClientConnectionMap.put(address, tmp);
        }

        // 关闭无用连接
        for (InetSocketAddress address : clientConnectionMap.keySet()) {
            if (!newClientConnectionMap.containsKey(address)) {
                List<ClientConnection> tmp = clientConnectionMap.get(address);
                for (ClientConnection conn : tmp) {
                    conn.close();
                }
            }
        }
        ClientConnectionGetter newClientGetter = new ClientConnectionGetter(newClientConnectionList);
        this.clientConnectionGetter = newClientGetter;
        this.clientConnectionList = newClientConnectionList;
        this.clientConnectionMap = newClientConnectionMap;
        this.initClientNumMap();
    }

    private AtomicInteger loopCount = new AtomicInteger(0);

    private ClientConnection getConnection() {
        List<ClientConnection> connList = clientConnectionList;
        return connList.get((loopCount.getAndIncrement() % connList.size() + connList.size()) % connList.size());
    }

    private void initClientNumMap() {
        for (int i = 0; i < this.clientConnectionNum; i++) {
            List<ClientConnection> clients = new ArrayList<>();
            for (InetSocketAddress address : clientConnectionMap.keySet()) {
                List<ClientConnection> tmp = clientConnectionMap.get(address);
                if (tmp != null && tmp.size() > i) {
                    clients.add(tmp.get(i));
                } else if (tmp.size() > 0) {
                    clients.add(tmp.get(0));
                }
            }
            if (clients.size() > 0) {
                clientNumMap.put(new Integer(i), clients);
            }
        }
    }

    private List<ClientConnection> getPushConnections(PushVo pushVo) {
        if (pushVo == null) {
            throw new NullPointerException();
        }
        //广播，则每个节点返回一个连接
        if (pushVo.pushType == Message.PushType.GROUP) {
            int index = ConsistentHash.getIndex(clientConnectionNum);
            return clientNumMap.containsKey(index) && clientNumMap.get(index).size() > 0 ? clientNumMap.get(index) : clientNumMap.get(0);
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
