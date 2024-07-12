package io.github.opensabe.common.utils.mapstruct.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Customer {
    private Long id;
    private String customerName;
    private List<OrderItemDto> orders;
    private Map<OrderItemKeyDto, OrderItemDto> stock;
}
