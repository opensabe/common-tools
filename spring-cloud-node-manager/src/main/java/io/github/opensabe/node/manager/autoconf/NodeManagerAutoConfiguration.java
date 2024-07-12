package io.github.opensabe.node.manager.autoconf;

import io.github.opensabe.node.manager.NodeManagerInitializeListener;
import io.github.opensabe.node.manager.config.NodeManagerConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({
        NodeManagerConfiguration.class
})
@AutoConfiguration
public class NodeManagerAutoConfiguration {

    @Bean
    public NodeManagerInitializeListener nodeManagerInitializeListener() {
        return new NodeManagerInitializeListener();
    }
}
