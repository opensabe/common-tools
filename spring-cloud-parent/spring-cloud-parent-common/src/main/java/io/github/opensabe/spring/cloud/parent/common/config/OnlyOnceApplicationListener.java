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
package io.github.opensabe.spring.cloud.parent.common.config;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

import static org.springframework.cloud.bootstrap.BootstrapApplicationListener.BOOTSTRAP_PROPERTY_SOURCE_NAME;

/**
 * 保证同一 ApplicationContext 内只执行一次的监听器基类。
 * <p>
 * 用于规避 Spring Cloud {@code RestartListener} 与多次 refresh 导致的重复触发；
 * 同时跳过 Bootstrap 上下文中的事件。
 *
 * @param <T> 监听的 Spring 应用事件类型
 */
public abstract class OnlyOnceApplicationListener<T extends ApplicationEvent> implements ApplicationListener<T> {

    /** 是否已执行过 {@link #onlyOnce(ApplicationEvent)}。 */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(T event) {
        if (isBootstrapContext(event)) {
            return;
        }
        synchronized (initialized) {
            if (initialized.get()) {
                return;
            }
            onlyOnce(event);
            initialized.set(true);
        }
    }

    /**
     * 判断事件是否来自 Bootstrap 上下文。
     *
     * @param applicationEvent 应用事件
     * @return 若为 Bootstrap 上下文则返回 {@code true}
     */
    protected boolean isBootstrapContext(T applicationEvent) {
        if (applicationEvent instanceof ApplicationContextEvent) {
            ApplicationContext applicationContext = ((ApplicationContextEvent) applicationEvent).getApplicationContext();
            if (applicationContext instanceof ConfigurableApplicationContext) {
                ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
                return context.getEnvironment().getPropertySources().contains(BOOTSTRAP_PROPERTY_SOURCE_NAME);
            }
        }
        if (applicationEvent instanceof ApplicationReadyEvent) {
            return ((ApplicationReadyEvent) applicationEvent).getApplicationContext()
                    .getEnvironment().getPropertySources().contains(BOOTSTRAP_PROPERTY_SOURCE_NAME);
        }
        return false;
    }

    /**
     * 子类实现的一次性处理逻辑。
     *
     * @param event 应用事件
     */
    protected abstract void onlyOnce(T event);
}
