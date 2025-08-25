/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.buffer;

//等 https://github.com/abel533/Mapper/releases/tag/5.0.0-rc2 发布，更新 tk mybatis 之后就可以去掉 javax 的依赖和注解
import jakarta.persistence.Transient;

public abstract class BufferedElement {
    protected BufferedElement() {
        bufferedElementJFREvent = new BufferedElementJFREvent();
        bufferedElementJFREvent.begin();
    }

    /**
     * 用于负载均衡的 key
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
