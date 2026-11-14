package io.github.opensabe.spring.boot.starter.rocketmq;

/**
 * RocketMQ 经典延时等级（对应 Broker messageDelayLevel 默认 18 级）。
 * 实际时长以 Broker 配置为准，默认：
 * 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
 */
public enum MessageDelayLevel {
    LEVEL_1(1),   // 1s
    LEVEL_2(2),   // 5s
    LEVEL_3(3),   // 10s
    LEVEL_4(4),   // 30s
    LEVEL_5(5),   // 1m
    LEVEL_6(6),   // 2m
    LEVEL_7(7),   // 3m
    LEVEL_8(8),   // 4m
    LEVEL_9(9),   // 5m
    LEVEL_10(10), // 6m
    LEVEL_11(11), // 7m
    LEVEL_12(12), // 8m
    LEVEL_13(13), // 9m
    LEVEL_14(14), // 10m
    LEVEL_15(15), // 20m
    LEVEL_16(16), // 30m
    LEVEL_17(17), // 1h
    LEVEL_18(18); // 2h

    private final int level;

    MessageDelayLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static MessageDelayLevel of(int level) {
        for (MessageDelayLevel value : values()) {
            if (value.level == level) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unsupported delayLevel: " + level + ", expect 1..18");
    }
}