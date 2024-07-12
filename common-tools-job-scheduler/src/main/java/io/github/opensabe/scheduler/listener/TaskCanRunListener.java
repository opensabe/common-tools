package io.github.opensabe.scheduler.listener;

import io.github.opensabe.scheduler.server.SchedulerServer;
import io.github.opensabe.spring.cloud.parent.common.config.OnlyOnceApplicationListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Log4j2
public class TaskCanRunListener extends OnlyOnceApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private SchedulerServer schedulerServer;

    @Override
    protected void onlyOnce(ApplicationReadyEvent event) {
        log.info("TaskCanRunListener-onlyOnce [scheduler server start]");
        schedulerServer.start();
    }
}
