package io.github.opensabe.common.utils.mapstruct;

import io.github.opensabe.common.utils.mapstruct.vo.OrderItemDto;
import io.github.opensabe.mapstruct.core.MapperRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MapStruct自映射测试")
public class SelfTest {

    @Test
    @DisplayName("测试对象自映射功能")
    void test1 () {
        var i = new OrderItemDto();
        i.setQuantity(10L);
        i.setName("order");

        OrderItemDto order = MapperRepository.getInstance().getMapper(OrderItemDto.class).map(i);

        assertThat(order.getName()).isEqualTo("order");
        assertThat(order.getQuantity()).isEqualTo(10L);
    }

}
