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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads process snapshots from /proc without forking {@code ps}.
 */
public final class ProcessListReader {

    static final int MAX_CMDLINE_BYTES = 64 * 1024;

    private final Path procRoot;

    public ProcessListReader() {
        this(Path.of("/proc"));
    }

    public ProcessListReader(Path procRoot) {
        this.procRoot = procRoot;
    }

    public boolean isAvailable() {
        return Files.isDirectory(procRoot);
    }

    public List<ProcessSnapshot> readAll() throws IOException {
        List<ProcessSnapshot> result = new ArrayList<>();
        if (!isAvailable()) {
            return result;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(procRoot)) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                if (!isNumeric(name)) {
                    continue;
                }
                long pid;
                try {
                    pid = Long.parseLong(name);
                } catch (NumberFormatException e) {
                    continue;
                }
                Path statusPath = entry.resolve("status");
                Path cmdlinePath = entry.resolve("cmdline");
                if (!Files.isRegularFile(statusPath) || !Files.isRegularFile(cmdlinePath)) {
                    continue;
                }
                Long ppid = readPpid(statusPath);
                if (ppid == null) {
                    continue;
                }
                String cmdline = readCmdline(cmdlinePath);
                result.add(new ProcessSnapshot(pid, ppid, cmdline));
            }
        }
        return result;
    }

    static Long readPpid(Path statusPath) throws IOException {
        List<String> lines = Files.readAllLines(statusPath, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.startsWith("PPid:")) {
                String value = line.substring("PPid:".length()).trim();
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    static String readCmdline(Path cmdlinePath) throws IOException {
        byte[] bytes = Files.readAllBytes(cmdlinePath);
        if (bytes.length == 0) {
            return "";
        }
        int length = Math.min(bytes.length, MAX_CMDLINE_BYTES);
        String raw = new String(bytes, 0, length, StandardCharsets.UTF_8);
        return raw.replace('\0', ' ').trim();
    }

    private static boolean isNumeric(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isDigit(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
