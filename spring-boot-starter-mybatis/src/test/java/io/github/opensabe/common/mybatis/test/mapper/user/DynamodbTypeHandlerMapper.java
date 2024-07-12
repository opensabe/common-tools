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
