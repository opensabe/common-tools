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
package io.github.opensabe.common.mybatis.observation;

import java.util.Optional;

import javax.annotation.Nullable;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

/**
 * 统计事务或者SQL执行时间的content
 *
 * @author maheng
 */
@Getter
@Setter
public class SQLExecuteContext extends Observation.Context {
    private final String method;
    private final String transactionName;
    private long end;
    private boolean success;

    public SQLExecuteContext(String method, @Nullable String transactionName) {
        this.method = method;
        this.transactionName = Optional.ofNullable(transactionName).orElse("");
        this.success = true;
    }
}
