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
package io.github.opensabe.common.alive.client.message;

import io.github.opensabe.common.alive.client.message.enumeration.RetCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Response extends MqMessage {
    private Integer requestId;
    private RetCode retCode;
    private String rightHost;
    private long messageId;
    private Integer sendNum;
    private String extra;

    public Response() {
    }

    public Response(Integer requestId, RetCode retCode, String rightHost, long messageId, Integer sendNum, String extra) {
        this.requestId = requestId;
        this.retCode = retCode;
        this.rightHost = rightHost;
        this.messageId = messageId;
        this.sendNum = sendNum;
        this.extra = extra;
    }
}
