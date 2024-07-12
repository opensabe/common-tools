package io.github.opensabe.scheduler;

import io.github.opensabe.scheduler.conf.SimpleTask;
import io.github.opensabe.scheduler.job.SimpleJob;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@SimpleTask(cron = "0/1 * * * * ?")
public class TempleTask implements SimpleJob {
    public volatile boolean run = false;
    @Override
    public void execute() {
       log.info("----------");
       run = true;
    }
}
