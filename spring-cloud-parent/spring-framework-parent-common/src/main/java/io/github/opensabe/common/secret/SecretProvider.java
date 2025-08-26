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
package io.github.opensabe.common.secret;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class SecretProvider implements ApplicationListener<ApplicationReadyEvent> {
    private final GlobalSecretManager globalSecretManager;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private volatile boolean isScheduled = false;

    protected SecretProvider(GlobalSecretManager globalSecretManager) {
        this.globalSecretManager = globalSecretManager;
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("secret-reload-" + name())
                .setUncaughtExceptionHandler((t, e) -> {
                    log.error("SecretProvider: secret reload error {}", e.getMessage(), e);
                })
                .build());
    }

    protected abstract String name();

    protected abstract long reloadTimeInterval();

    protected abstract TimeUnit reloadTimeIntervalUnit();

    /**
     * key: secret name
     * value: secret related values
     *
     * @return
     */
    protected abstract Map<String, Set<String>> reload();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (isScheduled) {
            return;
        }
        synchronized (this) {
            if (isScheduled) {
                return;
            }
            this.reloadSecret();
            scheduledThreadPoolExecutor.scheduleAtFixedRate(this::reloadSecret, reloadTimeInterval(), reloadTimeInterval(), reloadTimeIntervalUnit());
            isScheduled = true;
        }
    }

    private void reloadSecret() {
        String name = name();
        log.info("SecretProvider-reloadSecret: reload secret {}", name);
        Map<String, Set<String>> reload = reload();
        globalSecretManager.putSecret(name, reload);
        log.info("SecretProvider-reloadSecret: reload secret {} success, size: {}", name, reload.size());
    }
}
