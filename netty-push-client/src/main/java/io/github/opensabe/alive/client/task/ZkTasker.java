package io.github.opensabe.alive.client.task;

import io.github.opensabe.alive.client.impl.AliveServerList;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        if(!CollectionUtils.isEmpty(serverLists)){
            for(AliveServerList aliveServerList:serverLists){
                aliveServerList.refreshServerList();
            }
        }
        log.info("refreshConfig end serverLists size:{},cost time{}",serverLists.size(),(System.currentTimeMillis()-startTime)/1000);
    }
}
