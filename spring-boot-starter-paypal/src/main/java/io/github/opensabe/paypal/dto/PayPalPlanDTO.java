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
package io.github.opensabe.paypal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayPal Plan DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayPalPlanDTO {

    /**
     * plan id
     */
    private String id;

    /**
     * 产品id
     */
    private String productId;

    /**
     * plan name
     */
    private String name;

    /**
     * plan 状态，有效的：ACTIVE  无效的：INACTIVE
     */
    private String status;

    /**
     * plan描述
     */
    private String description;

    /**
     * 得到许可的 LICENSED
     */
    private String usageType;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     *
     */
    private Object links;
}
