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
