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
package io.github.opensabe.common.utils.mapstruct;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.opensabe.common.utils.mapstruct.vo.OrderItemDto;
import io.github.opensabe.mapstruct.core.MapperRepository;

@DisplayName("MapStruct自映射测试")
public class SelfTest {

    @Test
    @DisplayName("测试对象自映射功能")
    void test1() {
        var i = new OrderItemDto();
        i.setQuantity(10L);
        i.setName("order");

        OrderItemDto order = MapperRepository.getInstance().getMapper(OrderItemDto.class).map(i);

        assertThat(order.getName()).isEqualTo("order");
        assertThat(order.getQuantity()).isEqualTo(10L);
    }

}
