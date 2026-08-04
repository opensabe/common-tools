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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * AliveServerList 类。
 * <p>Alive 推送ServerList。</p>
 */
public class AliveServerList implements Watcher {

/** emptyServerList 字段。 */
    private static InetSocketAddress[] emptyServerList = new InetSocketAddress[]{};

/** logger 字段。 */
    private static Logger logger = LoggerFactory.getLogger(AliveServerList.class);

    private volatile InetSocketAddress[] serverList = emptyServerList;

/** listener 字段。 */
    private AliveServerListListener listener;

/** curator 字段。 */
    private CuratorFramework curator;

/** closed 字段。 */
    private boolean closed = false;

/** started 字段。 */
    private boolean started = false;

/** zkDelayLock 字段。 */
    private Object zkDelayLock = new Object();

/** zkDelayed 字段。 */
    private boolean zkDelayed = false;

/** zkPath 字段。 */
    private String zkPath;

/** zkMaxDelay 字段。 */
    private int zkMaxDelay;

    public AliveServerList(String zkString, String zkPath, int zkRetryInterval, int zkRetryMax, int zkMaxDelay,
                           AliveServerListListener listener) {
        this.zkPath = zkPath;
        this.zkMaxDelay = zkMaxDelay;
        this.curator = CuratorFrameworkFactory.newClient(zkString, new RetryNTimes(zkRetryInterval, zkRetryMax));
        this.listener = listener;

        curator.getConnectionStateListenable().addListener(new ConnectionStateListener() {

/**
 * stateChanged 方法。
 */
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                logger.info("stateChanged client {},state{}", client, newState);
                if (newState == ConnectionState.RECONNECTED || newState == ConnectionState.CONNECTED) {
                    watchServerList();
                    refreshServerList();
                }
                if (!zkDelayed) {
                    synchronized (zkDelayLock) {
                        zkDelayed = true;
                        zkDelayLock.notify();
                    }
                }
            }
        });
    }

    public AliveServerList() {
    }

/**
 * start 方法。
 */
    public synchronized void start() {
        if (!started) {
            started = true;
            curator.start();

            synchronized (zkDelayLock) {
                if (!zkDelayed) {
                    try {
                        zkDelayLock.wait(zkMaxDelay);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }
    }

/**
 * close 方法。
 */
    public synchronized void close() {
        logger.info("try to close zookeeper");
        if (!closed) {
            closed = true;
            if (curator != null) {
                try {
                    curator.close();
                } catch (Exception e) {
                    logger.error("zookeeper close error", e);
                }
            }
        }
        logger.info("close zookeeper");
    }

/**
 * getServerList 方法。
 */
    public InetSocketAddress[] getServerList() {
        return serverList;
    }

/**
 * watchServerList 方法。
 */
    private void watchServerList() {
        logger.info("try to watch server list");
        try {
            curator.getChildren().usingWatcher(this).forPath(zkPath);
        } catch (Exception e) {
            logger.error("try to watch server list error", e);
        }
    }

/**
 * refreshServerList 方法。
 */
    public void refreshServerList() {
        logger.info("try to refresh server list");
        try {
            List<InetSocketAddress> newServerList = new ArrayList<>();
            List<String> nodeList = curator.getChildren().forPath(zkPath);
            for (String node : nodeList) {
                try {
                    byte[] data = curator.getData().forPath(zkPath + "/" + node);
                    String address = new String(data, "UTF-8");
                    InetSocketAddress inetSocketAddress = ClientUtils.string2InetSocketAddress(address);
                    if (inetSocketAddress != null) {
                        newServerList.add(inetSocketAddress);
                    }
                } catch (Exception e) {
                    logger.error("try to read node " + node, e);
                }
            }
            serverList = newServerList.toArray(new InetSocketAddress[]{});
            logger.info("alive server list " + StringUtils.join(serverList, ","));
        } catch (Exception e) {
            logger.info("refresh server list error", e);
        }
        if (listener != null) {
            listener.serverListChanged();
        }
    }

/**
 * process 方法。
 */
    @Override
    public void process(WatchedEvent event) {
        logger.info("zookeeper trigger event, " + event);
        if (StringUtils.equals(event.getPath(), zkPath)
                && event.getType() == EventType.NodeChildrenChanged) {
            watchServerList();
            refreshServerList();
        }
    }

/**
 * getCurator 方法。
 */
    public CuratorFramework getCurator() {
        return curator;
    }

/**
 * AliveServerListListener 接口。
 * <p>Alive 推送ServerListListener。</p>
 */
    public interface AliveServerListListener {

        void serverListChanged();
    }
}
