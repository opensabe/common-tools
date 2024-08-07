package io.github.opensabe.spring.cloud.parent.common.system;

import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.cloud.parent.common.config.OnlyOnceApplicationListener;
import io.github.opensabe.spring.cloud.parent.common.system.jfr.MemoryStatJfrEvent;
import io.github.opensabe.spring.cloud.parent.common.system.jfr.MemorySwStatJfrEvent;
import io.github.opensabe.spring.cloud.parent.common.system.jfr.NativeMemoryTrackingJfrEvent;
import io.github.opensabe.spring.cloud.parent.common.system.jfr.OOMScoreJfrEvent;
import io.github.opensabe.spring.cloud.parent.common.system.jfr.SmapsJfrEvent;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 内存监控，用于指导内存分配
 */
@Log4j2
public class MonitorMemoryRSS extends OnlyOnceApplicationListener<ApplicationReadyEvent> {
    private static final ScheduledThreadPoolExecutor sc = new ScheduledThreadPoolExecutor(1);

    @Override
    protected void onlyOnce(ApplicationReadyEvent event) {
        sc.scheduleAtFixedRate(() -> {
            long pid = ProcessHandle.current().pid();
            try {
                smapsProcess(pid);
                memoryStatProcess();
                memorySwStatProcess();
                //native memory tracking 是看 JVM 自己认为自己申请了多少，以及每一部分
//                Process process = Runtime.getRuntime().exec(new String[]{"jcmd", pid + "", "VM.native_memory"});
//                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                    log.info("MonitorMemoryRSS, native_memory: {}", reader.lines().collect(Collectors.joining("\n")));
//                }
                nativeMemoryTrackingProcess(pid);
                //oom score
                OOMScoreRecording(pid);
            } catch (IOException ignore) {
            }

        }, 0, 30, TimeUnit.SECONDS);
    }

    private void smapsProcess(long pid) throws IOException {
        //smaps 中是实际内存映射占用
        List<String> strings = FileUtils.readLines(new File("/proc/" + pid + "/smaps_rollup"), Charset.defaultCharset());
        SmapsJfrEvent event = new SmapsJfrEvent();
        strings.stream().skip(1).forEach(s -> {
            String[] split = s.replaceAll(" ", "").split(":");
            String fieldName = split[0].replaceAll("_", "");
            long value = Long.parseLong(split[1].replaceAll("kB", ""));
            try {
                Method setter = SmapsJfrEvent.class.getMethod("set" + fieldName, long.class);
                dynamicSetter(event, setter, value);
            } catch (NoSuchMethodException ignore) {
            }
        });
        event.commit();
        log.info("MonitorMemoryRSS, smapsRollup: {}", StringUtils.join(strings, "\n"));
    }

    private void memorySwStatProcess() throws IOException {
        List<String> usageInBytes = FileUtils.readLines(new File("/sys/fs/cgroup/memory/memory.memsw.usage_in_bytes"), Charset.defaultCharset());
        List<String> maxUsageInBytes = FileUtils.readLines(new File("/sys/fs/cgroup/memory/memory.memsw.max_usage_in_bytes"), Charset.defaultCharset());
        List<String> limitInBytes = FileUtils.readLines(new File("/sys/fs/cgroup/memory/memory.memsw.limit_in_bytes"), Charset.defaultCharset());
        MemorySwStatJfrEvent event = new MemorySwStatJfrEvent(Long.parseLong(usageInBytes.get(0)), Long.parseLong(maxUsageInBytes.get(0)), Long.parseLong(limitInBytes.get(0)));
        event.commit();
        log.info("MonitorMemoryRSS, memorySwStat: {}", JsonUtil.toJSONString(event));
    }

    private void memoryStatProcess() throws IOException {
        List<String> strings = FileUtils.readLines(new File("/sys/fs/cgroup/memory/memory.stat"), Charset.defaultCharset());
        MemoryStatJfrEvent event = new MemoryStatJfrEvent();
        strings.forEach( s -> {
            String[] split = s.split(" ");
            String fieldName = Stream.of(split[0].split("_")).map(s1 -> s1.substring(0, 1).toUpperCase() + s1.substring(1)).collect(Collectors.joining());
            long value = Long.parseLong(split[1]);
            try {
                Method setter = MemoryStatJfrEvent.class.getMethod("set" + fieldName, long.class);
                dynamicSetter(event, setter, value);
            } catch (NoSuchMethodException e) {
//                log.error("memoryStatProcess, NoSuchMethodException: {} not find", fieldName, e);
            }
        });
        event.commit();
        log.info("MonitorMemoryRSS, memoryStat: {}", StringUtils.join(strings, "\n"));
    }

    private void OOMScoreRecording(long pid) {
        //oom 三个分数  /proc/<pid>/oom_adj, /proc/<pid>/oom_score, /proc/<pid>/oom_score_adj 由于与memory都有关，合并到这个方法
        //oom_adj
        try {
            List<String> oomAdjList = FileUtils.readLines(new File("/proc/" + pid + "/oom_adj"), Charset.defaultCharset());
            //oom_score
            List<String> oomScoreList = FileUtils.readLines(new File("/proc/" + pid + "/oom_score"), Charset.defaultCharset());
            //oom_score_adj
            List<String> oomScoreAdjList = FileUtils.readLines(new File("/proc/" + pid + "/oom_score_adj"), Charset.defaultCharset());
            //oom_adj    oom_score   oom_score_adj  list实际只要一个数值
            long oomAdj = oomAdjList.isEmpty() ? 0L :(oomAdjList.get(0) != null ? Long.parseLong(oomAdjList.get(0)) : 0L);
            long oomScore = oomScoreList.isEmpty() ? 0L : (oomScoreList.get(0) != null ? Long.parseLong(oomScoreList.get(0)) : 0L);
            long oomScoreAdj = oomScoreAdjList.isEmpty() ? 0L : (oomScoreAdjList.get(0) != null ? Long.parseLong(oomScoreAdjList.get(0)) : 0L) ;
            log.info("Monitoring OOM Score, oom_adj: {} , oom_score: {} ,  oom_score_adj :{} .", oomAdj, oomScore, oomScoreAdj);
            //  /proc/<pid>/oom_adj, /proc/<pid>/oom_score, /proc/<pid>/oom_score_adj  将三个文件的内容抽象为一个 JFR 事件的三个字段生成 JFR 事件
            OOMScoreJfrEventProcess(oomAdj, oomScore, oomScoreAdj);
        } catch (Throwable e) {
            log.error("Retrieve OOM Score from /proc/pid/oom exception!", e);
        }
    }

    private void OOMScoreJfrEventProcess(long oomAdj, long oomScore, long oomScoreAdj) {
        try {
            OOMScoreJfrEvent oomScoreJfrEvent = new OOMScoreJfrEvent(oomAdj, oomScore, oomScoreAdj);
            oomScoreJfrEvent.commit();
        } catch (Throwable e) {
            log.error("Process OOM Score Jfr Event log exception ", e);
        }
    }

    private void nativeMemoryTrackingProcess(long pid) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"jcmd", pid + "", "VM.native_memory"});
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                List<String> nmt = reader.lines().collect(Collectors.toList());
                log.info("MonitorMemoryRSS, native_memory: {}", String.join("\n", nmt));
                nmt.forEach(line -> {
                    if (line.contains("Total:")) {
                        String[] arr = line.trim().substring(line.indexOf(": ") + 1).split(",");
                        NativeMemoryTrackingJfrEvent nativeMemoryTrackingJfrEvent = new NativeMemoryTrackingJfrEvent("Total",
                                Long.parseLong(arr[0].substring(arr[0].indexOf('=') + 1, arr[0].lastIndexOf("KB"))),
                                Long.parseLong(arr[1].substring(arr[1].indexOf('=') + 1, arr[1].lastIndexOf("KB"))));
                        nativeMemoryTrackingJfrEvent.commit();
                    }
                    if (!line.isEmpty() && line.charAt(0) == '-') {
                        String newLine = line.substring(1).trim();
                        String name = newLine.substring(0, newLine.indexOf('(')).trim();
                        String data = newLine.substring(newLine.indexOf('(') + 1, newLine.indexOf(')')).trim();
                        String[] arr = data.trim().substring(data.indexOf(": ") + 1).split(",");
                        NativeMemoryTrackingJfrEvent nativeMemoryTrackingJfrEvent = new NativeMemoryTrackingJfrEvent(name,
                                Long.parseLong(arr[0].substring(arr[0].indexOf('=') + 1, arr[0].lastIndexOf("KB"))),
                                Long.parseLong(arr[1].substring(arr[1].indexOf('=') + 1, arr[1].lastIndexOf("KB"))));
                        nativeMemoryTrackingJfrEvent.commit();
                    }
                });
            }
        } catch (Throwable e) {
            log.error("Process Native Memory Tracking log exception ", e);
        }
    }

    private void dynamicSetter(Object targetClass, Method method, long value) {
        try {
            method.invoke(targetClass, value);
        }catch (Exception e){
            log.error("Dynamic setter exception: method {}", method.getName(), e);
        }
    }
}
