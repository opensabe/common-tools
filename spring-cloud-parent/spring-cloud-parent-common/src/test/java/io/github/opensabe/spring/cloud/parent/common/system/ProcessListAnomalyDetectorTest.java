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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProcessListAnomalyDetector rules")
class ProcessListAnomalyDetectorTest {

    private static final long SELF = 100L;

    @Test
    @DisplayName("skips self pid and empty cmdline")
    void skipsSelfAndEmptyCmdline() {
        ProcessListAnomalyDetector detector = new ProcessListAnomalyDetector();
        List<String> anomalies = detector.findAnomalies(SELF, List.of(
                new ProcessSnapshot(SELF, 1L, "java -jar app.jar"),
                new ProcessSnapshot(101L, 1L, ""),
                new ProcessSnapshot(102L, 1L, "   ")
        ));
        assertTrue(anomalies.isEmpty());
    }

    @Test
    @DisplayName("allows jcmd JVM child; flags unexpected JVM children")
    void jvmChildRules() {
        ProcessListAnomalyDetector detector = new ProcessListAnomalyDetector();
        List<String> anomalies = detector.findAnomalies(SELF, List.of(
                new ProcessSnapshot(200L, SELF, "jcmd 100 GC.heap_info"),
                new ProcessSnapshot(201L, SELF, "/usr/bin/jcmd 100 VM.native_memory"),
                new ProcessSnapshot(202L, SELF, "sh -c curl http://evil"),
                new ProcessSnapshot(203L, SELF, "curl http://evil")
        ));
        assertEquals(2, anomalies.size());
        assertTrue(anomalies.stream().allMatch(a -> a.startsWith("unexpected_jvm_child:")));
    }

    @Test
    @DisplayName("allow-jvm-child-substrings extends JVM child allow list")
    void configurableJvmChildAllow() {
        ProcessListMonitorProperties props = new ProcessListMonitorProperties();
        props.setAllowJvmChildSubstrings(List.of("/opt/tools/helper"));
        ProcessListAnomalyDetector detector = new ProcessListAnomalyDetector(props);
        List<String> anomalies = detector.findAnomalies(SELF, List.of(
                new ProcessSnapshot(210L, SELF, "/opt/tools/helper --once")
        ));
        assertTrue(anomalies.isEmpty());
    }

    @Test
    @DisplayName("allow-substrings and allow-base-commands append to built-ins")
    void configurableAllowLists() {
        ProcessListMonitorProperties props = new ProcessListMonitorProperties();
        props.setAllowSubstrings(List.of("/opt/sidecar/agent"));
        props.setAllowBaseCommands(List.of("rclone"));
        ProcessListAnomalyDetector detector = new ProcessListAnomalyDetector(props);

        List<String> anomalies = detector.findAnomalies(SELF, List.of(
                new ProcessSnapshot(300L, 1L, "/opt/sidecar/agent --config=/etc/agent.yaml"),
                new ProcessSnapshot(301L, 1L, "rclone copy /project/app/log/ :s3://bucket/path")
        ));
        assertTrue(anomalies.isEmpty());
    }

    @Test
    @DisplayName("sh -c is not allowed via base command whitelist")
    void shellDashCNotAllowedByBase() {
        ProcessListAnomalyDetector detector = new ProcessListAnomalyDetector();
        List<String> anomalies = detector.findAnomalies(SELF, List.of(
                new ProcessSnapshot(400L, 1L, "bash -c 'python /tmp/x.py'"),
                new ProcessSnapshot(401L, 1L, "sh")
        ));
        assertEquals(1, anomalies.size());
        assertTrue(anomalies.get(0).startsWith("deny_pattern:"));
    }

    @Test
    @DisplayName("unconfigured rclone is deny; built-in java -jar is allowed")
    void rcloneAndJavaJar() {
        ProcessListAnomalyDetector detector = new ProcessListAnomalyDetector();
        List<String> anomalies = detector.findAnomalies(SELF, List.of(
                new ProcessSnapshot(500L, 1L, "rclone copy /tmp/dump :s3://bucket/path"),
                new ProcessSnapshot(501L, 1L, "java -jar /app/service.jar"),
                new ProcessSnapshot(502L, 1L, "/nobody/bin/start.sh")
        ));
        assertEquals(1, anomalies.size());
        assertTrue(anomalies.get(0).contains("rclone"));
    }

    @Test
    @DisplayName("deny-substrings append to built-in deny list")
    void configurableDeny() {
        ProcessListMonitorProperties props = new ProcessListMonitorProperties();
        props.setDenySubstrings(List.of("evil-tool"));
        ProcessListAnomalyDetector detector = new ProcessListAnomalyDetector(props);
        List<String> anomalies = detector.findAnomalies(SELF, List.of(
                new ProcessSnapshot(600L, 1L, "evil-tool --run")
        ));
        assertEquals(1, anomalies.size());
        assertTrue(anomalies.get(0).startsWith("deny_pattern:"));
    }

    @Test
    @DisplayName("allow wins over deny for configured substrings")
    void allowBeforeDeny() {
        ProcessListMonitorProperties props = new ProcessListMonitorProperties();
        props.setAllowSubstrings(List.of("aws s3 sync /project/log"));
        ProcessListAnomalyDetector detector = new ProcessListAnomalyDetector(props);
        List<String> anomalies = detector.findAnomalies(SELF, List.of(
                new ProcessSnapshot(700L, 1L, "aws s3 sync /project/log /backup")
        ));
        assertTrue(anomalies.isEmpty());
    }

    @Test
    @DisplayName("baseCommand extracts basename")
    void baseCommand() {
        assertEquals("rclone", ProcessListAnomalyDetector.baseCommand("/usr/bin/rclone copy a b"));
        assertEquals("jcmd", ProcessListAnomalyDetector.baseCommand("jcmd 1 GC.run"));
        assertEquals(null, ProcessListAnomalyDetector.baseCommand(""));
        assertFalse(new ProcessListAnomalyDetector().isAllowedJvmChild("curl http://x"));
        assertTrue(new ProcessListAnomalyDetector().isAllowedJvmChild("jcmd 1 VM.info"));
    }
}
