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
import java.util.concurrent.locks.ReentrantLock;

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

            for (Scenario scenario : Scenario.values()) {
                results.add(runInterleaved(scenario, consumer, registry));
            }
        } finally {
            consumer.stop();
            consumer.destroy();
        }

        Path report = resolveReportPath();
        String markdown = renderMarkdown(startedAt, env, results);
        Files.createDirectories(report.getParent());
        Files.writeString(report, markdown, StandardCharsets.UTF_8);
        System.out.println(markdown);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.enabled().attached());
        // Catastrophic-only guard (manual perf gate lives in the written report).
        assertThat(results)
                .allMatch(r -> r.wallRatioP50() < 5.0d,
                        "unexpected >5× wall p50 overhead in a scenario");
    }

    private static ScenarioResult runInterleaved(
            Scenario scenario,
            JfrMetricsConsumer consumer,
            SimpleMeterRegistry registry) throws InterruptedException {
        List<Sample> baselines = new ArrayList<>(SAMPLES);
        List<Sample> enabled = new ArrayList<>(SAMPLES);

        int attempts = 0;
        int maxAttempts = SAMPLES * 4;
        while (baselines.size() < SAMPLES && attempts < maxAttempts) {
            attempts++;
            consumer.stop();
            OwnedConsumerCpu.endSession();
            // Settle after stop so baseline can observe residual owned-thread CPU honestly.
            Thread.sleep(250L);
            Sample baseline = measure(() -> blackhole(scenario.run(scenario.measureUnits(), WORKER_THREADS)));

            // Snapshot foreign EventStream thread ids before this consumer starts.
            OwnedConsumerCpu.markForeignEventStreams();
            consumer.start();
            awaitAttached(registry, Duration.ofSeconds(15));
            OwnedConsumerCpu.beginSession();
            Sample enabledSample = measure(() -> blackhole(scenario.run(scenario.measureUnits(), WORKER_THREADS)));
            // Drop pairs where owned threads exited/replaced mid-sample (incomplete CPU).
            if (!enabledSample.valid()) {
                continue;
            }
            baselines.add(baseline);
            enabled.add(enabledSample);
        }
        if (baselines.size() < SAMPLES) {
            throw new AssertionError(
                    "only " + baselines.size() + "/" + SAMPLES
                            + " valid consumer-CPU samples after " + attempts + " attempts");
        }

        Thread.sleep(1_200L);
        Diagnostics diagnostics = Diagnostics.capture(registry);
        return ScenarioResult.from(scenario, baselines, enabled, diagnostics);
    }

    private static Sample measure(Runnable work) {
        GcSnapshot gcBefore = GcSnapshot.capture();
        long processCpuBefore = processCpuNanos();
        OwnedConsumerCpu.clearIncomplete();
        long consumerCpuBefore = OwnedConsumerCpu.totalCpuNanos();
        long wallBefore = System.nanoTime();
        WORKLOAD_ALLOC_BYTES.set(-1L);

        java.util.concurrent.atomic.AtomicBoolean sampling = new java.util.concurrent.atomic.AtomicBoolean(true);
        Thread sampler = new Thread(() -> {
            while (sampling.get()) {
                OwnedConsumerCpu.totalCpuNanos();
                try {
                    Thread.sleep(1L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "jfr-perf-cpu-sampler");
        sampler.setDaemon(true);
        sampler.start();
        try {
            work.run();
        } finally {
            sampling.set(false);
            try {
                sampler.join(2_000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long wall = System.nanoTime() - wallBefore;
        long processCpu = processCpuNanos() - processCpuBefore;
        long consumerCpu = Math.max(0L, OwnedConsumerCpu.totalCpuNanos() - consumerCpuBefore);
        boolean valid = !OwnedConsumerCpu.isIncomplete();
        long allocated = WORKLOAD_ALLOC_BYTES.get();
        GcSnapshot gcAfter = GcSnapshot.capture();
        return new Sample(
                wall,
                Math.max(0L, processCpu),
                consumerCpu,
                allocated,
                gcAfter.minus(gcBefore),
                valid);
    }

    private static JfrMetricsProperties enabledProperties() {
        JfrMetricsProperties properties = new JfrMetricsProperties();
        properties.setEnabled(true);
        properties.getPublish().setFlushInterval(Duration.ofSeconds(1));
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
        sb.append("- Worker threads (LOCK/MIXED): `").append(WORKER_THREADS).append("`\n\n");
        sb.append("Both legs use the same disk Flight Recording. Delta ≈ consume-side cost only.\n\n");
        sb.append("## Reading the ratios\n\n");
        sb.append("- `wall/process CPU/alloc ratio ≈ 1.0` → consume-side overhead negligible on that workload\n");
        sb.append("- `consumer CPU p50` tracks session-owned thread ids (`jfr-metrics-consumer` + EventStream ids born after pre-start mark); 1ms sampler; pairs where owned threads exit/replace mid-sample are **invalidated and retried**\n");
        sb.append("- EventStream worker identity is not a public JDK API; ownership is appearance-after-mark (best effort). Baseline measures residual owned ids after stop\n");
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
     * during work). Mid-sample owned exit/replace sets {@link #isIncomplete()} so the
     * harness can invalidate that sample. {@link #endSession()} freezes adoption but keeps
     * owned ids so baseline legs measure residual CPU honestly.
     */
    private static final class OwnedConsumerCpu {
        private static final java.util.Set<Long> FOREIGN = java.util.concurrent.ConcurrentHashMap.newKeySet();
        private static final java.util.Set<Long> OWNED = java.util.concurrent.ConcurrentHashMap.newKeySet();
        private static final java.util.Set<Long> LIVE = java.util.concurrent.ConcurrentHashMap.newKeySet();
        private static final java.util.concurrent.ConcurrentHashMap<Long, Long> LAST_CPU =
                new java.util.concurrent.ConcurrentHashMap<>();
        private static final java.util.concurrent.atomic.AtomicBoolean INCOMPLETE =
                new java.util.concurrent.atomic.AtomicBoolean();
        private static volatile boolean sessionActive;

        private OwnedConsumerCpu() {
        }

        static void clearIncomplete() {
            INCOMPLETE.set(false);
        }

        static boolean isIncomplete() {
            return INCOMPLETE.get();
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
            LIVE.clear();
            LAST_CPU.clear();
            INCOMPLETE.set(false);
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
            INCOMPLETE.set(false);
        }

        static long totalCpuNanos() {
            if (OWNED.isEmpty() && !sessionActive) {
                return 0L;
            }
            if (sessionActive) {
                adoptReplacementsIfNeeded();
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
                // Replacement; refreshLastCpu already flags INCOMPLETE when the prior id left LIVE.
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
                    LIVE.add(id);
                    LAST_CPU.merge(id, cpu, Math::max);
                } else if (sessionActive && LIVE.remove(id)) {
                    // Owned thread was live earlier in this sample and has now exited.
                    INCOMPLETE.set(true);
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
        LOCK("multi-thread contended ReentrantLock (JavaMonitorEnter pressure)") {
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
        MIXED("multi-thread mix of alloc + CPU + short locks") {
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
            ReentrantLock lock = new ReentrantLock();
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
                                lock.lock();
                                try {
                                    local ^= i;
                                    Thread.onSpinWait();
                                } finally {
                                    lock.unlock();
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
            boolean valid) {
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

    private record ScenarioResult(Scenario scenario, LegStats baseline, LegStats enabled) {
        static ScenarioResult from(
                Scenario scenario,
                List<Sample> baselines,
                List<Sample> enabledSamples,
                Diagnostics diagnostics) {
            // Baseline has no consumer; still reuse diagnostics shape with attached=false.
            LegStats baseline = LegStats.from(baselines, new Diagnostics(false, 0L, 0L));
            LegStats enabled = LegStats.from(enabledSamples, diagnostics);
            return new ScenarioResult(scenario, baseline, enabled);
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
