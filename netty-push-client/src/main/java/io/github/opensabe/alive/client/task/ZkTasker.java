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
package io.github.opensabe.alive.client.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import io.github.opensabe.alive.client.impl.AliveServerList;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * Author: duchaoqun
 * Date: 2019/8/29 0029 16:37
 */

@Data
@Log4j2
@EnableScheduling
public class ZkTasker {

    private List<AliveServerList> serverLists = Collections.synchronizedList(new ArrayList<>());

    /**
     * 定时刷新zk节点
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void refreshConfig() {
        long startTime = System.currentTimeMillis();
        if (!CollectionUtils.isEmpty(serverLists)) {
            for (AliveServerList aliveServerList : serverLists) {
                aliveServerList.refreshServerList();
            }
        }
        log.info("refreshConfig end serverLists size:{},cost time{}", serverLists.size(), (System.currentTimeMillis() - startTime) / 1000);
    }
}
