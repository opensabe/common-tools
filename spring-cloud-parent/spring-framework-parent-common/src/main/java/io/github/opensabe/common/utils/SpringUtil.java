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
package io.github.opensabe.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import lombok.extern.log4j.Log4j2;

/**
 * 如果之后再用 SpringUtils 这种工具，遇到 ApplicationContext 为空。那么估计是将这个 SpringUtils 放在 Bean 初始化里面用了。
 * 那么让对应的 Bean 实现 ApplicationListener<ApplicationStartedEvent>, 或者实现 ApplicationRunner，将 SpringUtils 逻辑放在里面执行就行。
 * 一些定时开始，或者 Bean 就绪之后立刻开始调用的，例如 RocketMQ 消费，可能这时候还没有初始化好所有的 Bean，那么可以阻塞直到收到 ApplicationListener<ApplicationReadyEvent>
 * <p>
 * <p>
 * Spring boot 中，事件主要包括：
 * ApplicationStartingEvent：这个是spring boot应用一开始启动时，发出的事件，只是用来标识，应用开始启动了，一般没什么用
 * ApplicationEnvironmentPreparedEvent：这个是在创建好Environment（通过上下文配置，判断到底创建StandardServletEnvironment（针对Servlet环境），StandardReactiveWebEnvironment（针对Reactive环境）还是StandardEnvironment（针对无servlet环境））之后发出的事件。
 * ApplicationContextInitializedEvent: 这个是在创建好Context并调用ApplicationContextInitializer初始化context之后发布这个事件，在加载bean信息之前
 * ApplicationPreparedEvent:加载bean信息之后，但是还没有创建bean的时候，发步这个事件。这个事件是和调用ApplicationContextAware设置ApplicationContext一起进行的，可以看出，setApplicationContext方法里面不能去获取bean，因为bean可能还没有初始化完成
 * ApplicationStartedEvent: 加载初始化各种需要的bean并依赖注入之后，在运行ApplicationRunner做一些用户自定义的初始化操作之前，会发布这个事件。
 * ApplicationReadyEvent：运行ApplicationRunner做一些用户自定义的初始化操作之后，会发布这个事件。
 *
 */
@Log4j2
public class SpringUtil implements ApplicationContextAware {

    private static volatile ApplicationContext applicationContext;

    // 获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtil.applicationContext == null) {
            SpringUtil.applicationContext = applicationContext;
            log.info("SpringUtil: setApplicationContext successful: {}", applicationContext);
        } else {
            AlarmUtil.fatal("SpringUtil: reset ApplicationContext {} -> {}", SpringUtil.applicationContext, applicationContext);
            SpringUtil.applicationContext = applicationContext;
        }
    }

    // 通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    // 通过name获取 Bean.
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    // 通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}