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
package io.github.opensabe.spring.cloud.parent.web.common.test;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

@Testcontainers
public class CommonMicroServiceTest {

    private static final Network network = Network.newNetwork();

    private static final String HTTPBIN = "httpbin";
    private static final int HTTPBIN_PORT = 8080;
    private static final GenericContainer<?> HTTPBIN_CONTAINER
            = new GenericContainer<>("mccutchen/go-httpbin")
            .withExposedPorts(HTTPBIN_PORT)
            .withNetwork(network)
            .withNetworkAliases(HTTPBIN);
    /**
     * <a href="https://java.testcontainers.org/modules/toxiproxy/">toxiproxy</a>
     * 使用 toxiproxy 封装 httpbin
     * 可以使用 toxiproxy 模拟网络故障等情况
     * 可以用的 port 范围是 8666～8697
     */
    private static final ToxiproxyContainer TOXIPROXY_CONTAINER = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0")
            .withNetwork(network);

    private static final int GOOD_HTTPBIN_PROXY_PORT = 8666;
    private static final int READ_TIMEOUT_HTTPBIN_PROXY_PORT = 8667;
    private static final int RESET_PEER_HTTPBIN_PROXY_PORT = 8668;

    public static final String GOOD_HOST;
    public static final int GOOD_PORT;
    /**
     * 以下代表请求已经发出到服务端，但是响应超时，或者不能响应（比如服务器重启）
     */
    public static final String READ_TIMEOUT_HOST;
    public static final int READ_TIMEOUT_PORT;
    public static final String RESET_PEER_HOST;
    public static final int RESET_PEER_PORT;

    /**
     * 以下代表请求都没有发出去，TCP 链接都没有建立
     */
    public static final String CONNECT_TIMEOUT_HOST = "localhost";
    /**
     * 端口一定连不上的
     */
    public static final int CONNECT_TIMEOUT_PORT = 9999;


    static {
        //不使用 @Container 注解管理容器声明周期，因为我们需要在静态块生成代理，必须在这之前启动容器
        //不用担心容器不会被关闭，因为 testcontainers 会启动一个 ryuk 容器，用于监控并关闭所有容器
        HTTPBIN_CONTAINER.start();
        TOXIPROXY_CONTAINER.start();
        final ToxiproxyClient toxiproxyClient = new ToxiproxyClient(TOXIPROXY_CONTAINER.getHost(), TOXIPROXY_CONTAINER.getControlPort());
        try {
            Proxy proxy = toxiproxyClient.createProxy("good", "0.0.0.0:" + GOOD_HTTPBIN_PROXY_PORT, HTTPBIN + ":" + HTTPBIN_PORT);
            //关闭流量，会 READ TIME OUT
            proxy = toxiproxyClient.createProxy("read_timeout", "0.0.0.0:" + READ_TIMEOUT_HTTPBIN_PROXY_PORT, HTTPBIN + ":" + HTTPBIN_PORT);
            proxy.toxics().bandwidth("UP_DISABLE", ToxicDirection.UPSTREAM, 0);
            proxy.toxics().bandwidth("DOWN_DISABLE", ToxicDirection.DOWNSTREAM, 0);
            proxy = toxiproxyClient.createProxy("connect_timeout", "0.0.0.0:" + RESET_PEER_HTTPBIN_PROXY_PORT, HTTPBIN + ":" + HTTPBIN_PORT);
            proxy.toxics().resetPeer("UP_SLOW_CLOSE", ToxicDirection.UPSTREAM, 1);
            proxy.toxics().resetPeer("DOWN_SLOW_CLOSE", ToxicDirection.DOWNSTREAM, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GOOD_HOST = TOXIPROXY_CONTAINER.getHost();
        GOOD_PORT = TOXIPROXY_CONTAINER.getMappedPort(GOOD_HTTPBIN_PROXY_PORT);
        READ_TIMEOUT_HOST = TOXIPROXY_CONTAINER.getHost();
        READ_TIMEOUT_PORT = TOXIPROXY_CONTAINER.getMappedPort(READ_TIMEOUT_HTTPBIN_PROXY_PORT);
        RESET_PEER_HOST = TOXIPROXY_CONTAINER.getHost();
        RESET_PEER_PORT = TOXIPROXY_CONTAINER.getMappedPort(RESET_PEER_HTTPBIN_PROXY_PORT);
    }
}
