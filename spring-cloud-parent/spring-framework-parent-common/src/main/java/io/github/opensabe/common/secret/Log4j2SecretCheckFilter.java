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


import java.time.Duration;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.springframework.context.ApplicationContext;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.opensabe.common.utils.SpringUtil;

/**
 * Log4j2 核心 Filter：在日志落盘前检测并拦截含敏感信息的输出。
 * <p>
 * 异步日志场景下 filter 在单线程执行，故对相同格式化消息使用 1 分钟本地缓存以降低重复扫描开销。
 */
@Plugin(name = "SecretCheckFilter", category = "Core", elementType = "filter", printObject = true)
public class Log4j2SecretCheckFilter extends AbstractFilter {
    /** 按格式化消息缓存是否含敏感信息，避免每条日志全量扫描。 */
    private final Cache<String, Boolean> hasSecretCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).build();

    /**
     * Log4j2 插件工厂方法。
     *
     * @return Filter 实例
     */
    @PluginFactory
    public static Log4j2SecretCheckFilter createFilter() {
        return new Log4j2SecretCheckFilter();
    }

    @Override
    public Result filter(LogEvent event) {
        String format = event.getMessage().getFormattedMessage();
        if (format == null) {
            return Result.NEUTRAL;
        }
        Boolean ifPresent = hasSecretCache.getIfPresent(format);
        if (Objects.nonNull(ifPresent)) {
            if (ifPresent) {
                return check(event.getMessage().getFormattedMessage());
            } else {
                return Result.NEUTRAL;
            }
        }
        Result check = check(event.getMessage().getFormattedMessage());
        hasSecretCache.put(format, check == Result.DENY);
        return check;
    }

    /**
     * 对单条消息文本执行敏感信息检查。
     *
     * @param message 日志消息
     * @return {@link Result#DENY} 含敏感信息；否则 {@link Result#NEUTRAL}
     */
    private Result check(String message) {
        if (StringUtils.isEmpty(message)) {
            return Result.NEUTRAL;
        }
        ApplicationContext applicationContext = SpringUtil.getApplicationContext();
        if (applicationContext == null) {
            return Result.NEUTRAL;
        }
        GlobalSecretManager globalSecretManager = applicationContext.getBean(GlobalSecretManager.class);
        FilterSecretStringResult filterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(message);
        if (filterSecretStringResult.isFoundSensitiveString()) {
            return Result.DENY;
        }
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return check(Objects.toString(msg));
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        Result check = check(msg);
        if (check == Result.DENY) {
            return check;
        }
        for (Object param : params) {
            Result check1 = check(Objects.toString(param));
            if (check1 == Result.DENY) {
                return check1;
            }
        }
        return super.filter(logger, level, marker, msg, params);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        Result check = check(msg.getFormattedMessage());
        if (check == Result.DENY) {
            return check;
        }
        check = check(t == null ? null : t.getMessage());
        if (check == Result.DENY) {
            return check;
        }
        return super.filter(logger, level, marker, msg, t);
    }
}
