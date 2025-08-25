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
package io.github.opensabe.common.mybatis.test.mapper.user;

import io.github.opensabe.common.mybatis.base.BaseMapper;
import io.github.opensabe.common.mybatis.test.po.DynamodbPO;
import io.github.opensabe.common.mybatis.types.DynamoDbTypeHandler;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface DynamodbTypeHandlerMapper extends BaseMapper<DynamodbPO> {

    @Results({
            @Result(column = "order_info", property = "orderInfo", typeHandler = DynamoDbTypeHandler.class)
    })
    @Select("select * from t_dynamodb_type_handler where id = #{id}")
    DynamodbPO selectByMapper (@Param("id") String id);
}
