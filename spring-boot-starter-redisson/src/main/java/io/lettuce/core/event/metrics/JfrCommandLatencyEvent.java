package io.lettuce.core.event.metrics;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;

/**
 * Lettuce 的 JfrEventRecorder 会自动扫描事件对应的 JFR 事件是否存在，格式是原始类的包 + "Jfr" + 原始类名字
 * 如果存在则自动调用其构造器生成这个 JFR 事件
 * @see io.lettuce.core.event.jfr.JfrEventRecorder
 */
@Category({ "Lettuce", "Command Events" })
@Label("Command Latency Trigger")
@StackTrace(false)
public class JfrCommandLatencyEvent extends Event {
    private final int size;

    public JfrCommandLatencyEvent(CommandLatencyEvent commandLatencyEvent) {
        this.size = commandLatencyEvent.getLatencies().size();
        commandLatencyEvent.getLatencies().forEach((commandLatencyId, commandMetrics) -> {
            JfrCommandLatency jfrCommandLatency = new JfrCommandLatency(commandLatencyId, commandMetrics);
            jfrCommandLatency.commit();
        });
    }
}
