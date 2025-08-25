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
package io.github.opensabe.springdoc.autoconf;

import io.github.opensabe.springdoc.config.CloudConfig;
import io.github.opensabe.springdoc.config.FrameworkConfig;
import io.github.opensabe.springdoc.config.GenerateConfig;
import io.github.opensabe.springdoc.responses.page.PageModelConverter;
import io.swagger.v3.core.converter.ModelConverter;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * @author heng.ma
 */
@Import({GenerateConfig.class, FrameworkConfig.class, CloudConfig.class})
@ConditionalOnBean(SpringDocConfiguration.class)
@ConditionalOnClass(ModelConverter.class)
@AutoConfiguration(before = SpringDocConfiguration.class)
public class SpringdocAutoConfiguration {
    static {
        PageModelConverter.config();
    }
}
