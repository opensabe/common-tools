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

/**
 * 敏感信息提供者抽象基类：应用就绪后立即加载并在固定间隔刷新密钥快照至 {@link GlobalSecretManager}。
 */
@Log4j2
public abstract class SecretProvider implements ApplicationListener<ApplicationReadyEvent> {
    private final GlobalSecretManager globalSecretManager;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private volatile boolean isScheduled = false;

    /**
     * @param globalSecretManager 全局密钥管理器
     */
    protected SecretProvider(GlobalSecretManager globalSecretManager) {
        this.globalSecretManager = globalSecretManager;
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("secret-reload-" + name())
                .setUncaughtExceptionHandler((t, e) -> {
                    log.error("SecretProvider: secret reload error {}", e.getMessage(), e);
                })
                .build());
    }

    /**
     * Provider 唯一名称，用作 {@link GlobalSecretManager} 中的分区键。
     *
     * @return Provider 名称
     */
    protected abstract String name();

    /**
     * 定时刷新间隔数值。
     *
     * @return 间隔长度
     */
    protected abstract long reloadTimeInterval();

    /**
     * 定时刷新间隔单位。
     *
     * @return 时间单位
     */
    protected abstract TimeUnit reloadTimeIntervalUnit();

    /**
     * 重新加载敏感值快照。
     *
     * @return 配置键到敏感值集合的映射
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

    /** 执行一次 reload 并写入 {@link GlobalSecretManager}。 */
    private void reloadSecret() {
        String name = name();
        log.info("SecretProvider-reloadSecret: reload secret {}", name);
        Map<String, Set<String>> reload = reload();
        globalSecretManager.putSecret(name, reload);
        log.info("SecretProvider-reloadSecret: reload secret {} success, size: {}", name, reload.size());
    }
}
