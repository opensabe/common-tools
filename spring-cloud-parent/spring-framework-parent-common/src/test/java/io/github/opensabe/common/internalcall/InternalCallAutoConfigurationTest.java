package io.github.opensabe.common.internalcall;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class InternalCallAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(InternalCallAutoConfiguration.class));

    @Test
    @DisplayName("enabled=false 时上下文可起")
    void disabledStarts() {
        runner.withPropertyValues("opensabe.internal-call.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(InternalCallSigner.class);
                    assertThat(context).hasSingleBean(Ipv4CidrSet.class);
                });
    }

    @Test
    @DisplayName("非法 CIDR 启动失败")
    void illegalCidrFailsStartup() {
        runner.withPropertyValues(
                "opensabe.internal-call.enabled=false",
                "opensabe.internal-call.edge.trusted-cidrs[0]=010.0.0.0/8"
        ).run(context -> assertThat(context).hasFailed());
    }
}
