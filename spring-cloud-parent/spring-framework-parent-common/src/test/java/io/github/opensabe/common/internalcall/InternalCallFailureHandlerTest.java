package io.github.opensabe.common.internalcall;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InternalCallFailureHandlerTest {

    @Test
    @DisplayName("去重：同一 fingerprint 第二次不抛")
    void dedup() {
        InternalCallProperties props = new InternalCallProperties();
        props.getFailure().setAlarmDedupSeconds(60);
        InternalCallFailureHandler handler = new InternalCallFailureHandler(props);
        assertDoesNotThrow(() -> handler.report(
                InternalCallFailureReason.MISSING_HEADER, "10.0.0.1", "/api", "gw"));
        assertDoesNotThrow(() -> handler.report(
                InternalCallFailureReason.MISSING_HEADER, "10.0.0.1", "/api", "gw"));
    }

    @Test
    @DisplayName("path 截断到 256")
    void truncate() {
        String longPath = "x".repeat(300);
        assertEquals(256, InternalCallFailureHandler.truncate(longPath, 256).length());
    }

    @Test
    @DisplayName("path 去掉 query，避免日志泄露")
    void stripQuery() {
        assertEquals("/api/order", InternalCallFailureHandler.stripQuery("/api/order?token=secret"));
        assertEquals("", InternalCallFailureHandler.stripQuery(null));
    }
}
