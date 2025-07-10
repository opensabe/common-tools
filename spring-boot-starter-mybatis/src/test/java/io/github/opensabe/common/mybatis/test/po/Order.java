package io.github.opensabe.common.mybatis.test.po;

import io.github.opensabe.common.mybatis.types.S3TypeHandler;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.JdbcType;
import tk.mybatis.mapper.annotation.ColumnType;

import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Table(name = "t_order")
public class Order {

    @Id
    private String id;

    @ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = S3TypeHandler.class)
    private OrderInfo orderInfo;

    @Getter
    @Setter
    public static class OrderInfo {
        private String stockId;

        private Integer stake;
    }
}
