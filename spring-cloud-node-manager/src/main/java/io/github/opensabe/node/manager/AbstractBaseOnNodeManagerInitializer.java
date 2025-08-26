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
package io.github.opensabe.node.manager;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import static org.springframework.cloud.bootstrap.BootstrapApplicationListener.BOOTSTRAP_PROPERTY_SOURCE_NAME;


public abstract class AbstractBaseOnNodeManagerInitializer implements ApplicationListener<ApplicationStartedEvent>, Ordered {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    static boolean isBootstrapContext(ApplicationStartedEvent applicationEvent) {
        return applicationEvent.getApplicationContext().getEnvironment().getPropertySources().contains(BOOTSTRAP_PROPERTY_SOURCE_NAME);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (isBootstrapContext(event)) {
            return;
        }
        //由于spring-cloud的org.springframework.cloud.context.restart.RestartListener导致同一个context触发多次
        //我个人感觉 org.springframework.cloud.context.restart.RestartListener 这个在spring-boot2.0.0之后的spring-cloud版本是没有必要存在的
        //但是官方并没有正面回应，以防之后官方还拿这个做点事情，这里我们做个适配，参考我问的这个issue：https://github.com/spring-cloud/spring-cloud-commons/issues/693
        synchronized (INITIALIZED) {
            if (INITIALIZED.get()) {
                return;
            }
            //每个spring-cloud应用只能初始化一次
            init();
            INITIALIZED.set(true);
        }
    }

    protected abstract void init();

    @Override
    public int getOrder() {
        //一定要在NodeManagerInitializeListener之后，否则NodeManager的NodeManager可能还没有初始化完成
        return NodeManagerInitializeListener.ORDER + 1;
    }
}
