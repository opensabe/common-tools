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
package io.github.opensabe.common.compat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FastjsonCompatBridge compat-libs 路径解析")
class FastjsonCompatBridgePathTest {

    @Test
    @DisplayName("resolve 到含三件套 JAR 的目录（不依赖进程 cwd）")
    void resolveFindsCopiedJars() {
        Path dir = FastjsonCompatBridge.requireCompatLibsDir();
        assertTrue(Files.isDirectory(dir), dir.toString());
        assertTrue(dir.toAbsolutePath().normalize().endsWith(Path.of("target", "compat-libs")));
    }
}
