package io.github.opensabe.common.utils.mapstruct.vo;

import io.github.opensabe.mapstruct.core.Binding;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

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
