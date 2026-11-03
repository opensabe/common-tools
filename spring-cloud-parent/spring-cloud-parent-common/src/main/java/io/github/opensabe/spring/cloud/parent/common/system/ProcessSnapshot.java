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

/**
 * One process snapshot from /proc.
 */
public final class ProcessSnapshot {

    private final long pid;
    private final long ppid;
    private final String cmdline;

    public ProcessSnapshot(long pid, long ppid, String cmdline) {
        this.pid = pid;
        this.ppid = ppid;
        this.cmdline = cmdline == null ? "" : cmdline;
    }

    public long getPid() {
        return pid;
    }

    public long getPpid() {
        return ppid;
    }

    public String getCmdline() {
        return cmdline;
    }

    @Override
    public String toString() {
        return "pid=" + pid + " ppid=" + ppid + " cmd=" + cmdline;
    }
}
