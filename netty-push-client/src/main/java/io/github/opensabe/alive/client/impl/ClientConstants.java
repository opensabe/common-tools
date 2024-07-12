package io.github.opensabe.alive.client.impl;


public class ClientConstants {

    /**
     * zk启动延迟最大时间，默认1000ms
     */
    public static final int ZK_MAX_DELAY = 1000;
    /**
     * zk调用失败重试次数，默认3
     */
    public static int ZK_RETRY_MAX = 3;
    public static int ZK_TIMEOUT = 1000;
    public static String ZK_PATH = "/nettypush_clientServer/host";
    public static long DEFAULT_TIMEOUT = 5000L;
    /**
     * zk调用失败重试等待时间，默认100ms
     */
    public static int ZK_RETRY_INTERVAL = 500;

}
