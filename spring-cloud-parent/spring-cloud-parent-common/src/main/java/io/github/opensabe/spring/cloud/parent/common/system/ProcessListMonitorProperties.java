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
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Suspicious process-list monitor. Default {@code enabled=false} keeps upgrades no-op.
 */
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "opensabe.process-monitor")
public class ProcessListMonitorProperties {

    /**
     * Master switch. Default false for compatibility.
     */
    private boolean enabled = false;

    /**
     * Scan interval in seconds.
     */
    private long intervalSeconds = 5L;

    /**
     * Dedup window for AlarmUtil.fatal fingerprint.
     */
    private int alarmDedupSeconds = 60;

    /**
     * Extra cmdline substrings (case-insensitive), appended to built-in allow list.
     */
    private List<String> allowSubstrings = new ArrayList<>();

    /**
     * Extra argv0 basenames (exact, case-insensitive), appended to built-in allow list.
     */
    private List<String> allowBaseCommands = new ArrayList<>();

    /**
     * Extra substrings allowed for direct JVM children; built-in only allows jcmd.
     */
    private List<String> allowJvmChildSubstrings = new ArrayList<>();

    /**
     * Extra suspicious substrings, appended to built-in deny list.
     */
    private List<String> denySubstrings = new ArrayList<>();
}
