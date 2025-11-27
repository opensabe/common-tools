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
package io.github.opensabe.common.utils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import it.unimi.dsi.fastutil.Pair;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * 目前 ops 报警，针对 app.log 仅收集 error 级别，聚合统计个数报警到 rd
 * 针对 app.alarm.log 收集所有 json，根据 unique 判断是否聚合，报警到对应报警组
 * 我们的目标是：
 * 1. 以后去掉 fatal 级别，第一步是先将 fatal 集中起来以后一起改，目前全部集中到了 AlarmLog.log.fatal，其实现在就能去掉，级别也没有打印出
 * 2. app.log 中聚合 error 的逻辑，放在后台自己控制，同时针对不同报警设置不同的限制
 * 3. 原有的 fatal 输出到 app.alarm.log, 并且格式是 json，便于 ops 解析
 */
@Log4j2
public class AlarmUtil {

    //实现一个近似于固定窗口（非滑动窗口）的报警，即报警第一次出现之后 Interval 时间内如果超过多少次就会报警，报警后清空
    private static final LoadingCache<Interval, LoadingCache<String, AtomicInteger>> ERROR_CACHE =
            Caffeine.newBuilder().build(k -> {
                return Caffeine.newBuilder().expireAfterWrite(k.interval, k.timeUnit)
                        .build(s -> new AtomicInteger(0));
            });
    private static final Pattern UNIQUE_PATTERN = Pattern.compile("\\bUNIQUE\\b", Pattern.CASE_INSENSITIVE);
    private static final Set<String> ALL_GROUPS = Set.of(
            "pm", "op", "mk", "rd", "td", "ad", "pr"
    );
    private static final Pattern EXTRACT_GROUP_PATTERN = Pattern.compile("\\[(.*?)\\]");

    /**
     * 默认 5 分钟内超过 5 次就会报警
     *
     * @param message
     * @param params
     */
    public static void errorAccumulatedFatal(String message, Object... params) {
        errorAccumulatedFatal(5, 5, TimeUnit.MINUTES, message, params);
    }

    public static void errorAccumulatedFatal(int threshold, long interval, TimeUnit timeUnit, String message, Object... params) {
        LoadingCache<String, AtomicInteger> cache = ERROR_CACHE.get(Interval.builder().interval(interval).timeUnit(timeUnit).build());
        AtomicInteger atomicInteger = cache.get(message);
        int count = atomicInteger.incrementAndGet();
        if (count > threshold) {
            atomicInteger.set(0);
            log.warn("AlarmUtil-errorAccumulatedFatal: Threshold {}[{}{}] reached, therefore output as fatal", threshold, interval, timeUnit);
            fatal("[AlarmThreshold: {} times in {} {}]" + message, threshold, interval, timeUnit, params);
        } else {
            log.warn("[AlarmUtil-errorAccumulatedFatal: Threshold {}[{}{}] not reached, therefore output as warn not as fatal or error]" + message, threshold, interval, timeUnit, params);
        }
    }

    public static void fatal(String message, Object... params) {
        log.fatal(message, params);
        Pair<String, String> traceSpan = analysisTraceSpan();

        MessageFactory messageFactory = AlarmLog.log.getMessageFactory();
        Message messageAlarmLog = messageFactory.newMessage(message, params);
        String formattedMessage = messageAlarmLog.getFormattedMessage();
        AlarmLogContent alarmLogContent = AlarmLogContent
                .builder()
                .unique(hasUnique(formattedMessage))
                .group(extractGroup(formattedMessage))
                .content(formattedMessage)
                .traceId(traceSpan.left())
                .spanId(traceSpan.right())
                .template(message)
                .build();
        AlarmLog.log.fatal(JsonUtil.toJSONString(alarmLogContent));
    }

    private static Pair<String, String> analysisTraceSpan() {
        try {
            UnifiedObservationFactory unifiedObservationFactory = SpringUtil.getBean(UnifiedObservationFactory.class);
            if (Objects.nonNull(unifiedObservationFactory)) {
                Observation currentObservation = unifiedObservationFactory.getCurrentObservation();
                if (Objects.nonNull(currentObservation)) {
                    TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentObservation);
                    return Pair.of(traceContext.traceId(), traceContext.spanId());
                }
            }
        } catch (Throwable throwable) {

        }

        return Pair.of("", "");
    }

    public static void fatal(String message, Set<String> groups, Boolean unique, Object... params) {
        final String messageResult = groups.toString() + " " + (unique ? "UNIQUE" : "") + message;
        log.fatal(messageResult, params);
        Pair<String, String> traceSpan = analysisTraceSpan();
        MessageFactory messageFactory = AlarmLog.log.getMessageFactory();
        Message messageAlarmLog = messageFactory.newMessage(message, params);
        String formattedMessage = messageAlarmLog.getFormattedMessage();
        AlarmLogContent alarmLogContent = AlarmLogContent
                .builder()
                .unique(unique)
                .group(groups)
                .content(formattedMessage)
                .traceId(traceSpan.left())
                .spanId(traceSpan.right())
                .template(message)
                .build();
        AlarmLog.log.fatal(JsonUtil.toJSONString(alarmLogContent));
    }

    public static Boolean hasUnique(String searchString) {
        // 创建 Matcher 对象
        Matcher matcher = UNIQUE_PATTERN.matcher(searchString);
        // 进行匹配
        if (matcher.find()) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public static Set<String> extractGroup(String searchString) {
        // 创建 Matcher 对象
        Matcher matcher = EXTRACT_GROUP_PATTERN.matcher(searchString);

        // 创建一个集合来存放提取的值
        Set<String> values = new HashSet<>();

        // 查找匹配的内容并提取每个方括号中的值，直到匹配
        while (matcher.find()) {
            // 获取匹配到的内容（去掉方括号）
            boolean find = false;
            String content = matcher.group(1);
            // 根据逗号分割内容并放入集合中
            for (String s : content.split(",")) {
                s = s.trim().toLowerCase();
                // 如果匹配上组，则加入，并且标记找到了
                if (ALL_GROUPS.contains(s)) {
                    find = true;
                    values.add(s);
                } else {
                    // 尝试部分匹配，可以匹配到比如 project1pm，project2op 这种
                    for (String group : ALL_GROUPS) {
                        if (s.contains(group)) {
                            find = true;
                            values.add(group);
                            break;
                        }
                    }
                }
            }
            if (find) {
                break;
            }
        }
        return values;
    }

    @Log4j2
    public static class AlarmLog {

    }

    @Log4j2
    public static class AlarmTraceLog {

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmLogContent {
        private Boolean unique;

        private Set<String> group;

        private String content;

        private String template;

        private String traceId;

        private String spanId;

        @Builder.Default
        private LocalDateTime localDateTime = LocalDateTime.now();
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static final class Interval {
        private final long interval;
        private final TimeUnit timeUnit;
    }
}
