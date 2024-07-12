package io.github.opensabe.common.config.dal.db.dao;

import java.util.Date;
import java.util.List;

import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntity;
import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntityExample;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;

public interface MqFailLogEntityMapper {
    @SelectProvider(type=MqFailLogEntitySqlProvider.class, method="countByExample")
    long countByExample(MqFailLogEntityExample example);

    @DeleteProvider(type=MqFailLogEntitySqlProvider.class, method="deleteByExample")
    int deleteByExample(MqFailLogEntityExample example);

    @Insert({
            "insert into t_common_mq_fail_log (id, topic, ",
            "hash_key, trace_id, ",
            "send_config, retry_num, ",
            "send_status, create_time, ",
            "update_time, body)",
            "values (#{id,jdbcType=VARCHAR}, #{topic,jdbcType=VARCHAR}, ",
            "#{hashKey,jdbcType=VARCHAR}, #{traceId,jdbcType=VARCHAR}, ",
            "#{sendConfig,jdbcType=VARCHAR}, #{retryNum,jdbcType=INTEGER}, ",
            "#{sendStatus,jdbcType=INTEGER}, #{createTime,jdbcType=TIMESTAMP}, ",
            "#{updateTime,jdbcType=TIMESTAMP}, #{body,jdbcType=LONGVARCHAR})"
    })
    int insert(MqFailLogEntity record);

    @InsertProvider(type=MqFailLogEntitySqlProvider.class, method="insertSelective")
    int insertSelective(MqFailLogEntity record);

    @SelectProvider(type=MqFailLogEntitySqlProvider.class, method="selectByExampleWithBLOBs")
    @ConstructorArgs({
            @Arg(column="id", javaType=String.class, jdbcType=JdbcType.VARCHAR),
            @Arg(column="topic", javaType=String.class, jdbcType=JdbcType.VARCHAR),
            @Arg(column="hash_key", javaType=String.class, jdbcType=JdbcType.VARCHAR),
            @Arg(column="trace_id", javaType=String.class, jdbcType=JdbcType.VARCHAR),
            @Arg(column="send_config", javaType=String.class, jdbcType=JdbcType.VARCHAR),
            @Arg(column="retry_num", javaType=Integer.class, jdbcType=JdbcType.INTEGER),
            @Arg(column="send_status", javaType=Integer.class, jdbcType=JdbcType.INTEGER),
            @Arg(column="create_time", javaType=Date.class, jdbcType=JdbcType.TIMESTAMP),
            @Arg(column="update_time", javaType=Date.class, jdbcType=JdbcType.TIMESTAMP),
            @Arg(column="body", javaType=String.class, jdbcType=JdbcType.LONGVARCHAR)
    })
    List<MqFailLogEntity> selectByExampleWithBLOBs(MqFailLogEntityExample example);

    @SelectProvider(type=MqFailLogEntitySqlProvider.class, method="selectByExample")
    @ConstructorArgs({
            @Arg(column="id", javaType=String.class, jdbcType=JdbcType.VARCHAR),
            @Arg(column="topic", javaType=String.class, jdbcType=JdbcType.VARCHAR),
            @Arg(column="hash_key", javaType=String.class, jdbcType=JdbcType.VARCHAR),
            @Arg(column="trace_id", javaType=String.class, jdbcType=JdbcType.VARCHAR),
            @Arg(column="send_config", javaType=String.class, jdbcType=JdbcType.VARCHAR),
            @Arg(column="retry_num", javaType=Integer.class, jdbcType=JdbcType.INTEGER),
            @Arg(column="send_status", javaType=Integer.class, jdbcType=JdbcType.INTEGER),
            @Arg(column="create_time", javaType=Date.class, jdbcType=JdbcType.TIMESTAMP),
            @Arg(column="update_time", javaType=Date.class, jdbcType=JdbcType.TIMESTAMP)
    })
    List<MqFailLogEntity> selectByExample(MqFailLogEntityExample example);

    @UpdateProvider(type=MqFailLogEntitySqlProvider.class, method="updateByExampleSelective")
    int updateByExampleSelective(@Param("record") MqFailLogEntity record, @Param("example") MqFailLogEntityExample example);

    @UpdateProvider(type=MqFailLogEntitySqlProvider.class, method="updateByExampleWithBLOBs")
    int updateByExampleWithBLOBs(@Param("record") MqFailLogEntity record, @Param("example") MqFailLogEntityExample example);

    @UpdateProvider(type=MqFailLogEntitySqlProvider.class, method="updateByExample")
    int updateByExample(@Param("record") MqFailLogEntity record, @Param("example") MqFailLogEntityExample example);
}