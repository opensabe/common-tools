package io.github.opensabe.common.alive.client.message.enumeration;

public enum SubType {
    SUB(1),
    UNSUB(2);
    private int val;
    public int getVal() {
        return val;
    }
    SubType(int val) {
        this.val = val;
    }
}