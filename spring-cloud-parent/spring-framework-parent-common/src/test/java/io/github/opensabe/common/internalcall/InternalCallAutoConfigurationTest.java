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
