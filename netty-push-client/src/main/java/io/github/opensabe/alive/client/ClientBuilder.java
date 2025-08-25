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
package io.github.opensabe.alive.client;

import io.github.opensabe.alive.client.impl.MultiConnClientImpl;
import io.github.opensabe.alive.client.task.ZkTasker;

import io.github.opensabe.alive.client.impl.ClientConstants;
import io.github.opensabe.alive.client.impl.ClientImpl;
import org.apache.commons.lang3.StringUtils;

public class ClientBuilder {

    private int productCode = 0;

    private String authToken;

    private String zkString;

    private int clientNum;

    private long connectTimeout = 5000L;

    private long authTimeout = 5000L;

    private long heartTimeout = 5000L;

    private long heartInterval = 10 * 1000L;

    private String zkPath = ClientConstants.ZK_PATH;

    private int zkRetryInterval = ClientConstants.ZK_RETRY_INTERVAL;

    private int zkRetryMax = ClientConstants.ZK_RETRY_MAX;

    private int zkMaxDelay = ClientConstants.ZK_MAX_DELAY;

    private ZkTasker zkTasker;

    /**
     * 创建builder
     *
     * @return 返回builder对象
     */
    public static ClientBuilder create() {
        return new ClientBuilder();
    }

    /**
     * 设置产品代码
     *
     * @param productCode 各个产品的产品代码
     * @return 返回自身
     */
    public ClientBuilder withProductCode(int productCode) {
        this.productCode = productCode;
        return this;
    }

    /**
     * 设置验证的authToken
     *
     * @param authToken 验证token
     * @return 返回自身
     */
    public ClientBuilder withAuthToken(String authToken) {
        this.authToken = authToken;
        return this;
    }

    /**
     * 设置连接超时时间，当创建连接超时时会抛出异常
     *
     * @param connectTimeout 连接超时时间，单位毫秒，默认1000ms
     * @return 返回自身
     */
    public ClientBuilder withConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 设置心跳超时时间，当心跳返回超过超时时间时客户端会重新建立到长连接平台连接
     *
     * @param heartTimeout 心跳超时时间，单位毫秒，默认1000ms
     * @return 返回自身
     */
    public ClientBuilder withHeartTimeout(long heartTimeout) {
        this.heartTimeout = heartTimeout;
        return this;
    }

    /**
     * 设置心跳间隔时间，当超过该间隔时间没有写操作时，会触发到长连接平台的心跳请求
     *
     * @param heartInterval 心跳间隔时间，单位毫秒，默认1000ms
     * @return 返回自身
     */
    public ClientBuilder withHeartInterval(long heartInterval) {
        this.heartInterval = heartInterval;
        return this;
    }

    /**
     * 设置认证超时时间，当认证超时时，会抛出异常
     *
     * @param authTimeout 认证请求超时时间，单位毫秒，默认1000ms
     * @return 返回自身
     */
    public ClientBuilder withAuthTimeout(long authTimeout) {
        this.authTimeout = authTimeout;
        return this;
    }

    /**
     * 设置长连接平台所用ZK路径，用于获得长连接服务器地址，不设置则直接读取默认ZK服务器地址。
     *
     * @param zkString 表示zk各个节点的字符串host:port,host:port，一般不需要配置，使用默认即可。
     * @return 返回自身
     */
    public ClientBuilder withZkString(String zkString) {
        this.zkString = zkString;
        return this;
    }

    /**
     * 设置长连接平台所用ZK路径，用于获得长连接服务器地址，不设置则直接读取默认路径。
     *
     * @param zkPath 路径，/开头的字符串，表示zk中的节点，一般不需要配置，使用默认即可。
     * @return 返回自身
     */
    public ClientBuilder withZkPath(String zkPath) {
        this.zkPath = zkPath;
        return this;
    }

    /**
     * 设置zk连接初始最大等待延迟时间
     *
     * @param zkMaxDelay zk初始化等待最长时间，单位毫秒，默认1000ms
     * @return 返回自身
     */
    public ClientBuilder withZkMaxDelay(int zkMaxDelay) {
        this.zkMaxDelay = zkMaxDelay;
        return this;
    }


    /**
     * 设置连接ZK时所用的会话超时时间
     *
     * @param zkTimeout zk会话超时时间，单位毫秒，默认1000ms
     * @return 返回自身
     */
    @Deprecated
    public ClientBuilder withZkTimeout(int zkTimeout) {
        return this;
    }

    /**
     * 设置 到每个节点的连接数量
     *
     * @param clientNum 连接数
     * @return 返回自身
     */
    public ClientBuilder withClientNum(int clientNum) {
        this.clientNum = clientNum;
        return this;
    }

    public ClientBuilder withZkTasker(ZkTasker zkTasker) {
        this.zkTasker = zkTasker;
        return this;
    }


    public Client build() {
        checkParamater();
        if (clientNum <= 1) {
            //每个节点 一个连接
            return new ClientImpl(productCode, authToken, zkString, zkPath, zkRetryInterval, zkRetryMax, zkMaxDelay, connectTimeout,
                authTimeout, heartTimeout, heartInterval, zkTasker);
        } else {
            //每个节点  多个连接
            return new MultiConnClientImpl(productCode, authToken, clientNum, zkString, zkPath, zkRetryInterval, zkRetryMax, zkMaxDelay,
                connectTimeout,
                authTimeout, heartTimeout, heartInterval, zkTasker);
        }
    }

    private void checkParamater() {
        if (productCode == 0 || StringUtils.isBlank(authToken) || StringUtils.isBlank(zkString)) {
            throw new IllegalArgumentException("Client paramter illegal.");
        }
    }

}
