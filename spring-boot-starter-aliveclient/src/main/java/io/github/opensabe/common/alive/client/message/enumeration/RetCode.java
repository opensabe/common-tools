package io.github.opensabe.common.alive.client.message.enumeration;

public enum RetCode {
    SUCCESS(1),
    FAIL(2),
    CACHED(3),
    PARAM_ERR(4),
    NO_AUTH(5);

    private int val;

    public int getVal() {
        return val;
    }

    RetCode(int val) {
        this.val = val;
    }
    }