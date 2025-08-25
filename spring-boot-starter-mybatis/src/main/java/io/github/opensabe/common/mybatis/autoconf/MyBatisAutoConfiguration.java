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
package io.github.opensabe.common.mybatis.autoconf;

import io.github.opensabe.common.mybatis.configuration.MonitorConfiguration;
import io.github.opensabe.common.mybatis.configuration.MybatisConfiguration;
import io.github.opensabe.common.mybatis.configuration.PageHelperProperties;
import io.github.opensabe.common.mybatis.configuration.SqlSessionFactoryConfiguration;
import io.github.opensabe.common.mybatis.properties.CountryProperties;
import io.github.opensabe.common.mybatis.configuration.PageHelperAutoConfiguration;
import io.github.opensabe.common.mybatis.configuration.WebInterceptorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import tk.mybatis.mapper.autoconfigure.MybatisProperties;

@AutoConfiguration
@Import({
        MybatisConfiguration.class,
        PageHelperAutoConfiguration.class,
        SqlSessionFactoryConfiguration.class,
        WebInterceptorConfiguration.class,
        MonitorConfiguration.class
})
@EnableConfigurationProperties({MybatisProperties.class, PageHelperProperties.class, CountryProperties.class})
public class MyBatisAutoConfiguration {
}
