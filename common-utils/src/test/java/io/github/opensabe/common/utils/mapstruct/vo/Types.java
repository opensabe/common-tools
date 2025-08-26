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
package io.github.opensabe.common.utils.mapstruct.vo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

import io.github.opensabe.mapstruct.core.Binding;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Binding(TypesDto.class)
public class Types {
    private String name;
    private int age;
    private Integer age0;
    private LocalDateTime time1;
    private Instant time2;
    private Date time3;
    private boolean flag;
    private Boolean flag0;
    private double money;
    private Double money0;
    private long count;
    private Long count0;
    private float weight;
    private Float weight0;
    private short height;
    private Short height0;
    private BigDecimal price;
}
