package io.github.opensabe.common.alive.client.message.enumeration;

public enum PushType {
    GROUP(1),
    SPECIAL(2),
    MULTI(3);
    private int val;

    public int getVal() {
        return val;
    }

    PushType(int val) {
        this.val = val;
    }
}