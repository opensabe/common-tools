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
package io.github.opensabe.common.secret;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.apache.logging.log4j.core.filter.Filterable;
import org.springframework.boot.CommandLineRunner;

import io.github.opensabe.common.utils.AlarmUtil;
import lombok.extern.log4j.Log4j2;

/**
 * 检查 Log4j2 的 Appender 是否包含指定的 Filter
 */
@Log4j2
public class Log4jAppenderCheckSecretCheckFilter implements CommandLineRunner {

    /**
     * 递归检查 Filter 树是否包含指定类型的子 Filter。
     *
     * @param filter      当前 Filter
     * @param filterClass 目标 Filter 类型
     * @return 是否包含
     */
    private static boolean containsFilter(Filter filter, Class<?> filterClass) {
        if (filter == null) {
            return false;
        }

        if (filterClass.isInstance(filter)) {
            return true;
        }

        // Check nested filters in CompositeFilter
        if (filter instanceof CompositeFilter compositeFilter) {
            for (Filter subFilter : compositeFilter.getFiltersArray()) {
                if (containsFilter(subFilter, filterClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查单个 LoggerConfig 下各 Appender 是否挂载 {@link Log4j2SecretCheckFilter}。
     *
     * @param loggerConfig Logger 配置
     * @param filterClass  期望的 Filter 类型
     */
    private static void checkFilter(LoggerConfig loggerConfig, Class<?> filterClass) {
        if (!StringUtils.containsIgnoreCase(loggerConfig.getName(), AlarmUtil.class.getName())) {
            for (Map.Entry<String, Appender> entry : loggerConfig.getAppenders().entrySet()) {
                String appenderName = entry.getKey();
                if (loggerConfig.getAppenders().containsKey(appenderName)) {
                    Appender appender = loggerConfig.getAppenders().get(appenderName);
                    if (appender instanceof Filterable) {
                        Filter filter = ((Filterable) appender).getFilter();
                        boolean containsFilter = containsFilter(filter, filterClass);
                        if (!containsFilter) {
                            AlarmUtil.fatal("Logger '{}' Appender '{}' does not contain filter '{}'", loggerConfig.getName(), appenderName, filterClass.getName());
                        }
                    }
                }
            }
        }
    }

    /**
     * 应用启动后校验所有 Logger/Appender 是否配置密钥检查 Filter。
     *
     * @param args 命令行参数（未使用）
     */
    @Override
    public void run(String... args) throws Exception {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        Class<?> filterClass = Log4j2SecretCheckFilter.class;

        LoggerConfig rootLoggerConfig = config.getRootLogger();
        checkFilter(rootLoggerConfig, filterClass);

        for (LoggerConfig loggerConfig : config.getLoggers().values()) {
            checkFilter(loggerConfig, filterClass);
        }
    }
}
