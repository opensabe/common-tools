package io.github.opensabe.node.manager;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import java.util.concurrent.atomic.AtomicBoolean;

public class NodeManagerInitializeListener implements ApplicationListener<ApplicationStartedEvent>, Ordered {
    public final static int ORDER = Ordered.HIGHEST_PRECEDENCE;

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (AbstractBaseOnNodeManagerInitializer.isBootstrapContext(event)) {
            return;
        }
        //由于spring-cloud的org.springframework.cloud.context.restart.RestartListener导致同一个context触发多次
        //我个人感觉org.springframework.cloud.context.restart.RestartListener这个在spring-boot2.0.0之后的spring-cloud版本是没有必要存在的
        //但是官方并没有正面回应，以防之后官方还拿这个做点事情，这里我们做个适配，参考我问的这个issue：https://github.com/spring-cloud/spring-cloud-commons/issues/693
        synchronized (INITIALIZED) {
            if (INITIALIZED.get()) {
                return;
            }
            NodeManager nodeManager = event.getApplicationContext().getBean(NodeManager.class);
            //每个spring-cloud应用只能初始化一次
            nodeManager.init();
            INITIALIZED.set(true);
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
