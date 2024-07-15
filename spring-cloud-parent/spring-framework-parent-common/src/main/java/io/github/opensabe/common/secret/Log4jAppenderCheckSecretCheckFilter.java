package io.github.opensabe.common.secret;

import io.github.opensabe.common.utils.AlarmUtil;
import lombok.extern.log4j.Log4j2;
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

import java.util.Map;

/**
 * 检查 Log4j2 的 Appender 是否包含指定的 Filter
 */
@Log4j2
public class Log4jAppenderCheckSecretCheckFilter implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        // 要检查的 Filter 类
        Class<?> filterClass = Log4j2SecretCheckFilter.class;

        // 检查根 Logger
        LoggerConfig rootLoggerConfig = config.getRootLogger();
        checkFilter(rootLoggerConfig, filterClass);

        // 检查所有其他 Logger
        for (LoggerConfig loggerConfig : config.getLoggers().values()) {
            checkFilter(loggerConfig, filterClass);
        }
    }

    private static boolean containsFilter(Filter filter, Class<?> filterClass) {
        if (filter == null) {
            return false;
        }

        if (filterClass.isInstance(filter)) {
            return true;
        }

        // 检查子过滤器
        if (filter instanceof CompositeFilter compositeFilter) {
            for (Filter subFilter : compositeFilter.getFiltersArray()) {
                if (containsFilter(subFilter, filterClass)) {
                    return true;
                }
            }
        }
        return false;
    }

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
}
