package io.github.opensabe.common.mybatis.test.mapper.user;

import io.github.opensabe.common.mybatis.base.BaseMapper;
import io.github.opensabe.common.mybatis.test.po.Order;
import io.github.opensabe.common.mybatis.types.S3TypeHandler;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface OrderMapper extends BaseMapper<Order> {

    @Results(
            {
                    @Result(column = "order_info", property = "orderInfo", typeHandler = S3TypeHandler.class)
            }
    )
    @Select("select * from t_order where id = #{id}")
    Order selectByMapper (@Param("id") String id);
}
