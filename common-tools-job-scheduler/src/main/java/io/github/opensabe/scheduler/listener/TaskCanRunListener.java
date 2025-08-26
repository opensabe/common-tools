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
package io.github.opensabe.scheduler.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import io.github.opensabe.scheduler.server.SchedulerServer;
import io.github.opensabe.spring.cloud.parent.common.config.OnlyOnceApplicationListener;
import lombok.extern.log4j.Log4j2;

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
