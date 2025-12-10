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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
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
public class PayPalPlanDetailResponseDTO {

    /**
     * plan id
     */
    private String id;

    /**
     * 产品id
     */
    @JsonAlias({"product_id"})
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
     * 用于试用计费和定期计费的一系列计费周期。一个计划最多可以有两个试验周期，只有一个常规周期。
     * <p>
     * 也可以使用：org.openapitools.client.model.BillingCycle
     * 这里就获取续订类型，就自定义内部内即可
     */
    @JsonAlias({"billing_cycles"})
    private List<BillingCycle> billingCycles;

    @Data
    public static class BillingCycle {

        /**
         * 计费周期的保有期类型。如果计划有试验周期，则每个计划只允许2个试验周期。
         *
         * @see TenureTypeEnum
         */
        @JsonAlias({"tenure_type"})
        private String tenureType;

        /**
         * 此周期在其他计费周期中运行的顺序。例如，试用计费周期具有1的序列，而常规计费周期具有2的序列，因此试用周期在常规周期之前运行。
         */
        private Integer sequence;

        /**
         * 执行此计费周期的次数。试用计费周期只能执行有限的次数（对于total_cycles，值在1到999之间）。
         * 常规计费周期可以执行无限次（对于total_cycles，值为0）或有限次数（对于total-cycles，值在1和999之间）。
         */
        @JsonAlias({"total_cycles"})
        private Integer totalCycles;

    }
}
