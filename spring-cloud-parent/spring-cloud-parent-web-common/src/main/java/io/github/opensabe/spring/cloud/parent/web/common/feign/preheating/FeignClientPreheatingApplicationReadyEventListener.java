package io.github.opensabe.spring.cloud.parent.web.common.feign.preheating;

import io.github.opensabe.spring.cloud.parent.common.config.OnlyOnceApplicationListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.stream.Collectors;

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
