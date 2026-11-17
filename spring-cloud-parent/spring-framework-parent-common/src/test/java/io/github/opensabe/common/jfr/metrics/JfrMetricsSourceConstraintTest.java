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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Production consumer must stay passive: no RecordingStream and no enable/disable of JFR events.
 */
class JfrMetricsSourceConstraintTest {

    @Test
    void productionSourcesDoNotStartOrConfigureRecording() throws IOException {
        Path dir = Path.of("src/main/java/io/github/opensabe/common/jfr/metrics");
        if (!Files.isDirectory(dir)) {
            fail("missing metrics package: " + dir.toAbsolutePath());
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            List<Path> javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
            assertFalse(javaFiles.isEmpty());
            for (Path file : javaFiles) {
                String source = Files.readString(file);
                assertFalse(source.contains("RecordingStream"), file + " must not use RecordingStream");
                assertFalse(source.contains(".enable("), file + " must not call enable(");
                assertFalse(source.contains(".disable("), file + " must not call disable(");
                assertFalse(source.contains(".withPeriod("), file + " must not call withPeriod(");
                assertFalse(source.contains(".withThreshold("), file + " must not call withThreshold(");
                assertFalse(source.contains(".withStackTrace("), file + " must not call withStackTrace(");
                assertFalse(source.contains(".withoutStackTrace("), file + " must not call withoutStackTrace(");
            }
        }
    }
}
