package io.github.opensabe.common.secret;


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

import java.util.Objects;

@Plugin(name = "SecretCheckFilter", category = "Core", elementType = "filter", printObject = true)
public class Log4j2SecretCheckFilter extends AbstractFilter {
    @PluginFactory
    public static Log4j2SecretCheckFilter createFilter() {
        return new Log4j2SecretCheckFilter();
    }
    @Override
    public Result filter(LogEvent event) {
        return check(event.getMessage().getFormattedMessage());
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

