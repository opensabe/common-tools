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
package io.github.opensabe.spring.cloud.parent.common.system;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.opensabe.common.utils.AlarmUtil;
import io.github.opensabe.spring.cloud.parent.common.config.OnlyOnceApplicationListener;
import lombok.extern.log4j.Log4j2;

/**
 * Periodic process-list scan via /proc. Does not fork {@code ps}.
 * Allows common-tools {@code jcmd} children and configured allow lists.
 */
@Log4j2
public class MonitorProcessList extends OnlyOnceApplicationListener<ApplicationReadyEvent>
        implements DisposableBean {

    private final ProcessListMonitorProperties properties;
    private final ProcessListReader reader;
    private final ProcessListAnomalyDetector detector;
    private final Cache<String, Boolean> alarmDedup;
    private final ScheduledThreadPoolExecutor scheduler;
    private final AtomicBoolean procUnavailableWarned = new AtomicBoolean(false);

    public MonitorProcessList(ProcessListMonitorProperties properties) {
        this(properties, new ProcessListReader(), new ProcessListAnomalyDetector(properties));
    }

    MonitorProcessList(
            ProcessListMonitorProperties properties,
            ProcessListReader reader,
            ProcessListAnomalyDetector detector
    ) {
        this.properties = properties == null ? new ProcessListMonitorProperties() : properties;
        this.reader = reader;
        this.detector = detector;
        int dedupSeconds = Math.max(1, this.properties.getAlarmDedupSeconds());
        this.alarmDedup = Caffeine.newBuilder()
                .expireAfterWrite(dedupSeconds, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build();
        this.scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "monitor-process-list");
            t.setDaemon(true);
            return t;
        });
        this.scheduler.setRemoveOnCancelPolicy(true);
    }

    @Override
    protected void onlyOnce(ApplicationReadyEvent event) {
        long interval = Math.max(1L, properties.getIntervalSeconds());
        scheduler.scheduleAtFixedRate(this::safeCheck, 0L, interval, TimeUnit.SECONDS);
        log.info("MonitorProcessList-onlyOnce: scheduled process list check every {}s", interval);
    }

    @Override
    public void destroy() {
        scheduler.shutdownNow();
    }

    void safeCheck() {
        try {
            checkOnce();
        } catch (Throwable t) {
            log.error("MonitorProcessList-safeCheck: unexpected error", t);
        }
    }

    void checkOnce() throws Exception {
        if (!reader.isAvailable()) {
            if (procUnavailableWarned.compareAndSet(false, true)) {
                log.warn("MonitorProcessList-checkOnce: /proc unavailable, skip process list check");
            }
            return;
        }
        long selfPid = ProcessHandle.current().pid();
        List<ProcessSnapshot> processes = reader.readAll();
        List<String> anomalies = detector.findAnomalies(selfPid, processes);
        if (log.isDebugEnabled()) {
            String processSummary = ProcessCmdlineMasker.mask(processes.stream()
                    .map(ProcessSnapshot::toString)
                    .collect(Collectors.joining(" | ")));
            log.debug("MonitorProcessList-checkOnce: selfPid={} processCount={} processes=[{}]",
                    selfPid, processes.size(), processSummary);
        }
        if (anomalies.isEmpty()) {
            log.info("MonitorProcessList-checkOnce: status=OK selfPid={} processCount={}",
                    selfPid, processes.size());
            return;
        }
        String anomalySummary = ProcessCmdlineMasker.mask(String.join("; ", anomalies));
        log.error("MonitorProcessList-checkOnce: status=ANOMALY selfPid={} processCount={} anomalies={}",
                selfPid, processes.size(), anomalySummary);
        String fingerprint = anomalySummary.length() > 512 ? anomalySummary.substring(0, 512) : anomalySummary;
        if (alarmDedup.asMap().putIfAbsent(fingerprint, Boolean.TRUE) != null) {
            return;
        }
        AlarmUtil.fatal(
                "MonitorProcessList-checkOnce: UNIQUE suspicious process list selfPid={} anomalies={}",
                selfPid, anomalySummary);
    }
}
