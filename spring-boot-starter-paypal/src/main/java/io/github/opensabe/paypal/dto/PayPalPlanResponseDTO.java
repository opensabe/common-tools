package io.github.opensabe.paypal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PayPal Plan DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayPalPlanResponseDTO {

    /**
     * plan集合
     */
    private List<PayPalPlanDTO> plans;

    /**
     * links：无用
     */
    private Object links;
}
