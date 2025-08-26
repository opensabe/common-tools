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
package io.github.opensabe.spring.cloud.parent.web.common.feign.preheating;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;

import io.github.opensabe.spring.cloud.parent.common.config.OnlyOnceApplicationListener;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FeignClientPreheatingApplicationReadyEventListener extends OnlyOnceApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private ApplicationContext applicationContext;

    private void feignClientPreheating() {
        Map<String, Object> feignClientMap = applicationContext.getBeansWithAnnotation(FeignClient.class).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        log.info("FeignClientPreheatingApplicationReadyEventListener-feignClientPreheating feign client object is {}. ", feignClientMap);

        if (!feignClientMap.values().isEmpty()) {
            var entrys = feignClientMap.values();
            for (var item : entrys) {
                //如果@FeignClient的接口有extends继承 预热接口FeignPreheatingBase，则启动预热方法
                if (item instanceof FeignPreheatingBase) {
                    try {
                        //调用item (feignclient的proxy实体) 转化FeignPreheatingBase 调用heartbeat 预热
                        String result = ((FeignPreheatingBase) item).heartbeat();
                        log.info("FeignClientPreheatingApplicationReadyEventListener-feignClientPreheating preheating feign client object is {} " +
                                "and result is {}. ", item, result);
                    } catch (Throwable t) {
                        log.info("FeignClientPreheatingApplicationReadyEventListener-feignClientPreheating preheating feign client fails,which is {} " +
                                "and exception is {}. ", item, t);
                    }
                }
            }
        }
    }

    @Override
    protected void onlyOnce(ApplicationReadyEvent event) {
        //feign client preheating
        feignClientPreheating();
    }
}
