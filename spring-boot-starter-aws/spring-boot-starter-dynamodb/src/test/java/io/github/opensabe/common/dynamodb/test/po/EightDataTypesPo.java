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
package io.github.opensabe.common.dynamodb.test.po;


import com.alibaba.fastjson.annotation.JSONField;
import io.github.opensabe.common.dynamodb.annotation.HashKeyName;
import io.github.opensabe.common.dynamodb.annotation.RangeKeyName;
import io.github.opensabe.common.dynamodb.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
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
    @JSONField(name = "update_time")
    private LocalDateTime updateTime;
    private BigDecimal bg;
    private BigInteger bi;

}
