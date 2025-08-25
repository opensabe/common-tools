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
package io.github.opensabe.spring.cloud.parent.common.system.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Category({"Native Memory Tracking"})
@Label("Smaps Rollup")
@NoArgsConstructor
@Getter
@Setter
public class SmapsJfrEvent extends Event {

    private long rss;
    private long pss;
    private long pssAnon;
    private long pssFile;
    private long pssShmem;
    private long sharedClean;
    private long sharedDirty;
    private long privateClean;
    private long privateDirty;
    private long referenced;
    private long anonymous;
    private long lazyFree;
    private long anonHugePages;
    private long shmemPmdMapped;
    private long filePmdMapped;
    private long sharedHugetlb;
    private long privateHugetlb;
    private long swap;
    private long swapPss;
    private long locked;
}
