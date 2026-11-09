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
package io.github.opensabe.common.jfr.metrics;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

import io.micrometer.core.instrument.MeterRegistry;
import jdk.jfr.consumer.EventStream;
import jdk.jfr.consumer.RecordedClass;
import jdk.jfr.consumer.RecordedEvent;
import lombok.extern.log4j.Log4j2;

/**
 * Passive JFR consumer: {@link EventStream#openRepository()} only.
 * Never starts recordings or calls enable/disable/withPeriod/withThreshold.
 */
@Log4j2
public class JfrMetricsConsumer implements SmartLifecycle, DisposableBean {

    static final String EVENT_ALLOC_SAMPLE = "jdk.ObjectAllocationSample";
    static final String EVENT_OUTSIDE_TLAB = "jdk.ObjectAllocationOutsideTLAB";
    static final String EVENT_ALLOC_REQ_GC = "jdk.AllocationRequiringGC";
    static final String EVENT_OLD_OBJECT = "jdk.OldObjectSample";
    static final String EVENT_EXECUTION = "jdk.ExecutionSample";
    static final String EVENT_MONITOR = "jdk.JavaMonitorEnter";
    static final String EVENT_Z_STALL = "jdk.ZAllocationStall";

    private final JfrMetricsProperties properties;
    private final JfrMetricsPublisher publisher;
    private final JfrConsumerCircuitBreaker circuitBreaker;
    private final StackFrameNormalizer stackFrameNormalizer;
    private final int topN;
    private final long outsideTlabMinBytes;

    private final TopNWindowStore allocSampleByClass;
    private final TopNWindowStore allocSampleByMethod;
    private final TopNWindowStore outsideTlabByClass;
    private final TopNWindowStore allocReqGcByMethod;
    private final TopNWindowStore oldObjectByClass;
    private final TopNWindowStore executionByMethod;
    private final TopNWindowStore monitorWaitByKey;
    private final TopNWindowStore monitorCountByKey;
    private final AtomicLong zStallNanos = new AtomicLong();
    private final AtomicLong oldObjectHeap = new AtomicLong();
    private final Set<String> seenEvents = ConcurrentHashMap.newKeySet();

    private final AtomicReference<ScheduledExecutorService> schedulerRef = new AtomicReference<>();
    private final AtomicReference<EventStream> streamRef = new AtomicReference<>();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean destroyed = new AtomicBoolean(false);
    /** Coordinates streamRef install/close with repository_attached gauge updates. */
    private final Object streamLock = new Object();
    /** Coordinates event-seen markers with window drain so markers and samples stay in the same window. */
    private final Object windowLock = new Object();
    private final Map<String, Consumer<RecordedEvent>> handlers = new ConcurrentHashMap<>();
    private final Map<String, Long> trippedAt = new ConcurrentHashMap<>();
    private final AtomicLong streamTrippedAt = new AtomicLong(0L);
    private volatile ScheduledFuture<?> attachFuture;
    private volatile ScheduledFuture<?> flushFuture;
    private volatile ScheduledFuture<?> circuitFuture;
    private long attachDeadlineNanos;

    public JfrMetricsConsumer(JfrMetricsProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.topN = Math.max(1, properties.getPublish().getTopN());
        int maxKeys = Math.max(topN * 4, topN);
        this.outsideTlabMinBytes = properties.getConsume().getOutsideTlab().getMinSize().toBytes();
        this.publisher = new JfrMetricsPublisher(meterRegistry, topN);
        this.circuitBreaker = new JfrConsumerCircuitBreaker(properties.getCircuitBreaker());
        this.stackFrameNormalizer = new StackFrameNormalizer(properties.getStack().getBusinessPackages());
        this.allocSampleByClass = new TopNWindowStore(maxKeys);
        this.allocSampleByMethod = new TopNWindowStore(maxKeys);
        this.outsideTlabByClass = new TopNWindowStore(maxKeys);
        this.allocReqGcByMethod = new TopNWindowStore(maxKeys);
        this.oldObjectByClass = new TopNWindowStore(maxKeys);
        this.executionByMethod = new TopNWindowStore(maxKeys);
        this.monitorWaitByKey = new TopNWindowStore(maxKeys);
        this.monitorCountByKey = new TopNWindowStore(maxKeys);
    }

    @Override
    public void start() {
        if (!properties.isEnabled() || destroyed.get() || !started.compareAndSet(false, true)) {
            return;
        }
        running.set(true);
        ScheduledExecutorService scheduler = ensureScheduler();
        attachDeadlineNanos = System.nanoTime() + properties.getRepository().getAttachTimeout().toNanos();
        Duration retry = properties.getRepository().getRetryInterval();
        attachFuture = scheduler.scheduleWithFixedDelay(
                this::tryAttach, 0, Math.max(1L, retry.toMillis()), TimeUnit.MILLISECONDS);
        Duration flush = properties.getPublish().getFlushInterval();
        flushFuture = scheduler.scheduleAtFixedRate(
                this::flushWindow, flush.toMillis(), Math.max(1L, flush.toMillis()), TimeUnit.MILLISECONDS);
        circuitFuture = scheduler.scheduleAtFixedRate(this::maintainCircuits, 1, 1, TimeUnit.SECONDS);
        log.info("JFR metrics consumer started (passive EventStream only; process must already record to disk)");
    }

    /**
     * Stops consuming but keeps the bean restartable until {@link #destroy()}.
     */
    @Override
    public void stop() {
        running.set(false);
        cancelTasks();
        closeStreamOnly();
        started.set(false);
        publisher.setRepositoryAttached(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private ScheduledExecutorService ensureScheduler() {
        ScheduledExecutorService existing = schedulerRef.get();
        if (existing != null && !existing.isShutdown()) {
            return existing;
        }
        ScheduledExecutorService created = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "jfr-metrics-consumer");
            t.setDaemon(true);
            return t;
        });
        if (schedulerRef.compareAndSet(existing, created)) {
            return created;
        }
        created.shutdownNow();
        return schedulerRef.get();
    }

    private void cancelTasks() {
        if (attachFuture != null) {
            attachFuture.cancel(true);
            attachFuture = null;
        }
        if (flushFuture != null) {
            flushFuture.cancel(true);
            flushFuture = null;
        }
        if (circuitFuture != null) {
            circuitFuture.cancel(true);
            circuitFuture = null;
        }
    }

    private void tryAttach() {
        if (!running.get() || destroyed.get() || streamRef.get() != null) {
            return;
        }
        // Block scheduled retries while a stream/event cooldown is in effect (maintainCircuits owns recovery).
        if (streamTrippedAt.get() > 0L || circuitBreaker.isStreamOpen()) {
            return;
        }
        if (System.nanoTime() > attachDeadlineNanos) {
            publisher.setRepositoryAttached(false);
            attachDeadlineNanos = System.nanoTime() + properties.getRepository().getAttachTimeout().toNanos();
            log.warn("JFR disk repository not available yet (jdk.jfr.repository). Consumer idle until recording starts.");
            return;
        }
        String repository = System.getProperty("jdk.jfr.repository");
        if (repository == null || repository.isBlank()) {
            publisher.setRepositoryAttached(false);
            return;
        }
        EventStream stream = null;
        try {
            stream = EventStream.openRepository(Path.of(repository));
            stream.setReuse(true);
            registerHandlers(stream);
            // Publish only on flush-interval; do not drain on every JFR chunk flush.
            stream.onError(error -> log.warn("JFR EventStream error: {}", error.toString()));
            AtomicBoolean closedEarly = new AtomicBoolean(false);
            EventStream registered = stream;
            stream.onClose(() -> {
                synchronized (streamLock) {
                    closedEarly.set(true);
                    if (streamRef.compareAndSet(registered, null)) {
                        publisher.setRepositoryAttached(false);
                    }
                }
            });
            stream.startAsync();
            boolean installed = false;
            synchronized (streamLock) {
                if (closedEarly.get() || !running.get() || destroyed.get()) {
                    // refuse closed / stopped streams
                } else if (streamRef.compareAndSet(null, stream)) {
                    publisher.setRepositoryAttached(true);
                    resetAllCircuits();
                    installed = true;
                }
            }
            if (!installed) {
                closeQuietly(stream);
                synchronized (streamLock) {
                    if (streamRef.get() == null) {
                        publisher.setRepositoryAttached(false);
                    }
                }
                return;
            }
            log.info("Attached passive JFR EventStream to repository {}", repository);
        } catch (IOException ex) {
            closeQuietly(stream);
            publisher.setRepositoryAttached(false);
            log.debug("Unable to open JFR repository {}: {}", repository, ex.toString());
        }
    }

    private void registerHandlers(EventStream stream) {
        handlers.clear();
        JfrMetricsProperties.Consume consume = properties.getConsume();
        if (consume.getAllocationSample().isEnabled()) {
            bind(stream, EVENT_ALLOC_SAMPLE, this::onAllocSample);
        }
        if (consume.getOutsideTlab().isEnabled()) {
            bind(stream, EVENT_OUTSIDE_TLAB, this::onOutsideTlab);
        }
        if (consume.getAllocationRequiringGc().isEnabled()) {
            bind(stream, EVENT_ALLOC_REQ_GC, this::onAllocReqGc);
        }
        if (consume.getOldObjects().isEnabled()) {
            bind(stream, EVENT_OLD_OBJECT, this::onOldObject);
        }
        if (consume.getExecutionSample().isEnabled()) {
            bind(stream, EVENT_EXECUTION, this::onExecution);
        }
        if (consume.getMonitorEnter().isEnabled()) {
            bind(stream, EVENT_MONITOR, this::onMonitor);
        }
        if (consume.getZgcAllocationStall().isEnabled()) {
            bind(stream, EVENT_Z_STALL, this::onZStall);
        }
    }

    private void bind(EventStream stream, String eventName, Consumer<RecordedEvent> handler) {
        Consumer<RecordedEvent> wrapped = event -> {
            if (!circuitBreaker.tryAcquire(eventName)) {
                return;
            }
            synchronized (windowLock) {
                seenEvents.add(eventName);
                handler.accept(event);
            }
        };
        handlers.put(eventName, wrapped);
        stream.onEvent(eventName, wrapped);
    }

    private void onAllocSample(RecordedEvent event) {
        RecordedClass objectClass = event.getClass("objectClass");
        long weight = event.getLong("weight");
        if (objectClass != null && objectClass.getName() != null) {
            allocSampleByClass.add(objectClass.getName(), weight);
        }
        allocSampleByMethod.add(stackFrameNormalizer.normalize(event.getStackTrace()), weight);
    }

    private void onOutsideTlab(RecordedEvent event) {
        long size = event.getLong("allocationSize");
        if (size < outsideTlabMinBytes) {
            return;
        }
        RecordedClass objectClass = event.getClass("objectClass");
        if (objectClass != null && objectClass.getName() != null) {
            outsideTlabByClass.add(objectClass.getName(), size);
        }
    }

    private void onAllocReqGc(RecordedEvent event) {
        long size = event.getLong("size");
        String method = stackFrameNormalizer.normalize(event.getStackTrace());
        allocReqGcByMethod.add(method, size);
        allocReqGcByMethod.observeMax(method, size);
    }

    private void onOldObject(RecordedEvent event) {
        if (event.hasField("object.type")) {
            RecordedClass type = event.getClass("object.type");
            if (type != null && type.getName() != null) {
                oldObjectByClass.add(type.getName(), 1L);
            }
        }
        if (event.hasField("lastKnownHeapUsage")) {
            oldObjectHeap.set(event.getLong("lastKnownHeapUsage"));
        }
    }

    private void onExecution(RecordedEvent event) {
        executionByMethod.add(stackFrameNormalizer.normalize(event.getStackTrace()), 1L);
    }

    private void onMonitor(RecordedEvent event) {
        String method = stackFrameNormalizer.normalize(event.getStackTrace());
        RecordedClass monitorClass = event.hasField("monitorClass") ? event.getClass("monitorClass") : null;
        String monitor = monitorClass == null || monitorClass.getName() == null ? "unknown" : monitorClass.getName();
        String key = method + "|" + monitor;
        monitorWaitByKey.add(key, event.getDuration().toNanos());
        monitorCountByKey.add(key, 1L);
    }

    private void onZStall(RecordedEvent event) {
        zStallNanos.addAndGet(event.getDuration().toNanos());
    }

    private void flushWindow() {
        Set<String> publishedSeen;
        TopNWindowStore.Snapshot allocClass;
        TopNWindowStore.Snapshot allocMethod;
        TopNWindowStore.Snapshot outside;
        TopNWindowStore.Snapshot reqGc;
        TopNWindowStore.Snapshot oldObjects;
        TopNWindowStore.Snapshot execution;
        TopNWindowStore.Snapshot monitorWait;
        TopNWindowStore.Snapshot monitorCount;
        long heap;
        double zStall;
        long dropped;
        synchronized (windowLock) {
            if (!running.get() || destroyed.get()) {
                return;
            }
            publishedSeen = Set.copyOf(seenEvents);
            seenEvents.clear();
            allocClass = allocSampleByClass.drainTopN(topN);
            allocMethod = allocSampleByMethod.drainTopN(topN);
            outside = outsideTlabByClass.drainTopN(topN);
            reqGc = allocReqGcByMethod.drainTopN(topN);
            oldObjects = oldObjectByClass.drainTopN(topN);
            execution = executionByMethod.drainTopN(topN);
            monitorWait = monitorWaitByKey.drainTopN(topN);
            monitorCount = monitorCountByKey.drainTopN(topN);
            heap = oldObjectHeap.get();
            zStall = zStallNanos.getAndSet(0L) / 1_000_000_000d;
            dropped = allocClass.droppedKeys()
                    + allocMethod.droppedKeys()
                    + outside.droppedKeys()
                    + reqGc.droppedKeys()
                    + oldObjects.droppedKeys()
                    + execution.droppedKeys()
                    + monitorWait.droppedKeys()
                    + monitorCount.droppedKeys()
                    + circuitBreaker.drainDropped();
        }

        publisher.publishEventSeen(publishedSeen);
        publisher.publishLabeled(JfrMetricsPublisher.ALLOC_SAMPLE_BY_CLASS, "class", allocClass);
        publisher.publishLabeled(JfrMetricsPublisher.ALLOC_SAMPLE_BY_METHOD, "method", allocMethod);
        publisher.publishLabeled(JfrMetricsPublisher.OUTSIDE_TLAB_BY_CLASS, "class", outside);
        publisher.publishLabeled(JfrMetricsPublisher.ALLOC_REQ_GC_BY_METHOD, "method", reqGc);
        publisher.publishLabeledMax(JfrMetricsPublisher.ALLOC_REQ_GC_MAX_BY_METHOD, "method", reqGc);
        publisher.publishLabeled(JfrMetricsPublisher.OLD_OBJECT_SAMPLES, "class", oldObjects);
        publisher.publishLabeled(JfrMetricsPublisher.EXECUTION_SAMPLES, "method", execution);
        publisher.publishMonitor(nanosToMilliSnapshot(monitorWait), monitorCount);
        publisher.setOldObjectHeap(heap);
        publisher.setZStallSeconds(zStall);
        publisher.setAggregatorKeys(
                (long) allocClass.sums().size()
                        + allocMethod.sums().size()
                        + outside.sums().size()
                        + reqGc.sums().size()
                        + oldObjects.sums().size()
                        + execution.sums().size()
                        + monitorWait.sums().size());
        publisher.addDropped(dropped);
    }

    private static TopNWindowStore.Snapshot nanosToMilliSnapshot(TopNWindowStore.Snapshot nanos) {
        Map<String, Long> millis = new LinkedHashMap<>();
        nanos.sums().forEach((k, v) -> millis.put(k, v / 1_000_000L));
        return new TopNWindowStore.Snapshot(millis, Map.of(), nanos.droppedKeys());
    }

    private void maintainCircuits() {
        if (!running.get() || destroyed.get()) {
            return;
        }
        long now = System.nanoTime();
        long cooldown = circuitBreaker.cooldownNanos();

        if (circuitBreaker.isStreamOpen()) {
            publisher.setStreamCircuitOpen(true);
            if (streamRef.get() != null) {
                closeStreamOnly();
                streamTrippedAt.compareAndSet(0L, now);
            }
            return;
        }

        if (streamRef.get() == null && streamTrippedAt.get() > 0L) {
            if (now - streamTrippedAt.get() >= cooldown) {
                resetAllCircuits();
                streamTrippedAt.set(0L);
                tryAttach();
            }
            return;
        }

        publisher.setStreamCircuitOpen(false);
        EventStream stream = streamRef.get();
        if (stream == null) {
            return;
        }

        for (Map.Entry<String, Boolean> entry : circuitBreaker.openSnapshot().entrySet()) {
            String eventName = entry.getKey();
            boolean open = Boolean.TRUE.equals(entry.getValue());
            publisher.setCircuitOpen(eventName, open);
            if (open && !trippedAt.containsKey(eventName)) {
                Consumer<RecordedEvent> handler = handlers.get(eventName);
                if (handler != null) {
                    stream.remove(handler);
                    trippedAt.put(eventName, now);
                }
            }
        }

        if (circuitBreaker.openEventCount() >= 2) {
            closeStreamOnly();
            streamTrippedAt.set(now);
            circuitBreaker.tripStream();
            publisher.setStreamCircuitOpen(true);
            return;
        }

        for (Map.Entry<String, Long> entry : Map.copyOf(trippedAt).entrySet()) {
            if (now - entry.getValue() < cooldown) {
                continue;
            }
            String eventName = entry.getKey();
            circuitBreaker.clearEvent(eventName);
            publisher.setCircuitOpen(eventName, false);
            Consumer<RecordedEvent> handler = handlers.get(eventName);
            EventStream current = streamRef.get();
            if (handler != null && current != null) {
                current.onEvent(eventName, handler);
            }
            trippedAt.remove(eventName);
        }
    }

    /**
     * After stream cooldown (or successful reattach), clear stream + every per-event open flag —
     * including events that tripped between openSnapshot and openEventCount checks and never entered {@code trippedAt}.
     */
    private void resetAllCircuits() {
        circuitBreaker.clearStream();
        circuitBreaker.clearAllEvents();
        for (String eventName : Map.copyOf(trippedAt).keySet()) {
            publisher.setCircuitOpen(eventName, false);
        }
        for (Map.Entry<String, Boolean> entry : circuitBreaker.openSnapshot().entrySet()) {
            publisher.setCircuitOpen(entry.getKey(), false);
        }
        trippedAt.clear();
        publisher.setStreamCircuitOpen(false);
    }

    private void closeStreamOnly() {
        EventStream stream;
        synchronized (streamLock) {
            stream = streamRef.getAndSet(null);
            publisher.setRepositoryAttached(false);
        }
        closeQuietly(stream);
    }

    private static void closeQuietly(EventStream stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (Exception ignored) {
            // ignore close races
        }
    }

    @Override
    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }
        stop();
        ScheduledExecutorService scheduler = schedulerRef.getAndSet(null);
        if (scheduler != null) {
            scheduler.shutdownNow();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        // After scheduler drain: no in-flight flush can re-register gauges.
        synchronized (windowLock) {
            publisher.unregisterAll();
        }
    }
}
