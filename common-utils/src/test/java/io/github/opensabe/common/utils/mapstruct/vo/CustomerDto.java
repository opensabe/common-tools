/*
 * Copyright MapStruct Authors.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.opensabe.common.utils.mapstruct.vo;

import io.github.opensabe.mapstruct.core.Binding;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author Sjaak Derksen
 */
@Getter
@Setter
@Binding(value = Customer.class)
public class CustomerDto {

    private Long id;
    private String customerName;
    private List<OrderItemDto> orders;
    private Map<OrderItemKeyDto, OrderItemDto> stock;

}
