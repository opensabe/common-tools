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
package io.github.opensabe.node.manager;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class NodeManager {
    //private RestTemplate restTemplate = createRestTemplate();
    public static final String PATH = "/actuator/" + NodeInfoActuator.PATH;
    private final RedissonClient redissonClient;

//    private RestTemplate createRestTemplate() {
//        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
//        httpRequestFactory.setConnectionRequestTimeout(3000);
//        httpRequestFactory.setConnectTimeout(500);
//        httpRequestFactory.setReadTimeout(2500);
//        return new RestTemplate(httpRequestFactory);
//    }
    private final StringRedisTemplate redisTemplate;
    private final String serviceId;
    private final String instanceId;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    @Getter
    private volatile int nodeId = -1;

    public NodeManager(
            RedissonClient redissonClient,
            StringRedisTemplate redisTemplate,
            String serviceId,
            String instanceId
    ) {
        this.redissonClient = redissonClient;
        this.redisTemplate = redisTemplate;
        this.serviceId = serviceId;
        this.instanceId = instanceId;
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    }

    private String getLockName() {
        return "spring:node:manager:lock:" + serviceId;
    }

    private String getKey(int nodeId) {
        return "spring:node:manager:" + serviceId + ":" + nodeId;
    }

    /**
     * 在所有bean加载完毕后，，初始化
     */
    void init() {
        try {
            lock();
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                String key = getKey(i);
                String s = redisTemplate.opsForValue().get(key);
                if (StringUtils.isNotBlank(s)) {
                    log.info("node {} is occupied by {}", i, s);
                } else {
                    Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(key, instanceId, 10, TimeUnit.SECONDS);
                    if (ifAbsent) {
                        nodeId = i;
                        log.info("successfully occupied {}->{}", i, instanceId);
                        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
                            try {
                                redisTemplate.opsForValue().set(key, instanceId, 10, TimeUnit.SECONDS);
                            } catch (Throwable e) {
                                log.warn("reset node manager key: {} error", nodeId, e.getMessage());
                            }
                        }, 1, 1, TimeUnit.SECONDS);
                        return;
                    }
                }
            }
            throw new IllegalStateException("cannot get node id");
        } finally {
            unLock();
        }
    }

    private void lock() {
        RLock lock = redissonClient.getLock(getLockName());
        log.info("try to acquire node lock");
        lock.lock(1, TimeUnit.MINUTES);
        log.info("node lock acquired");

    }

    private void unLock() {
        RLock lock = redissonClient.getLock(getLockName());
        log.info("try to release node lock");
        lock.unlock();
        log.info("node lock released");
    }

    @NoArgsConstructor
    @Data
    private static class NodeResponseVO {
        /**
         * bizCode : 10000
         * innerMsg : success
         * message : success
         * data : 1
         */
        private int bizCode;
        private String innerMsg;
        private String message;
        private int data;
    }

//    public static void main(String args[]) {
//        byte[] symbol = "0⃣1⃣2⃣".getBytes();
//        String code = "";
//        for (byte b : symbol) {
//            code += "\\x" + Integer.toHexString(b & 0xff);
//        }
//        System.out.println(code);
////        Character.isSurrogatePair(0, 0)
//    }
}
