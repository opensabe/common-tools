package io.github.opensabe.common.buffer;


import jakarta.persistence.Transient;

public abstract class BufferedElement {
    protected BufferedElement() {
        bufferedElementJFREvent = new BufferedElementJFREvent();
        bufferedElementJFREvent.begin();
    }

    /**
     * 用于负载均衡的 key
     * @return
     */
    public abstract String hashKey();

    @Transient
    private String traceId;
    @Transient
    private String spanId;

    private final BufferedElementJFREvent bufferedElementJFREvent;

    public String traceId() {
        return traceId;
    }
    public String spanId() {
        return spanId;
    }
    public void setSubmitInfo(String traceId, String spanId) {
        this.traceId = traceId;
        this.bufferedElementJFREvent.setSubmitTraceId(traceId);
        this.spanId = spanId;
        this.bufferedElementJFREvent.setSubmitSpanId(spanId);
    }

    public void beforeElementManipulate(String spanId) {
        this.bufferedElementJFREvent.setQueueTime(
                //nanoseconds 的速度在一些操作系统比 currentTimeMillis 快
                System.currentTimeMillis() - this.bufferedElementJFREvent.getSubmitTime()
        );
        this.bufferedElementJFREvent.setBatchSpanId(spanId);
    }

    public void afterElementManipulate() {
        this.bufferedElementJFREvent.commit();
    }

    public void afterElementManipulateError(Throwable throwable) {
        this.bufferedElementJFREvent.setError(throwable.getMessage());
        this.bufferedElementJFREvent.commit();
    }
}
