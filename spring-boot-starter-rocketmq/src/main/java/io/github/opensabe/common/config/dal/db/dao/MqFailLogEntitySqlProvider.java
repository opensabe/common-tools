package io.github.opensabe.common.config.dal.db.dao;

import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntity;
import org.apache.ibatis.jdbc.SQL;

public class MqFailLogEntitySqlProvider {

    public String insertSelective(MqFailLogEntity record) {
        SQL sql = new SQL();
        sql.INSERT_INTO("t_common_mq_fail_log");
        
        if (record.getId() != null) {
            sql.VALUES("id", "#{id,jdbcType=VARCHAR}");
        }
        
        if (record.getTopic() != null) {
            sql.VALUES("topic", "#{topic,jdbcType=VARCHAR}");
        }
        
        if (record.getHashKey() != null) {
            sql.VALUES("hash_key", "#{hashKey,jdbcType=VARCHAR}");
        }
        
        if (record.getTraceId() != null) {
            sql.VALUES("trace_id", "#{traceId,jdbcType=VARCHAR}");
        }
        
        if (record.getSendConfig() != null) {
            sql.VALUES("send_config", "#{sendConfig,jdbcType=VARCHAR}");
        }
        
        if (record.getRetryNum() != null) {
            sql.VALUES("retry_num", "#{retryNum,jdbcType=INTEGER}");
        }
        
        if (record.getSendStatus() != null) {
            sql.VALUES("send_status", "#{sendStatus,jdbcType=INTEGER}");
        }
//
//        if (record.getCreateTime() != null) {
//            sql.VALUES("create_time", "#{createTime,jdbcType=TIMESTAMP}");
//        }
//
//        if (record.getUpdateTime() != null) {
//            sql.VALUES("update_time", "#{updateTime,jdbcType=TIMESTAMP}");
//        }
        
        if (record.getBody() != null) {
            sql.VALUES("body", "#{body,jdbcType=LONGVARCHAR}");
        }
        
        return sql.toString();
    }

}