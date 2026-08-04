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

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * 运维报警工具：聚合 error 计数、输出 fatal 级 JSON 结构化告警至 {@code app.alarm.log}。
 * <p>
 * ops 侧针对 {@code app.log} 收集 error 并聚合统计报警；针对 {@code app.alarm.log} 收集 JSON，
 * 按 {@code UNIQUE} 标记与报警组路由。目标是将 fatal 集中至 {@link AlarmLog} 并以 JSON 便于解析。
 */
@Log4j2
public class AlarmUtil {

    /** 集群/环境后缀，拼接在报警组标识后（如 {@code pmprod}）。 */
    public static String clusterSuffix = "";

    /** 固定窗口 error 计数缓存：外层按 {@link Interval} 分桶，内层按 message 模板计数。 */
    private static final LoadingCache<Interval, LoadingCache<String, AtomicInteger>> ERROR_CACHE =
            Caffeine.newBuilder().build(k -> {
                return Caffeine.newBuilder().expireAfterWrite(k.interval, k.timeUnit)
                        .build(s -> new AtomicInteger(0));
            });
    /** 匹配消息中 {@code UNIQUE} 标记（不区分大小写）。 */
    private static final Pattern UNIQUE_PATTERN = Pattern.compile("\\bUNIQUE\\b", Pattern.CASE_INSENSITIVE);
    /** 已知报警组短码集合。 */
    private static final Set<String> ALL_GROUPS = Set.of(
            "pm", "op", "mk", "rd", "td", "ad", "pr", "fm", "qa", "cs", "frt", "and", "ios"
    );
    /** 从 {@code [group1,group2]} 方括号片段提取报警组。 */
    private static final Pattern EXTRACT_GROUP_PATTERN = Pattern.compile("\\[(.*?)]");

    /**
     * 累计 error 并在默认阈值（5 次 / 5 分钟）超限时升级为 fatal。
     *
     * @param message 日志消息模板
     * @param params  占位符参数
     */
    public static void errorAccumulatedFatal(String message, Object... params) {
        errorAccumulatedFatal(5, 5, TimeUnit.MINUTES, message, params);
    }

    /**
     * 累计 error，在指定时间窗口内超过阈值时输出 fatal 并重置计数。
     *
     * @param threshold 触发 fatal 的次数阈值
     * @param interval  时间窗口长度
     * @param timeUnit  时间窗口单位
     * @param message   日志消息模板
     * @param params    占位符参数
     */
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

    /**
     * 输出 fatal 日志并写入 {@link AlarmLog} JSON 结构化告警（自动解析 UNIQUE 与报警组、附带 traceId/spanId）。
     *
     * @param message 消息模板
     * @param params  占位符参数
     */
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

    /**
     * 从当前 Observation 提取 traceId 与 spanId；不可用时返回空字符串对。
     *
     * @return (traceId, spanId)
     */
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

    /**
     * 显式指定报警组与 UNIQUE 标记输出 fatal 结构化告警。
     *
     * @param message 消息模板
     * @param groups  目标报警组集合
     * @param unique  是否标记为 UNIQUE（不聚合）
     * @param params  占位符参数
     */
    public static void fatal(String message, Set<String> groups, Boolean unique, Object... params) {
        final String messageResult = groups.toString() + " " + (unique ? "UNIQUE " : "") + message;
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

    /**
     * 流式构建报警组集合，{@link #add(String)} 自动附加 {@link AlarmUtil#clusterSuffix} 后缀。
     */
    public static class Group extends HashSet<String> {

        /** 集群后缀，写入每条组标识。 */
        @JsonIgnore
        private final String cluster;

        private Group(String cluster) {
            this.cluster = cluster;
        }

        /**
         * 指定集群后缀创建构建器。
         *
         * @param cluster 集群后缀
         * @return Group 构建器
         */
        public static Group builder(String cluster) {
            return new Group(cluster);
        }

        /**
         * 使用 {@link AlarmUtil#clusterSuffix} 作为后缀创建构建器。
         *
         * @return Group 构建器
         */
        public static Group builder() {
            return new Group(clusterSuffix);
        }

        @Override
        public boolean add(String string) {
            return super.add(string+cluster);
        }

        /** 添加产品（pm）组。 */
        public Group pm() {
            add("pm");
            return this;
        }

        /** 添加运营（op）组。 */
        public Group op() {
            add("op");
            return this;
        }

        /** 添加市场（mk）组。 */
        public Group mk() {
            add("mk");
            return this;
        }

        /** 添加研发（rd）组。 */
        public Group rd() {
            add("rd");
            return this;
        }

        /** 添加测试（td）组。 */
        public Group td() {
            add("td");
            return this;
        }

        /** 添加广告（ad）组。 */
        public Group ad() {
            add("ad");
            return this;
        }

        /** 添加产品（pr）组。 */
        public Group pr() {
            add("pr");
            return this;
        }
    }

    /**
     * 检测消息是否包含 {@code UNIQUE} 标记。
     *
     * @param searchString 待检消息
     * @return 是否含 UNIQUE
     */
    public static Boolean hasUnique(String searchString) {
        Matcher matcher = UNIQUE_PATTERN.matcher(searchString);
        if (matcher.find()) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    /**
     * 从消息首个 {@code [group,...]} 片段解析报警组（支持精确与前后缀模糊匹配）。
     *
     * @param searchString 待检消息
     * @return 解析到的报警组集合（含 {@link AlarmUtil#clusterSuffix} 后缀）
     */
    public static Set<String> extractGroup(String searchString) {
        Matcher matcher = EXTRACT_GROUP_PATTERN.matcher(searchString);

        Set<String> values = new HashSet<>();

        while (matcher.find()) {
            boolean find = false;
            String content = matcher.group(1);
            for (String s : content.split(",")) {
                s = s.trim().toLowerCase();
                if (ALL_GROUPS.contains(s)) {
                    find = true;
                    values.add(s + clusterSuffix);
                } else {
                    for (String group : ALL_GROUPS) {
                        if (s.startsWith(group) || s.endsWith(group)) {
                            find = true;
                            values.add(s);
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

    /** 结构化告警 JSON 输出专用 Logger（{@code app.alarm.log}）。 */
    @Log4j2
    public static class AlarmLog {

    }

    /** 带链路信息的告警 trace 日志 Logger。 */
    @Log4j2
    public static class AlarmTraceLog {

    }

    /** 写入 {@link AlarmLog} 的 JSON 告警载荷。 */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmLogContent {
        /** 是否为 UNIQUE（不参与 ops 聚合）。 */
        private Boolean unique;

        /** 目标报警组集合。 */
        private Set<String> group;

        /** 格式化后的完整告警正文。 */
        private String content;

        /** 原始消息模板。 */
        private String template;

        /** 关联 traceId。 */
        private String traceId;

        /** 关联 spanId。 */
        private String spanId;

        /** 告警产生时间。 */
        @Builder.Default
        private LocalDateTime localDateTime = LocalDateTime.now();
    }

    /** error 累计窗口键（interval + timeUnit）。 */
    @Data
    @Builder
    @AllArgsConstructor
    private static final class Interval {
        /** 窗口长度。 */
        private final long interval;
        /** 窗口时间单位。 */
        private final TimeUnit timeUnit;
    }

}
