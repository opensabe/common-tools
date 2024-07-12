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
