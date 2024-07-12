package io.github.opensabe.common.utils.mapstruct.vo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

public record TypesDto (String name,int age,Integer age0,
                        LocalDateTime time1,Instant time2,Date time3,
                        boolean flag,Boolean flag0,double money,
                        Double money0,long count,Long count0,
                        float weight,Float weight0,short height,Short height0,BigDecimal price) {

}
