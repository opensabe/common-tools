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

/**
 * 带业务类型编码的全局唯一 ID 生成接口。
 */
public interface UniqueID {
    /**
     * 生成标准唯一 ID（时间戳 + 业务类型 + 序列号）。
     *
     * @param bizType 业务类型编号（最长 4 字符）
     * @return 全局唯一 ID 字符串
     */
    String getUniqueId(String bizType);

    /**
     * 生成长格式唯一 ID（毫秒时间戳 + 业务类型 + 序列号）。
     *
     * @param bizType 业务类型编号（最长 4 字符）
     * @return 全局唯一 ID 字符串
     */
    String getLongUniqueId(String bizType);

    /**
     * 带超时的唯一 ID 生成。
     *
     * @param bizType  业务类型编号
     * @param time     超时数值
     * @param timeUnit 超时单位
     * @return 全局唯一 ID 字符串
     * @throws Exception 超时或执行异常
     */
    String getUniqueIdWithTimeOut(String bizType, long time, TimeUnit timeUnit) throws Exception;

    /**
     * 生成短唯一 ID，适用于每毫秒不超过 1000 个请求的场景。
     *
     * @param bizType 业务类型编号（最长 10 字符）
     * @return 短唯一 ID 字符串
     */
    String getShortUniqueId(String bizType);
}
