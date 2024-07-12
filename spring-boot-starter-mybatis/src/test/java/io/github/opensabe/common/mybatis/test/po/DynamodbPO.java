package io.github.opensabe.common.mybatis.test.po;

import io.github.opensabe.common.mybatis.types.DynamoDbTypeHandler;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.JdbcType;
import tk.mybatis.mapper.annotation.ColumnType;

import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Table(name = "t_dynamodb_type_handler")
public class DynamodbPO {

    @Id
    private String id;

    @ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = DynamoDbTypeHandler.class)
    private OrderInfo orderInfo;

    @Getter
    @Setter
    public static class OrderInfo {
        private String matchId;

        private Integer market;
    }
}
