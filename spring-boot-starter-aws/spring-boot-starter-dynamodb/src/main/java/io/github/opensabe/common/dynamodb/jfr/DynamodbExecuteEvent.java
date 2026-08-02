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
package io.github.opensabe.common.dynamodb.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

/**
 * DynamoDB 操作 JFR 事件。
 */
@Getter
@Setter
@Category({"observation", "Dynamodb-Execute"})
@Label("Dynamodb Execute Monitor")
@StackTrace(value = false)
public class DynamodbExecuteEvent extends Event {

    @Label("SQL Executed Method")
/** method。 */
    private final String method;

/** traceId。 */
    private String traceId;
/** spanId。 */
    private String spanId;
/** hashKey。 */
    private String hashKey;
/** rangeKey。 */
    private String rangeKey;
/** expression。 */
    private String expression;

    public DynamodbExecuteEvent(String method, String hashKey, String rangeKey, String expression) {
        this.method = method;
        this.hashKey = hashKey;
        this.rangeKey = rangeKey;
        this.expression = expression;
    }

}