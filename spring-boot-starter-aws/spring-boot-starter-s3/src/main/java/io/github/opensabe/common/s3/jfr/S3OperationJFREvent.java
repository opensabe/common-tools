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
package io.github.opensabe.common.s3.jfr;

import io.github.opensabe.common.s3.observation.S3OperationContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Category({"observation", "s3"})
@Label("operation")
@StackTrace(false)
public class S3OperationJFREvent extends Event {
    @Label("file name")
    private String fileName;
    @Label("file size")
    private long fileSize;
    @Label("operate type")
    private String operateType;
    private boolean success;
    private String traceId;
    private String spanId;

    public S3OperationJFREvent(S3OperationContext s3OperationContext) {
        this.fileName = s3OperationContext.getFileName();
        this.operateType=s3OperationContext.getOperateType();
    }
}
