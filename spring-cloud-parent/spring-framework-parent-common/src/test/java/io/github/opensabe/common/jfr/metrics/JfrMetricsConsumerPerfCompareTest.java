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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Rigorous A/B consume-side overhead compare. Opt-in only — excluded from CI.
 * <p>
 * Both legs keep JVM disk recording; only the passive consumer is toggled.
 * <pre>
 * mvn -pl spring-cloud-parent/spring-framework-parent-common -am test \
 *   -Dtest=JfrMetricsConsumerPerfCompareTest \
 *   -Djfr.perf.excludedGroups= \
 *   -Djfr.perf.compare=true
 * </pre>
 * Writes {@code docs/jfr-metrics-perf-results.md} (override with {@code -Djfr.perf.report=...}).
 */
@Tag("jfr-perf")
@EnabledIfSystemProperty(named = "jfr.perf.compare", matches = "true")
class JfrMetricsConsumerPerfCompareTest {

    private static final int SAMPLES = 9;
    private static final long SETTLE_MS = 250L;
    /** Keep sampler running after the workload so async EventStream batches can land. */
    private static final long FLUSH_COVER_MS = 500L;
    private static final int WORKER_THREADS = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
    /**
     * When a scenario measures allocation itself (esp. multi-thread), it stores the byte delta here
     * so {@link #measure} does not rely on live-thread snapshots after workers exit.
     */
    private static final AtomicLong WORKLOAD_ALLOC_BYTES = new AtomicLong(-1L);

    @Test
    @Timeout(value = 30, unit = TimeUnit.MINUTES)
    void compareAllScenariosAndWriteReport() throws Exception {
        assumeTrue(System.getProperty("jdk.jfr.repository") != null
                        && !System.getProperty("jdk.jfr.repository").isBlank(),
                "need disk JFR repository (-XX:StartFlightRecording)");

        Instant startedAt = Instant.now();
        RuntimeEnv env = RuntimeEnv.capture();
        List<ScenarioResult> results = new ArrayList<>();

        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        JfrMetricsConsumer consumer = new JfrMetricsConsumer(enabledProperties(), registry);
        consumer.start();
        try {
            awaitAttached(registry, Duration.ofSeconds(30));
            // Shared JIT / TLAB warmup with consumer attached, then stop for clean baseline start.
            for (Scenario scenario : Scenario.values()) {
                blackhole(scenario.run(scenario.warmupUnits(), WORKER_THREADS));
            }
            consumer.stop();

            verifyEventStreamOwnershipEdges(consumer, registry);

            for (Scenario scenario : Scenario.values()) {
                results.add(runInterleaved(scenario, consumer, registry));
            }
        } finally {
            consumer.stop();
            consumer.destroy();
        }

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.enabled().attached());
        // Consume work must show up in the enabled-leg consumer CPU window (flush-cover included).
        assertThat(results)
                .allMatch(r -> r.consumerCpuP50() > 0L,
                        "enabled consumer CPU p50 must be > 0 (events/consume window empty)");
        // Catastrophic-only guard (manual perf gate lives in the written report).
        assertThat(results)
                .allMatch(r -> r.wallRatioP50() < 5.0d,
                        "unexpected >5× wall p50 overhead in a scenario");

        Path report = resolveReportPath();
        String markdown = renderMarkdown(startedAt, env, results);
        Files.createDirectories(report.getParent());
        Files.writeString(report, markdown, StandardCharsets.UTF_8);
        System.out.println(markdown);
    }

    /** Shared fail-fast used by enabled measure path and ownership-edge verification. */
    private static void ensureOwnedEventStreamOrFail() {
        if (!OwnedConsumerCpu.hasOwnedEventStream()) {
            throw new AssertionError(
                    "no live EventStream worker owned after attach; consumer-CPU sample would be incomplete");
        }
    }

    /**
     * Runtime check: EventStream exits after attach / before identity collection → measure path fails;
     * re-attach → identity succeeds via the same {@link #ensureOwnedEventStreamOrFail()} gate.
     */
    private static void verifyEventStreamOwnershipEdges(
            JfrMetricsConsumer consumer, SimpleMeterRegistry registry) throws InterruptedException {
        // Happy path through the real enabled-leg preamble (without measuring).
        OwnedConsumerCpu.markForeignEventStreams();
        consumer.start();
        awaitAttached(registry, Duration.ofSeconds(15));
        OwnedConsumerCpu.beginSession();
        for (int i = 0; i < 50 && !OwnedConsumerCpu.hasOwnedEventStream(); i++) {
            OwnedConsumerCpu.adoptReplacementsOnce();
            Thread.sleep(20L);
        }
        ensureOwnedEventStreamOrFail();

        // Stream exits after attach gauge is true, before identity is collected for a new session.
        consumer.stop();
        Thread.sleep(SETTLE_MS);
        OwnedConsumerCpu.endSession();
        OwnedConsumerCpu.markForeignEventStreams();
        OwnedConsumerCpu.beginSession();
        for (int i = 0; i < 10 && !OwnedConsumerCpu.hasOwnedEventStream(); i++) {
            OwnedConsumerCpu.adoptReplacementsOnce();
            Thread.sleep(20L);
        }
        AssertionError failFast = null;
        try {
            ensureOwnedEventStreamOrFail();
        } catch (AssertionError expected) {
            failFast = expected;
        }
        assertThat(failFast)
                .as("measure path fails when EventStream exits after attach before identity")
                .isNotNull();

        // Re-attach: shared gate must pass again.
        OwnedConsumerCpu.markForeignEventStreams();
        consumer.start();
        awaitAttached(registry, Duration.ofSeconds(15));
        OwnedConsumerCpu.beginSession();
        for (int i = 0; i < 50 && !OwnedConsumerCpu.hasOwnedEventStream(); i++) {
            OwnedConsumerCpu.adoptReplacementsOnce();
            Thread.sleep(20L);
        }
        ensureOwnedEventStreamOrFail();
        consumer.stop();
        OwnedConsumerCpu.endSession();
        System.out.println(
                "ownership-edge verification: post-attach-exit→ensureOwnedEventStreamOrFail=FAIL(OK), re-attach=OK");
    }

    private static ScenarioResult runInterleaved(
            Scenario scenario,
            JfrMetricsConsumer consumer,
            SimpleMeterRegistry registry) throws InterruptedException {
        List<Sample> baselines = new ArrayList<>(SAMPLES);
        List<Sample> enabled = new ArrayList<>(SAMPLES);

        for (int i = 0; i < SAMPLES; i++) {
            // Alternate A/B order across pairs to reduce order bias.
            boolean enabledFirst = (i % 2) == 1;
            Sample baseline;
            Sample enabledSample;
            if (enabledFirst) {
                enabledSample = measureEnabledLeg(consumer, registry, scenario, true);
                baseline = measureBaselineLeg(consumer, scenario, true);
            } else {
                baseline = measureBaselineLeg(consumer, scenario, false);
                enabledSample = measureEnabledLeg(consumer, registry, scenario, false);
            }
            baselines.add(baseline);
            enabled.add(enabledSample);
        }

        Thread.sleep(1_200L);
        Diagnostics diagnostics = Diagnostics.capture(registry);
        return ScenarioResult.from(scenario, baselines, enabled, diagnostics);
    }

    private static Sample measureBaselineLeg(
            JfrMetricsConsumer consumer, Scenario scenario, boolean enabledFirst)
            throws InterruptedException {
        consumer.stop();
        OwnedConsumerCpu.endSession();
        Thread.sleep(SETTLE_MS);
        return measure(() -> blackhole(scenario.run(scenario.measureUnits(), WORKER_THREADS)), enabledFirst);
    }

    private static Sample measureEnabledLeg(
            JfrMetricsConsumer consumer,
            SimpleMeterRegistry registry,
            Scenario scenario,
            boolean enabledFirst) throws InterruptedException {
        // Always restart so either A/B order begins from a known stopped state.
        consumer.stop();
        OwnedConsumerCpu.endSession();
        Thread.sleep(SETTLE_MS);
        OwnedConsumerCpu.markForeignEventStreams();
        consumer.start();
        awaitAttached(registry, Duration.ofSeconds(15));
        OwnedConsumerCpu.beginSession();
        // EventStream worker may appear slightly after attach; wait briefly before measure.
        for (int i = 0; i < 50 && !OwnedConsumerCpu.hasOwnedEventStream(); i++) {
            OwnedConsumerCpu.adoptReplacementsOnce();
            Thread.sleep(20L);
        }
        ensureOwnedEventStreamOrFail();
        // Measure immediately afterward so consume work overlaps the workload (same
        // post-stop settle as baseline; no long idle settle that drains the backlog).
        return measure(() -> blackhole(scenario.run(scenario.measureUnits(), WORKER_THREADS)), enabledFirst);
    }

    private static Sample measure(Runnable work, boolean enabledFirst) {
        GcSnapshot gcBefore = GcSnapshot.capture();
        // One-shot replacement adopt outside the sampler so both legs share equal poll cost.
        OwnedConsumerCpu.adoptReplacementsOnce();
        long consumerCpuBefore = OwnedConsumerCpu.totalCpuNanos();
        WORKLOAD_ALLOC_BYTES.set(-1L);

        java.util.concurrent.atomic.AtomicBoolean sampling = new java.util.concurrent.atomic.AtomicBoolean(true);
        java.util.concurrent.CountDownLatch baselinesReady = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicLong samplerCpuEnd = new java.util.concurrent.atomic.AtomicLong(-1L);
        Thread sampler = new Thread(() -> {
            try {
                baselinesReady.await();
                while (sampling.get()) {
                    // Refresh only — no adopt/scan divergence between baseline and enabled legs.
                    OwnedConsumerCpu.totalCpuNanos();
                    try {
                        Thread.sleep(1L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // Capture CPU before the thread exits — getThreadCpuTime is unavailable after death.
                @SuppressWarnings("deprecation")
                long selfId = Thread.currentThread().getId();
                samplerCpuEnd.set(threadCpuNanos(selfId));
            }
        }, "jfr-perf-cpu-sampler");
        // Process baseline before the sampler starts; new sampler thread CPU starts at 0.
        long processCpuBefore = processCpuNanos();
        sampler.setDaemon(true);
        sampler.start();
        long samplerCpuBefore = 0L;
        baselinesReady.countDown();
        long wallBefore = System.nanoTime();
        try {
            work.run();
        } finally {
            // Workload wall excludes flush cover so short scenarios are not diluted toward 1.0.
        }
        long wallWorkload = System.nanoTime() - wallBefore;
        try {
            // Cover async EventStream delivery across ≥1 publish flush interval (still sampled).
            OwnedConsumerCpu.adoptReplacementsOnce();
            Thread.sleep(FLUSH_COVER_MS);
            OwnedConsumerCpu.adoptReplacementsOnce();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            sampling.set(false);
            try {
                sampler.join(2_000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (sampler.isAlive() || samplerCpuEnd.get() < 0L) {
                // Join timed out or finally did not publish — do not guess sampler CPU.
                samplerCpuEnd.compareAndSet(-1L, samplerCpuBefore);
            }
        }

        long samplerEnd = samplerCpuEnd.get();
        long samplerCpu = Math.max(0L, samplerEnd - samplerCpuBefore);
        // Exclude harness sampler CPU from process delta (same both legs).
        long processCpu = Math.max(0L, processCpuNanos() - processCpuBefore - samplerCpu);
        long consumerCpu = Math.max(0L, OwnedConsumerCpu.totalCpuNanos() - consumerCpuBefore);
        long allocated = WORKLOAD_ALLOC_BYTES.get();
        GcSnapshot gcAfter = GcSnapshot.capture();
        return new Sample(
                wallWorkload,
                processCpu,
                consumerCpu,
                allocated,
                gcAfter.minus(gcBefore),
                enabledFirst);
    }

    private static JfrMetricsProperties enabledProperties() {
        JfrMetricsProperties properties = new JfrMetricsProperties();
        properties.setEnabled(true);
        properties.getPublish().setFlushInterval(Duration.ofMillis(200));
        properties.getPublish().setTopN(30);
        properties.getRepository().setAttachTimeout(Duration.ofSeconds(20));
        properties.getRepository().setRetryInterval(Duration.ofMillis(200));
        properties.getCircuitBreaker().setMaxPerSecond(5_000);
        properties.getCircuitBreaker().setGlobalMaxPerSecond(20_000);
        return properties;
    }

    private static void awaitAttached(SimpleMeterRegistry registry, Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            Gauge gauge = registry.find("jfr_metrics_repository_attached").gauge();
            if (gauge != null && gauge.value() == 1.0d) {
                Thread.sleep(400L);
                return;
            }
            Thread.sleep(100L);
        }
        throw new AssertionError("JFR consumer did not attach within " + timeout);
    }

    private static Path resolveReportPath() {
        String override = System.getProperty("jfr.perf.report");
        if (override != null && !override.isBlank()) {
            return Path.of(override).toAbsolutePath().normalize();
        }
        Path cwd = Path.of("").toAbsolutePath().normalize();
        Path candidate = cwd.resolve("../../docs/jfr-metrics-perf-results.md").normalize();
        if (Files.isDirectory(candidate.getParent())) {
            return candidate;
        }
        return cwd.resolve("docs/jfr-metrics-perf-results.md").normalize();
    }

    private static String renderMarkdown(Instant startedAt, RuntimeEnv env, List<ScenarioResult> results) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                .withZone(ZoneId.systemDefault());
        StringBuilder sb = new StringBuilder();
        sb.append("# JFR metrics consumer — perf compare results\n\n");
        sb.append("> Generated by `JfrMetricsConsumerPerfCompareTest` (opt-in, not run in CI).\n\n");
        sb.append("- Captured at: `").append(fmt.format(startedAt)).append("`\n");
        sb.append("- Java: `").append(env.javaVersion()).append("`\n");
        sb.append("- OS: `").append(env.os()).append("`\n");
        sb.append("- CPUs: `").append(env.cpus()).append("`\n");
        sb.append("- Max heap: `").append(formatBytes(env.maxHeap())).append("`\n");
        sb.append("- JFR repository present: `").append(env.jfrRepositoryPresent()).append("`\n");
        sb.append("- Samples/scenario (interleaved A/B): `").append(SAMPLES).append("`\n");
        sb.append("- Worker threads (LOCK/MIXED): `").append(WORKER_THREADS).append("`\n");
        sb.append("- Settle both legs: `").append(SETTLE_MS).append("ms`; A/B order alternates per accepted pair\n");
        sb.append("- Post-work flush cover: `").append(FLUSH_COVER_MS).append("ms` (publish flush interval 200ms)\n\n");
        sb.append("Both legs use the same disk Flight Recording. Delta ≈ consume-side cost only.\n\n");
        sb.append("## Reading the ratios\n\n");
        sb.append("- `wall/process CPU/alloc ratio ≈ 1.0` → consume-side overhead negligible on that workload\n");
        sb.append("- `consumer CPU p50` tracks session-owned thread ids (`jfr-metrics-consumer` + EventStream ids born after pre-start mark); sampler refreshes last CPU on a nominal 1ms sleep\n");
        sb.append("- Limits: poll gaps follow `Thread.sleep(1)` + scheduling (not a hard 1ms error bound); mid-measure EventStream replacements are re-adopted around the flush-cover window, not every poll; identity is appearance-after-mark\n");
        sb.append("- Enabled legs fail fast if no live EventStream worker is owned after attach; sampler blocks on a latch until process/sampler CPU baselines are taken together\n");
        sb.append("- `wall` ratios use **workload-only** wall (flush-cover wait is excluded from wall, still included in consumer/process CPU sampling)\n");
        sb.append("- Sampler work is refresh-only (equal baseline/enabled poll cost); sampler CPU is subtracted from process CPU\n");
        long enabledFirst = results.stream()
                .mapToLong(r -> r.enabledFirstPairs())
                .sum();
        long totalPairs = (long) SAMPLES * results.size();
        sb.append("- Accepted pairs with enabled-first order: `")
                .append(enabledFirst).append('/').append(totalPairs).append("`\n");
        sb.append("- `alloc` is measured inside the workload (per-worker `getCurrentThreadAllocatedBytes`), not via live-thread snapshots\n");
        sb.append("- `dropped` / `keys` are Micrometer gauges after the scenario’s last enabled leg\n");
        sb.append("- Short GC Δ noise is normal; prefer wall/CPU/alloc ratios across scenarios\n\n");

        sb.append("## Summary\n\n");
        sb.append("| Scenario | wall p50 ratio | wall p95 ratio | process CPU p50 ratio | ")
                .append("alloc p50 ratio | consumer CPU p50 | GC time Δ p50 | dropped | keys |\n");
        sb.append("|---|---:|---:|---:|---:|---:|---:|---:|---:|\n");
        for (ScenarioResult r : results) {
            sb.append("| ").append(r.scenario().name())
                    .append(" | ").append(fmtRatio(r.wallRatioP50()))
                    .append(" | ").append(fmtRatio(r.wallRatioP95()))
                    .append(" | ").append(fmtRatio(r.processCpuRatioP50()))
                    .append(" | ").append(fmtRatio(r.allocRatioP50()))
                    .append(" | ").append(formatNanos(r.consumerCpuP50()))
                    .append(" | ").append(formatMillis(r.gcTimeDeltaP50Ms()))
                    .append(" | ").append(r.enabled().dropped())
                    .append(" | ").append(r.enabled().aggregatorKeys())
                    .append(" |\n");
        }

        sb.append("\n## Per-scenario detail\n\n");
        for (ScenarioResult r : results) {
            sb.append("### ").append(r.scenario().name()).append(" — ").append(r.scenario().description()).append("\n\n");
            sb.append("| Leg | wall p50 | wall p95 | process CPU p50 | alloc p50 | consumer CPU p50 | GC count Δ p50 | GC time Δ p50 |\n");
            sb.append("|---|---:|---:|---:|---:|---:|---:|---:|\n");
            sb.append("| baseline (recording only) | ")
                    .append(formatNanos(r.baseline().wallP50())).append(" | ")
                    .append(formatNanos(r.baseline().wallP95())).append(" | ")
                    .append(formatNanos(r.baseline().processCpuP50())).append(" | ")
                    .append(formatBytes(r.baseline().allocP50())).append(" | ")
                    .append(formatNanos(r.baseline().consumerCpuP50())).append(" | ")
                    .append(r.baseline().gcCountP50()).append(" | ")
                    .append(formatMillis(r.baseline().gcTimeP50Ms())).append(" |\n");
            sb.append("| consumer on | ")
                    .append(formatNanos(r.enabled().wallP50())).append(" | ")
                    .append(formatNanos(r.enabled().wallP95())).append(" | ")
                    .append(formatNanos(r.enabled().processCpuP50())).append(" | ")
                    .append(formatBytes(r.enabled().allocP50())).append(" | ")
                    .append(formatNanos(r.enabled().consumerCpuP50())).append(" | ")
                    .append(r.enabled().gcCountP50()).append(" | ")
                    .append(formatMillis(r.enabled().gcTimeP50Ms())).append(" |\n\n");
            sb.append("- repository_attached: `").append(r.enabled().attached()).append("`\n");
            sb.append("- jfr_metrics_dropped_total: `").append(r.enabled().dropped()).append("`\n");
            sb.append("- jfr_metrics_aggregator_keys: `").append(r.enabled().aggregatorKeys()).append("`\n\n");
        }

        sb.append("## How to re-run\n\n");
        sb.append("```bash\n");
        sb.append("mvn -pl spring-cloud-parent/spring-framework-parent-common -am test \\\n");
        sb.append("  -Dtest=JfrMetricsConsumerPerfCompareTest \\\n");
        sb.append("  -Djfr.perf.excludedGroups= \\\n");
        sb.append("  -Djfr.perf.compare=true\n");
        sb.append("```\n\n");
        sb.append("CI excludes tag `jfr-perf` via `jfr.perf.excludedGroups` (default) and requires ")
                .append("`jfr.perf.compare=true`.\n\n");

        double maxWall = results.stream().mapToDouble(ScenarioResult::wallRatioP50).filter(d -> !Double.isNaN(d)).max().orElse(Double.NaN);
        double maxCpu = results.stream().mapToDouble(ScenarioResult::processCpuRatioP50).filter(d -> !Double.isNaN(d)).max().orElse(Double.NaN);
        sb.append("## Conclusion (this capture)\n\n");
        sb.append("| Signal | Observation |\n|---|---|\n");
        sb.append("| Wall p50 ratio (max) | **").append(fmtRatio(maxWall)).append("** |\n");
        sb.append("| Process CPU p50 ratio (max) | **").append(fmtRatio(maxCpu)).append("** |\n");
        sb.append("| Consumer-side CPU p50 | see per-scenario (scheduler + EventStream) |\n");
        sb.append("| Dropped | see summary |\n");
        if (!Double.isNaN(maxWall) && maxWall < 1.15d && !Double.isNaN(maxCpu) && maxCpu < 1.15d) {
            sb.append("\n**Verdict:** Under default.jfc disk recording, consume-side overhead is **negligible** ")
                    .append("vs recording-only baseline on this machine.\n");
        } else {
            sb.append("\n**Verdict:** Review scenario ratios above; some workloads exceed the ~15% noise band.\n");
        }
        sb.append("\nThis does **not** measure the cost of enabling heavier jfc events (OutsideTLAB etc.); ")
                .append("that cost is on the recorder, not this module.\n");
        return sb.toString();
    }

    private static long blackhole(long value) {
        if (value == System.nanoTime()) {
            throw new AssertionError("blackhole");
        }
        return value;
    }

    private static long processCpuNanos() {
        OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof com.sun.management.OperatingSystemMXBean sun) {
            long v = sun.getProcessCpuTime();
            return v < 0L ? 0L : v;
        }
        return 0L;
    }

    private static long threadCpuNanos(long threadId) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (!bean.isThreadCpuTimeSupported()) {
            return 0L;
        }
        if (!bean.isThreadCpuTimeEnabled()) {
            bean.setThreadCpuTimeEnabled(true);
        }
        long cpu = bean.getThreadCpuTime(threadId);
        return cpu < 0L ? 0L : cpu;
    }

    @SuppressWarnings("deprecation")
    private static long currentThreadAllocatedBytes() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (!(bean instanceof com.sun.management.ThreadMXBean sun)
                || !sun.isThreadAllocatedMemorySupported()) {
            return -1L;
        }
        if (!sun.isThreadAllocatedMemoryEnabled()) {
            sun.setThreadAllocatedMemoryEnabled(true);
        }
        long allocated = sun.getThreadAllocatedBytes(Thread.currentThread().getId());
        return allocated < 0L ? -1L : allocated;
    }

    /**
     * Tracks CPU for threads owned by this harness’s consumer session only.
     * <p>
     * Call {@link #markForeignEventStreams()} before {@code consumer.start()}, then
     * {@link #beginSession()} after attach (one-shot adopt of {@code jfr-metrics-consumer}
     * plus non-foreign EventStream workers). During an active session, new EventStream
     * threads are adopted only as <em>replacements</em> when a previously owned EventStream
     * id is dead. {@link #totalCpuNanos()} refreshes last-seen CPU (callers should poll
     * during work so mid-sample exits keep accrued time). {@link #endSession()} freezes
     * adoption but keeps owned ids so baseline legs measure residual CPU honestly.
     */
    private static final class OwnedConsumerCpu {
        private static final java.util.Set<Long> FOREIGN = java.util.concurrent.ConcurrentHashMap.newKeySet();
        private static final java.util.Set<Long> OWNED = java.util.concurrent.ConcurrentHashMap.newKeySet();
        private static final java.util.concurrent.ConcurrentHashMap<Long, Long> LAST_CPU =
                new java.util.concurrent.ConcurrentHashMap<>();
        private static volatile boolean sessionActive;

        private OwnedConsumerCpu() {
        }

        /** Snapshot EventStream-named thread ids before this consumer starts. */
        static void markForeignEventStreams() {
            FOREIGN.clear();
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            for (long id : bean.getAllThreadIds()) {
                ThreadInfo info = bean.getThreadInfo(id);
                if (info != null && looksLikeEventStream(info.getThreadName())) {
                    FOREIGN.add(id);
                }
            }
        }

        static void beginSession() {
            OWNED.clear();
            LAST_CPU.clear();
            sessionActive = true;
            adoptInitialOwnedThreads();
            totalCpuNanos();
        }

        static void endSession() {
            if (sessionActive || !OWNED.isEmpty()) {
                totalCpuNanos();
            }
            // Freeze adoption; keep OWNED/LAST_CPU so baseline can observe residual CPU.
            sessionActive = false;
            FOREIGN.clear();
        }

        /** One-shot adopt for late/replacement workers; call outside the 1ms sampler loop. */
        static void adoptReplacementsOnce() {
            if (sessionActive) {
                adoptReplacementsIfNeeded();
            }
        }

        static boolean hasOwnedEventStream() {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            for (Long id : OWNED) {
                ThreadInfo info = bean.getThreadInfo(id);
                if (info != null && looksLikeEventStream(info.getThreadName())
                        && bean.getThreadCpuTime(id) >= 0L) {
                    return true;
                }
            }
            return false;
        }

        static long totalCpuNanos() {
            if (OWNED.isEmpty() && !sessionActive) {
                return 0L;
            }
            refreshLastCpu();
            long sum = 0L;
            for (Long id : OWNED) {
                sum += LAST_CPU.getOrDefault(id, 0L);
            }
            return sum;
        }

        private static void adoptInitialOwnedThreads() {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            for (long id : bean.getAllThreadIds()) {
                if (FOREIGN.contains(id)) {
                    continue;
                }
                ThreadInfo info = bean.getThreadInfo(id);
                if (info == null) {
                    continue;
                }
                String name = info.getThreadName();
                if ("jfr-metrics-consumer".equals(name) || looksLikeEventStream(name)) {
                    OWNED.add(id);
                }
            }
        }

        /**
         * Adopt a replacement only when a previously owned scheduler/EventStream id is dead.
         * Prevents picking up unrelated EventStreams that start while ours are still live.
         */
        private static void adoptReplacementsIfNeeded() {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            boolean liveScheduler = false;
            boolean ownedEventStream = false;
            boolean liveEventStream = false;
            for (Long id : OWNED) {
                ThreadInfo info = bean.getThreadInfo(id);
                if (info == null || bean.getThreadCpuTime(id) < 0L) {
                    continue;
                }
                String name = info.getThreadName();
                if ("jfr-metrics-consumer".equals(name)) {
                    liveScheduler = true;
                } else if (looksLikeEventStream(name)) {
                    ownedEventStream = true;
                    liveEventStream = true;
                }
            }
            for (Long id : OWNED) {
                ThreadInfo info = bean.getThreadInfo(id);
                if (info != null && looksLikeEventStream(info.getThreadName())) {
                    ownedEventStream = true;
                    break;
                }
            }
            if (!liveScheduler) {
                adoptFirstMatch(bean, "scheduler");
            }
            if (!ownedEventStream) {
                // Late initial attach — EventStream may appear slightly after beginSession.
                adoptFirstMatch(bean, "eventstream");
            } else if (!liveEventStream) {
                // Replacement for a dead owned EventStream worker.
                adoptFirstMatch(bean, "eventstream");
            }
        }

        private static void adoptFirstMatch(ThreadMXBean bean, String kind) {
            for (long id : bean.getAllThreadIds()) {
                if (OWNED.contains(id) || FOREIGN.contains(id)) {
                    continue;
                }
                ThreadInfo info = bean.getThreadInfo(id);
                if (info == null) {
                    continue;
                }
                String name = info.getThreadName();
                boolean match = "scheduler".equals(kind)
                        ? "jfr-metrics-consumer".equals(name)
                        : looksLikeEventStream(name);
                if (match) {
                    OWNED.add(id);
                    return;
                }
            }
        }

        private static void refreshLastCpu() {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            if (!bean.isThreadCpuTimeSupported()) {
                return;
            }
            if (!bean.isThreadCpuTimeEnabled()) {
                bean.setThreadCpuTimeEnabled(true);
            }
            for (Long id : OWNED) {
                long cpu = bean.getThreadCpuTime(id);
                if (cpu >= 0L) {
                    LAST_CPU.merge(id, cpu, Math::max);
                }
            }
        }

        /**
         * Heuristic only to discover candidate EventStream workers; ownership still requires
         * the thread id was absent at {@link #markForeignEventStreams()} time (or is a
         * replacement for a dead owned EventStream).
         */
        private static boolean looksLikeEventStream(String name) {
            return name != null && name.startsWith("JFR Event Stream");
        }
    }

    private static String fmtRatio(double ratio) {
        if (Double.isNaN(ratio) || Double.isInfinite(ratio)) {
            return "n/a";
        }
        return String.format("%.3f", ratio);
    }

    private static String formatNanos(long nanos) {
        if (nanos < 0L) {
            return "n/a";
        }
        if (nanos < 1_000_000L) {
            return String.format("%.1fµs", nanos / 1_000d);
        }
        return String.format("%.2fms", nanos / 1_000_000d);
    }

    private static String formatMillis(long millis) {
        if (millis < 0L) {
            return "n/a";
        }
        return millis + "ms";
    }

    private static String formatBytes(long bytes) {
        if (bytes < 0L) {
            return "n/a";
        }
        if (bytes < 1024L) {
            return bytes + "B";
        }
        if (bytes < 1024L * 1024L) {
            return String.format("%.1fKiB", bytes / 1024d);
        }
        return String.format("%.2fMiB", bytes / (1024d * 1024d));
    }

    private static long percentile(long[] sortedCopy, double p) {
        if (sortedCopy.length == 0) {
            return 0L;
        }
        int idx = (int) Math.ceil(p * sortedCopy.length) - 1;
        idx = Math.max(0, Math.min(sortedCopy.length - 1, idx));
        return sortedCopy[idx];
    }

    private static long[] sorted(List<Sample> samples, SampleLongExtractor extractor) {
        long[] values = new long[samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            values[i] = extractor.get(samples.get(i));
        }
        Arrays.sort(values);
        return values;
    }

    @FunctionalInterface
    private interface SampleLongExtractor {
        long get(Sample sample);
    }

    private enum Scenario {
        ALLOC("allocation-heavy single thread (ObjectAllocationSample pressure)") {
            @Override
            int warmupUnits() {
                return 300_000;
            }

            @Override
            int measureUnits() {
                return 2_000_000;
            }

            @Override
            long run(int units, int workers) {
                long before = currentThreadAllocatedBytes();
                long sink = 0L;
                byte[] buf = null;
                for (int i = 0; i < units; i++) {
                    buf = new byte[768 + (i & 0x7ff)];
                    sink += buf.length;
                    sink ^= buf[i & (buf.length - 1)];
                }
                long after = currentThreadAllocatedBytes();
                if (before >= 0L && after >= before) {
                    WORKLOAD_ALLOC_BYTES.set(after - before);
                }
                return sink;
            }
        },
        CPU("CPU-bound hash/rotate loop (ExecutionSample pressure)") {
            @Override
            int warmupUnits() {
                return 20_000_000;
            }

            @Override
            int measureUnits() {
                return 120_000_000;
            }

            @Override
            long run(int units, int workers) {
                long before = currentThreadAllocatedBytes();
                long sink = 0x9e3779b97f4a7c15L;
                for (int i = 0; i < units; i++) {
                    sink = Long.rotateLeft(sink ^ (i * 0xbf58476d1ce4e5b9L), 13);
                    sink *= 0x94d049bb133111ebL;
                }
                long after = currentThreadAllocatedBytes();
                if (before >= 0L && after >= before) {
                    WORKLOAD_ALLOC_BYTES.set(after - before);
                }
                return sink;
            }
        },
        LOCK("multi-thread contended synchronized monitor (JavaMonitorEnter pressure)") {
            @Override
            int warmupUnits() {
                return 200_000;
            }

            @Override
            int measureUnits() {
                return 1_500_000;
            }

            @Override
            long run(int units, int workers) {
                return runContended(units, workers, true);
            }
        },
        MIXED("multi-thread mix of alloc + CPU + short synchronized monitors") {
            @Override
            int warmupUnits() {
                return 250_000;
            }

            @Override
            int measureUnits() {
                return 1_200_000;
            }

            @Override
            long run(int units, int workers) {
                return runContended(units, workers, false);
            }
        };

        private final String description;

        Scenario(String description) {
            this.description = description;
        }

        String description() {
            return description;
        }

        abstract int warmupUnits();

        abstract int measureUnits();

        abstract long run(int units, int workers);

        private static long runContended(int units, int workers, boolean lockOnly) {
            // Use intrinsic monitors so default.jfc records jdk.JavaMonitorEnter (ReentrantLock does not).
            Object monitor = new Object();
            AtomicLong sink = new AtomicLong();
            AtomicLong allocated = new AtomicLong();
            int perWorker = Math.max(1, units / workers);
            ExecutorService pool = Executors.newFixedThreadPool(workers);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(workers);
            for (int w = 0; w < workers; w++) {
                pool.execute(() -> {
                    try {
                        start.await();
                        long allocBefore = currentThreadAllocatedBytes();
                        long local = 0L;
                        byte[] buf = null;
                        for (int i = 0; i < perWorker; i++) {
                            if (lockOnly || (i & 0x1f) == 0) {
                                synchronized (monitor) {
                                    local ^= i;
                                    Thread.onSpinWait();
                                }
                            }
                            if (!lockOnly) {
                                buf = new byte[256 + (i & 0xff)];
                                local += buf.length;
                                local = Long.rotateLeft(local ^ i, 7);
                            }
                        }
                        if (buf != null) {
                            local ^= buf[0];
                        }
                        sink.addAndGet(local);
                        long allocAfter = currentThreadAllocatedBytes();
                        if (allocBefore >= 0L && allocAfter >= allocBefore) {
                            allocated.addAndGet(allocAfter - allocBefore);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            try {
                if (!done.await(3, TimeUnit.MINUTES)) {
                    throw new IllegalStateException("workload timeout");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            } finally {
                pool.shutdownNow();
            }
            WORKLOAD_ALLOC_BYTES.set(allocated.get());
            return sink.get();
        }
    }

    private record Sample(
            long wallNanos,
            long processCpuNanos,
            long consumerCpuNanos,
            long allocatedBytes,
            GcDelta gc,
            boolean enabledFirst) {
    }

    private record GcDelta(long count, long timeMs) {
    }

    private record GcSnapshot(long count, long timeMs) {
        static GcSnapshot capture() {
            long count = 0L;
            long time = 0L;
            for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
                long c = bean.getCollectionCount();
                long t = bean.getCollectionTime();
                if (c > 0L) {
                    count += c;
                }
                if (t > 0L) {
                    time += t;
                }
            }
            return new GcSnapshot(count, time);
        }

        GcDelta minus(GcSnapshot before) {
            return new GcDelta(Math.max(0L, count - before.count), Math.max(0L, timeMs - before.timeMs));
        }
    }

    private record LegStats(
            long wallP50,
            long wallP95,
            long processCpuP50,
            long allocP50,
            long consumerCpuP50,
            long gcCountP50,
            long gcTimeP50Ms,
            boolean attached,
            long dropped,
            long aggregatorKeys) {
        static LegStats from(List<Sample> samples, Diagnostics diagnostics) {
            return new LegStats(
                    percentile(sorted(samples, Sample::wallNanos), 0.50d),
                    percentile(sorted(samples, Sample::wallNanos), 0.95d),
                    percentile(sorted(samples, Sample::processCpuNanos), 0.50d),
                    percentile(sorted(samples, Sample::allocatedBytes), 0.50d),
                    percentile(sorted(samples, Sample::consumerCpuNanos), 0.50d),
                    percentile(sorted(samples, s -> s.gc().count()), 0.50d),
                    percentile(sorted(samples, s -> s.gc().timeMs()), 0.50d),
                    diagnostics.attached(),
                    diagnostics.dropped(),
                    diagnostics.aggregatorKeys());
        }
    }

    private record Diagnostics(boolean attached, long dropped, long aggregatorKeys) {
        static Diagnostics capture(SimpleMeterRegistry registry) {
            Gauge attached = registry.find("jfr_metrics_repository_attached").gauge();
            Gauge dropped = registry.find("jfr_metrics_dropped_total").gauge();
            Gauge keys = registry.find("jfr_metrics_aggregator_keys").gauge();
            return new Diagnostics(
                    attached != null && attached.value() == 1.0d,
                    dropped == null ? -1L : (long) dropped.value(),
                    keys == null ? -1L : (long) keys.value());
        }
    }

    private record ScenarioResult(
            Scenario scenario,
            LegStats baseline,
            LegStats enabled,
            int enabledFirstPairs) {
        static ScenarioResult from(
                Scenario scenario,
                List<Sample> baselines,
                List<Sample> enabledSamples,
                Diagnostics diagnostics) {
            // Baseline has no consumer; still reuse diagnostics shape with attached=false.
            LegStats baseline = LegStats.from(baselines, new Diagnostics(false, 0L, 0L));
            LegStats enabled = LegStats.from(enabledSamples, diagnostics);
            int enabledFirst = (int) enabledSamples.stream().filter(Sample::enabledFirst).count();
            return new ScenarioResult(scenario, baseline, enabled, enabledFirst);
        }

        double wallRatioP50() {
            return ratio(enabled.wallP50(), baseline.wallP50());
        }

        double wallRatioP95() {
            return ratio(enabled.wallP95(), baseline.wallP95());
        }

        double processCpuRatioP50() {
            return ratio(enabled.processCpuP50(), baseline.processCpuP50());
        }

        double allocRatioP50() {
            return ratio(enabled.allocP50(), baseline.allocP50());
        }

        long consumerCpuP50() {
            return enabled.consumerCpuP50();
        }

        long gcTimeDeltaP50Ms() {
            return Math.max(0L, enabled.gcTimeP50Ms() - baseline.gcTimeP50Ms());
        }

        private static double ratio(long num, long den) {
            if (den <= 0L) {
                return Double.NaN;
            }
            return (double) num / (double) den;
        }
    }

    private record RuntimeEnv(
            String javaVersion,
            String os,
            int cpus,
            long maxHeap,
            boolean jfrRepositoryPresent) {
        static RuntimeEnv capture() {
            String repo = System.getProperty("jdk.jfr.repository");
            return new RuntimeEnv(
                    System.getProperty("java.version"),
                    System.getProperty("os.name") + " " + System.getProperty("os.version"),
                    Runtime.getRuntime().availableProcessors(),
                    Runtime.getRuntime().maxMemory(),
                    repo != null && !repo.isBlank());
        }
    }
}
