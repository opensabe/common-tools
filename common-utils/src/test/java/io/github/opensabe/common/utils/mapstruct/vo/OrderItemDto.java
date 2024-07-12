package io.github.opensabe.common.utils.mapstruct.vo;

import io.github.opensabe.mapstruct.core.Binding;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Sjaak Derksen
 */
@Getter
@Setter
@Binding
public class OrderItemDto {

    private String name;
    private Long quantity;
}
