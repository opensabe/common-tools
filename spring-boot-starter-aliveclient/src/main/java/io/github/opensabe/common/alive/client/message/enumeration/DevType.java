package io.github.opensabe.common.alive.client.message.enumeration;

public enum DevType {
    IOS(1),
    ANDROID(2),
    WP(3),
    WEB(4);
    private int val;

    public int getVal() {
        return val;
    }

    DevType(int val) {
        this.val = val;
    }
}