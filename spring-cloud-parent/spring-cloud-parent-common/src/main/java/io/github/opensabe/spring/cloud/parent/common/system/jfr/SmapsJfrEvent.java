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
