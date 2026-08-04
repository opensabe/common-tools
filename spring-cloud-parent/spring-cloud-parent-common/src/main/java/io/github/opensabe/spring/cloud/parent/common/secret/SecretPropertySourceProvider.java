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
package io.github.opensabe.spring.cloud.parent.common.secret;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import lombok.extern.log4j.Log4j2;

/**
 * Secret 属性源脱敏 Provider。
 * <p>
 * 从已解密的 {@link org.springframework.core.env.MapPropertySource} 收集敏感值，
 * 供 {@link io.github.opensabe.common.secret.GlobalSecretManager} 过滤日志与告警。
 *
 * @author maheng
 */
@Log4j2
public class SecretPropertySourceProvider extends SecretProvider implements EnvironmentAware {

    /** 当前应用 Environment。 */
    private ConfigurableEnvironment environment;

    /**
     * @param globalSecretManager 全局密钥管理器
     */
    public SecretPropertySourceProvider(GlobalSecretManager globalSecretManager) {
        super(globalSecretManager);
    }


    /** {@inheritDoc} */
    @Override
    protected String name() {
        return "secretPropertiesTablePropertiesProvider";
    }

    /** {@inheritDoc}；Secret 属性变更频率低，间隔 99 分钟。 */
    @Override
    protected long reloadTimeInterval() {
        return 99;
    }

    /** {@inheritDoc} */
    @Override
    protected TimeUnit reloadTimeIntervalUnit() {
        return TimeUnit.MINUTES;
    }

    /**
     * 从已解密的 MapPropertySource 收集敏感值集合。
     *
     * @return 属性源名称 → 敏感值集合
     */
    @Override
    protected Map<String, Set<String>> reload() {
        Map<String, Set<String>> map = new HashMap<>(2);
        if (Objects.nonNull(environment)) {
            MutablePropertySources mutablePropertySources = environment.getPropertySources();
            for (PropertySource<?> propertySource : mutablePropertySources) {
                //支持 多个secretPropertySource: bootstrapProperties-secretPropertySource-application-profile
                if (propertySource.getName().startsWith(SecretPropertySourceResolver.SECRET_PROPERTY_SOURCE_NAME)) {
                    //这里必须跟MapPropertySource比较，如果直接跟EnumerablePropertySource比较，可能会添加上未解密的
                    if (propertySource instanceof MapPropertySource mapPropertySource) {
                        Set<String> set = mapPropertySource.getSource().values()
                                .stream()
                                .filter(Objects::nonNull)
                                .map(Object::toString)
                                .filter(StringUtils::isNotBlank)
                                .collect(Collectors.toSet());
                        map.put(propertySource.getName(), set);
                    }
                }
            }
        }else {
            log.warn("SecretPropertySourceProvider reload error, environment is not ConfigurableEnvironment instance");
        }
        return map;
    }

    /** 注入 ConfigurableEnvironment。 */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment instanceof ConfigurableEnvironment configurableEnvironment ? configurableEnvironment : null;
    }
}
