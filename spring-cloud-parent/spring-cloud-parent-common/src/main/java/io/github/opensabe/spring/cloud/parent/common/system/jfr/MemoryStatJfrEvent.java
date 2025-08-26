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
@Label("Memory Stat")
@NoArgsConstructor
@Getter
@Setter
public class MemoryStatJfrEvent extends Event {

    private long cache;
    private long rss;
    private long rssHuge;
    private long shmem;
    private long mappedFile;
    private long dirty;
    private long writeback;
    private long swap;
    private long pgpgin;
    private long pgpgout;
    private long pgfault;
    private long pgmajfault;
    private long inactiveAnon;
    private long activeAnon;
    private long inactiveFile;
    private long activeFile;
    private long unevictable;
    private long hierarchicalMemoryLimit;
    private long hierarchicalMemswLimit;
    private long totalCache;
    private long totalRss;
    private long totalRssHuge;
    private long totalShmem;
    private long totalMappedFile;
    private long totalDirty;
    private long totalWriteback;
    private long totalSwap;
    private long totalPgpgin;
    private long totalPgpgout;
    private long totalPgfault;
    private long totalPgmajfault;
    private long totalInactiveAnon;
    private long totalActiveAnon;
    private long totalInactiveFile;
    private long totalActiveDFile;
    private long totalUnevictable;
}
