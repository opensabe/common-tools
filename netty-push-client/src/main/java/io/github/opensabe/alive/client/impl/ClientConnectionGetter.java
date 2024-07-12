package io.github.opensabe.alive.client.impl;

import io.github.opensabe.alive.util.ConsistentHash;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lone
 */
public class ClientConnectionGetter {

    private volatile ConsistentHash<String> consistentHash;
    private volatile Map<String, List<ClientConnection>> connectionMap;

    public ClientConnectionGetter(List<ClientConnection> connections) {
        build(connections);
    }

    private void build(List<ClientConnection> connections) {
        List<String> tmpHashs = new ArrayList<>();
        connectionMap = new HashMap<>();
        for (ClientConnection connection : connections) {
            String hashStr = getStrForHash(connection.getAddress());
            tmpHashs.add(hashStr);
            if (!connectionMap.containsKey(hashStr) || connectionMap.get(hashStr) == null) {
                connectionMap.put(hashStr, new ArrayList<ClientConnection>());
            }
            List<ClientConnection> tmp = connectionMap.get(hashStr);
            tmp.add(connection);
        }
        consistentHash = new ConsistentHash<>(200, tmpHashs);
    }

    public ClientConnection getConnection(String deviceId) {
        if (StringUtils.isBlank(deviceId)) {
            throw new NullPointerException();
        }
        List<ClientConnection> tmp = connectionMap.get(consistentHash.get(deviceId));
        return tmp.get(ConsistentHash.getIndex(tmp.size()));
    }

    private String getStrForHash(SocketAddress address) {
        final InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
        String host = inetSocketAddress.getHostName();
        int port = inetSocketAddress.getPort();
        return host + ":" + port;
    }

}
