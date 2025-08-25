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


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.opensabe.common.utils.SpringUtil;
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

import java.time.Duration;
import java.util.Objects;

@Plugin(name = "SecretCheckFilter", category = "Core", elementType = "filter", printObject = true)
public class Log4j2SecretCheckFilter extends AbstractFilter {
    private final Cache<String, Boolean> hasSecretCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).build();
    @PluginFactory
    public static Log4j2SecretCheckFilter createFilter() {
        return new Log4j2SecretCheckFilter();
    }
    @Override
    public Result filter(LogEvent event) {
        //对于异步日志，这里是单线程执行的，所以不能有太大消耗，不能每次都检查
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

