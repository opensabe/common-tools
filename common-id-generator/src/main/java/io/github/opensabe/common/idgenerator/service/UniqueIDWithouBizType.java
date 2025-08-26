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
package io.github.opensabe.common.idgenerator.service;

import java.util.concurrent.TimeUnit;

public interface UniqueIDWithouBizType {
    /**
     * @param bizType 业务类型编号
     * @return id     全局唯一id
     */
    Long getUniqueId(String bizType);

    Long getUniqueIdWithTimeOut(String bizType, long time, TimeUnit timeUnit) throws Exception;

    /**
     * 适用于每毫秒不超过 1000 个业务
     *
     * @param bizType
     * @return
     */
    Long getShortUniqueId(String bizType);
}
