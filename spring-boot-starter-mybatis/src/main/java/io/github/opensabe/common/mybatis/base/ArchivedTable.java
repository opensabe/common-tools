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
package io.github.opensabe.common.mybatis.base;

import com.alibaba.ttl.TransmittableThreadLocal;

import tk.mybatis.mapper.entity.IDynamicTableName;

public abstract class ArchivedTable implements IDynamicTableName {
    private static TransmittableThreadLocal<Boolean> currentIsHistory = new TransmittableThreadLocal<>();

    public static void setIsHistory(boolean isHistory) {
        currentIsHistory.set(isHistory);
    }

    protected abstract String tableName();

    protected abstract String historyTableName();

    @Override
    public String getDynamicTableName() {
        Boolean aBoolean = currentIsHistory.get();
        currentIsHistory.remove();
        if (aBoolean != null && aBoolean) {
            return historyTableName();
        } else {
            return tableName();
        }
    }
}
