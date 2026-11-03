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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Pure process-list anomaly rules (unit-test friendly).
 * <p>
 * Allows container-normal processes and common-tools {@code jcmd} children;
 * focuses on unexpected JVM children and deny-list commands.
 * Config allow/deny lists are appended to built-ins (never replace).
 */
public final class ProcessListAnomalyDetector {

    private static final String[] ALLOWED_SUBSTRINGS = {
            "/bin/init.sh",
            "/nobody/bin/start.sh",
            "start.sh",
            "init.sh",
            "-jar ",
            "/usr/bin/tee",
            "coreutils-prog-shebang=tee",
            "/usr/bin/sleep",
            "coreutils-prog-shebang=sleep",
            "jcmd",
    };

    private static final String[] ALLOWED_EXACT_BASE_COMMANDS = {
            "sh",
            "bash",
            "ps",
            "grep",
            "sleep",
            "tee",
            "jcmd",
    };

    /**
     * Hit means suspicious (matched against lower-cased cmdline).
     * Avoid very short tokens to reduce false positives.
     */
    private static final String[] DENY_SUBSTRINGS = {
            "python",
            "curl ",
            "curl\t",
            "wget ",
            "wget\t",
            " ncat",
            "ncat ",
            "socat",
            "/tmp/",
            "base64",
            " aws ",
            "aws ",
            "git-codecommit",
            "busybox wget",
            "nc -",
            "/bin/nc",
            "perl -",
            "ruby -",
            "php -",
    };

    private final List<String> extraAllowSubstrings;
    private final List<String> extraAllowBaseCommands;
    private final List<String> extraAllowJvmChildSubstrings;
    private final List<String> extraDenySubstrings;

    public ProcessListAnomalyDetector(ProcessListMonitorProperties properties) {
        this.extraAllowSubstrings = normalizeList(properties == null ? null : properties.getAllowSubstrings());
        this.extraAllowBaseCommands = normalizeList(properties == null ? null : properties.getAllowBaseCommands());
        this.extraAllowJvmChildSubstrings = normalizeList(
                properties == null ? null : properties.getAllowJvmChildSubstrings());
        this.extraDenySubstrings = normalizeList(properties == null ? null : properties.getDenySubstrings());
    }

    public ProcessListAnomalyDetector() {
        this(null);
    }

    /**
     * @param selfPid   current JVM pid
     * @param processes container process snapshots
     * @return anomaly descriptions; empty means OK
     */
    public List<String> findAnomalies(long selfPid, List<ProcessSnapshot> processes) {
        List<String> anomalies = new ArrayList<>();
        if (processes == null || processes.isEmpty()) {
            return anomalies;
        }
        for (ProcessSnapshot process : processes) {
            if (process == null) {
                continue;
            }
            if (process.getPid() == selfPid) {
                continue;
            }
            String cmdline = process.getCmdline();
            String normalized = normalize(cmdline);
            if (normalized.isEmpty()) {
                continue;
            }
            // JVM children: only jcmd (MonitorMemoryRSS) + configured extras
            if (process.getPpid() == selfPid) {
                if (!isAllowedJvmChild(normalized)) {
                    anomalies.add("unexpected_jvm_child: " + process);
                }
                continue;
            }
            if (isAllowed(normalized, cmdline)) {
                continue;
            }
            // Unallowed rclone is always suspicious
            if ("rclone".equalsIgnoreCase(baseCommand(cmdline))) {
                anomalies.add("deny_pattern: " + process);
                continue;
            }
            if (matchesDeny(normalized)) {
                anomalies.add("deny_pattern: " + process);
            }
        }
        return anomalies;
    }

    boolean isAllowedJvmChild(String normalizedCmdline) {
        if (normalizedCmdline.startsWith("jcmd")
                || normalizedCmdline.contains("/jcmd ")
                || normalizedCmdline.contains("/jcmd\t")) {
            return true;
        }
        for (String allowed : extraAllowJvmChildSubstrings) {
            if (normalizedCmdline.contains(allowed)) {
                return true;
            }
        }
        return false;
    }

    boolean isAllowed(String normalizedCmdline, String originalCmdline) {
        if (normalizedCmdline.startsWith("java ") || normalizedCmdline.contains(" java ")) {
            if (normalizedCmdline.contains("-jar")) {
                return true;
            }
        }
        if (isAllowedJvmChild(normalizedCmdline)) {
            return true;
        }
        for (String allowed : ALLOWED_SUBSTRINGS) {
            if (normalizedCmdline.contains(allowed.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        for (String allowed : extraAllowSubstrings) {
            if (normalizedCmdline.contains(allowed)) {
                return true;
            }
        }
        String base = baseCommand(originalCmdline);
        if (base == null) {
            return false;
        }
        String baseLower = base.toLowerCase(Locale.ROOT);
        // Interactive shells OK; scripted -c does not use base allow (deny / JVM-child rules)
        if (("sh".equals(baseLower) || "bash".equals(baseLower))
                && (normalizedCmdline.contains(" -c ") || normalizedCmdline.endsWith(" -c"))) {
            return false;
        }
        for (String allowedBase : ALLOWED_EXACT_BASE_COMMANDS) {
            if (allowedBase.equals(baseLower)) {
                return true;
            }
        }
        for (String allowedBase : extraAllowBaseCommands) {
            if (allowedBase.equals(baseLower)) {
                return true;
            }
        }
        return false;
    }

    boolean matchesDeny(String normalizedCmdline) {
        for (String deny : DENY_SUBSTRINGS) {
            if (normalizedCmdline.contains(deny)) {
                return true;
            }
        }
        for (String deny : extraDenySubstrings) {
            if (normalizedCmdline.contains(deny)) {
                return true;
            }
        }
        return false;
    }

    static String normalize(String cmdline) {
        if (cmdline == null) {
            return "";
        }
        return cmdline.replace('\0', ' ').trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Basename of the first cmdline token (path stripped).
     */
    static String baseCommand(String cmdline) {
        if (cmdline == null || cmdline.isBlank()) {
            return null;
        }
        String first = cmdline.replace('\0', ' ').trim();
        int space = first.indexOf(' ');
        if (space > 0) {
            first = first.substring(0, space);
        }
        int slash = Math.max(first.lastIndexOf('/'), first.lastIndexOf('\\'));
        if (slash >= 0 && slash + 1 < first.length()) {
            first = first.substring(slash + 1);
        }
        return first.isEmpty() ? null : first;
    }

    private static List<String> normalizeList(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>(raw.size());
        for (String item : raw) {
            if (item == null) {
                continue;
            }
            String trimmed = item.trim().toLowerCase(Locale.ROOT);
            if (!trimmed.isEmpty()) {
                out.add(trimmed);
            }
        }
        return Collections.unmodifiableList(out);
    }
}
