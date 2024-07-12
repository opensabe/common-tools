package io.github.opensabe.common.dynamodb.test.po;


import com.alibaba.fastjson.annotation.JSONField;
import io.github.opensabe.common.dynamodb.annotation.HashKeyName;
import io.github.opensabe.common.dynamodb.annotation.RangeKeyName;
import io.github.opensabe.common.dynamodb.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@TableName(name = "dynamodb_${aws_env}_eight_data_types")
@Data
public class EightDataTypesPo {

    @JSONField(name =  "id")
    @HashKeyName(name="id")
    private String id;
    @JSONField(name = "order")
    @RangeKeyName(name="order")
    private Integer order;
    @JSONField(name = "it")
    private int num1;
    private Double db1;
    private double db2;
    private Float ft1;
    private float ft2;
    private Byte by1;
    private byte by2;
    private Long lg1;
    private long lg2;
    private Boolean flag1;
    private boolean flag2;
    @JSONField(name = "create_time")
    private Date createTime;
    private BigDecimal bg;
    private BigInteger bi;

}
