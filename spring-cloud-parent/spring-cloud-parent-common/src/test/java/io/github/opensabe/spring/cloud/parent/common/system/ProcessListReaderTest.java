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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProcessListReader /proc parsing")
class ProcessListReaderTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("reads PPid from status and null-separated cmdline")
    void readsPpidAndCmdline() throws Exception {
        Path proc = tempDir.resolve("proc");
        Path pidDir = proc.resolve("42");
        Files.createDirectories(pidDir);
        Files.writeString(pidDir.resolve("status"), """
                Name:\tjava
                State:\tS (sleeping)
                Tgid:\t42
                Pid:\t42
                PPid:\t7
                """, StandardCharsets.UTF_8);
        byte[] cmdline = "java\0-jar\0/app/service.jar\0".getBytes(StandardCharsets.UTF_8);
        Files.write(pidDir.resolve("cmdline"), cmdline);

        Path other = proc.resolve("cpuinfo");
        Files.writeString(other, "processor");

        ProcessListReader reader = new ProcessListReader(proc);
        assertTrue(reader.isAvailable());
        List<ProcessSnapshot> snapshots = reader.readAll();
        assertEquals(1, snapshots.size());
        ProcessSnapshot snap = snapshots.get(0);
        assertEquals(42L, snap.getPid());
        assertEquals(7L, snap.getPpid());
        assertEquals("java -jar /app/service.jar", snap.getCmdline());
    }

    @Test
    @DisplayName("missing proc root is unavailable and returns empty")
    void missingProc() throws Exception {
        Path missing = tempDir.resolve("no-proc");
        ProcessListReader reader = new ProcessListReader(missing);
        assertFalse(reader.isAvailable());
        assertTrue(reader.readAll().isEmpty());
    }

    @Test
    @DisplayName("empty cmdline yields empty string")
    void emptyCmdline() throws Exception {
        Path proc = tempDir.resolve("proc2");
        Path pidDir = proc.resolve("9");
        Files.createDirectories(pidDir);
        Files.writeString(pidDir.resolve("status"), "Name:\tkthreadd\nPPid:\t0\n", StandardCharsets.UTF_8);
        Files.write(pidDir.resolve("cmdline"), new byte[0]);

        ProcessListReader reader = new ProcessListReader(proc);
        List<ProcessSnapshot> snapshots = reader.readAll();
        assertEquals(1, snapshots.size());
        assertEquals("", snapshots.get(0).getCmdline());
        assertEquals(0L, snapshots.get(0).getPpid());
    }
}
