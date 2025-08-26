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
package io.github.opensabe.scheduler.health;

import io.github.opensabe.scheduler.conf.SimpleTask;
import io.github.opensabe.scheduler.job.SimpleJob;

@SimpleTask(cron = "0/10 * * * * ? ")
public class HealthCheckJob implements SimpleJob {
    private final SimpleJobHealthService simpleJobHealthService;

    public HealthCheckJob(SimpleJobHealthService simpleJobHealthService) {
        this.simpleJobHealthService = simpleJobHealthService;
    }

    @Override
    public void execute() {
        simpleJobHealthService.setHealth();
    }
}
