package io.github.opensabe.common.config.dal.db.dao;

import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntity;
import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntityExample;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;

public class MqFailLogEntitySqlProvider {
    public String countByExample(MqFailLogEntityExample example) {
        SQL sql = new SQL();
        sql.SELECT("count(*)").FROM("t_common_mq_fail_log");
        applyWhere(sql, example, false);
        return sql.toString();
    }

    public String deleteByExample(MqFailLogEntityExample example) {
        SQL sql = new SQL();
        sql.DELETE_FROM("t_common_mq_fail_log");
        applyWhere(sql, example, false);
        return sql.toString();
    }

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
        
        if (record.getCreateTime() != null) {
            sql.VALUES("create_time", "#{createTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getUpdateTime() != null) {
            sql.VALUES("update_time", "#{updateTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getBody() != null) {
            sql.VALUES("body", "#{body,jdbcType=LONGVARCHAR}");
        }
        
        return sql.toString();
    }

    public String selectByExampleWithBLOBs(MqFailLogEntityExample example) {
        SQL sql = new SQL();
        if (example != null && example.isDistinct()) {
            sql.SELECT_DISTINCT("id");
        } else {
            sql.SELECT("id");
        }
        sql.SELECT("topic");
        sql.SELECT("hash_key");
        sql.SELECT("trace_id");
        sql.SELECT("send_config");
        sql.SELECT("retry_num");
        sql.SELECT("send_status");
        sql.SELECT("create_time");
        sql.SELECT("update_time");
        sql.SELECT("body");
        sql.FROM("t_common_mq_fail_log");
        applyWhere(sql, example, false);
        
        if (example != null && example.getOrderByClause() != null) {
            sql.ORDER_BY(example.getOrderByClause());
        }
        
        return sql.toString();
    }

    public String selectByExample(MqFailLogEntityExample example) {
        SQL sql = new SQL();
        if (example != null && example.isDistinct()) {
            sql.SELECT_DISTINCT("id");
        } else {
            sql.SELECT("id");
        }
        sql.SELECT("topic");
        sql.SELECT("hash_key");
        sql.SELECT("trace_id");
        sql.SELECT("send_config");
        sql.SELECT("retry_num");
        sql.SELECT("send_status");
        sql.SELECT("create_time");
        sql.SELECT("update_time");
        sql.FROM("t_common_mq_fail_log");
        applyWhere(sql, example, false);
        
        if (example != null && example.getOrderByClause() != null) {
            sql.ORDER_BY(example.getOrderByClause());
        }
        
        return sql.toString();
    }

    public String updateByExampleSelective(Map<String, Object> parameter) {
        MqFailLogEntity record = (MqFailLogEntity) parameter.get("record");
        MqFailLogEntityExample example = (MqFailLogEntityExample) parameter.get("example");
        
        SQL sql = new SQL();
        sql.UPDATE("t_common_mq_fail_log");
        
        if (record.getId() != null) {
            sql.SET("id = #{record.id,jdbcType=VARCHAR}");
        }
        
        if (record.getTopic() != null) {
            sql.SET("topic = #{record.topic,jdbcType=VARCHAR}");
        }
        
        if (record.getHashKey() != null) {
            sql.SET("hash_key = #{record.hashKey,jdbcType=VARCHAR}");
        }
        
        if (record.getTraceId() != null) {
            sql.SET("trace_id = #{record.traceId,jdbcType=VARCHAR}");
        }
        
        if (record.getSendConfig() != null) {
            sql.SET("send_config = #{record.sendConfig,jdbcType=VARCHAR}");
        }
        
        if (record.getRetryNum() != null) {
            sql.SET("retry_num = #{record.retryNum,jdbcType=INTEGER}");
        }
        
        if (record.getSendStatus() != null) {
            sql.SET("send_status = #{record.sendStatus,jdbcType=INTEGER}");
        }
        
        if (record.getCreateTime() != null) {
            sql.SET("create_time = #{record.createTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getUpdateTime() != null) {
            sql.SET("update_time = #{record.updateTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getBody() != null) {
            sql.SET("body = #{record.body,jdbcType=LONGVARCHAR}");
        }
        
        applyWhere(sql, example, true);
        return sql.toString();
    }

    public String updateByExampleWithBLOBs(Map<String, Object> parameter) {
        SQL sql = new SQL();
        sql.UPDATE("t_common_mq_fail_log");
        
        sql.SET("id = #{record.id,jdbcType=VARCHAR}");
        sql.SET("topic = #{record.topic,jdbcType=VARCHAR}");
        sql.SET("hash_key = #{record.hashKey,jdbcType=VARCHAR}");
        sql.SET("trace_id = #{record.traceId,jdbcType=VARCHAR}");
        sql.SET("send_config = #{record.sendConfig,jdbcType=VARCHAR}");
        sql.SET("retry_num = #{record.retryNum,jdbcType=INTEGER}");
        sql.SET("send_status = #{record.sendStatus,jdbcType=INTEGER}");
        sql.SET("create_time = #{record.createTime,jdbcType=TIMESTAMP}");
        sql.SET("update_time = #{record.updateTime,jdbcType=TIMESTAMP}");
        sql.SET("body = #{record.body,jdbcType=LONGVARCHAR}");
        
        MqFailLogEntityExample example = (MqFailLogEntityExample) parameter.get("example");
        applyWhere(sql, example, true);
        return sql.toString();
    }

    public String updateByExample(Map<String, Object> parameter) {
        SQL sql = new SQL();
        sql.UPDATE("t_common_mq_fail_log");
        
        sql.SET("id = #{record.id,jdbcType=VARCHAR}");
        sql.SET("topic = #{record.topic,jdbcType=VARCHAR}");
        sql.SET("hash_key = #{record.hashKey,jdbcType=VARCHAR}");
        sql.SET("trace_id = #{record.traceId,jdbcType=VARCHAR}");
        sql.SET("send_config = #{record.sendConfig,jdbcType=VARCHAR}");
        sql.SET("retry_num = #{record.retryNum,jdbcType=INTEGER}");
        sql.SET("send_status = #{record.sendStatus,jdbcType=INTEGER}");
        sql.SET("create_time = #{record.createTime,jdbcType=TIMESTAMP}");
        sql.SET("update_time = #{record.updateTime,jdbcType=TIMESTAMP}");
        
        MqFailLogEntityExample example = (MqFailLogEntityExample) parameter.get("example");
        applyWhere(sql, example, true);
        return sql.toString();
    }

    protected void applyWhere(SQL sql, MqFailLogEntityExample example, boolean includeExamplePhrase) {
        if (example == null) {
            return;
        }
        
        String parmPhrase1;
        String parmPhrase1_th;
        String parmPhrase2;
        String parmPhrase2_th;
        String parmPhrase3;
        String parmPhrase3_th;
        if (includeExamplePhrase) {
            parmPhrase1 = "%s #{example.oredCriteria[%d].allCriteria[%d].value}";
            parmPhrase1_th = "%s #{example.oredCriteria[%d].allCriteria[%d].value,typeHandler=%s}";
            parmPhrase2 = "%s #{example.oredCriteria[%d].allCriteria[%d].value} and #{example.oredCriteria[%d].criteria[%d].secondValue}";
            parmPhrase2_th = "%s #{example.oredCriteria[%d].allCriteria[%d].value,typeHandler=%s} and #{example.oredCriteria[%d].criteria[%d].secondValue,typeHandler=%s}";
            parmPhrase3 = "#{example.oredCriteria[%d].allCriteria[%d].value[%d]}";
            parmPhrase3_th = "#{example.oredCriteria[%d].allCriteria[%d].value[%d],typeHandler=%s}";
        } else {
            parmPhrase1 = "%s #{oredCriteria[%d].allCriteria[%d].value}";
            parmPhrase1_th = "%s #{oredCriteria[%d].allCriteria[%d].value,typeHandler=%s}";
            parmPhrase2 = "%s #{oredCriteria[%d].allCriteria[%d].value} and #{oredCriteria[%d].criteria[%d].secondValue}";
            parmPhrase2_th = "%s #{oredCriteria[%d].allCriteria[%d].value,typeHandler=%s} and #{oredCriteria[%d].criteria[%d].secondValue,typeHandler=%s}";
            parmPhrase3 = "#{oredCriteria[%d].allCriteria[%d].value[%d]}";
            parmPhrase3_th = "#{oredCriteria[%d].allCriteria[%d].value[%d],typeHandler=%s}";
        }
        
        StringBuilder sb = new StringBuilder();
        List<MqFailLogEntityExample.Criteria> oredCriteria = example.getOredCriteria();
        boolean firstCriteria = true;
        for (int i = 0; i < oredCriteria.size(); i++) {
            MqFailLogEntityExample.Criteria criteria = oredCriteria.get(i);
            if (criteria.isValid()) {
                if (firstCriteria) {
                    firstCriteria = false;
                } else {
                    sb.append(" or ");
                }
                
                sb.append('(');
                List<MqFailLogEntityExample.Criterion> criterions = criteria.getAllCriteria();
                boolean firstCriterion = true;
                for (int j = 0; j < criterions.size(); j++) {
                    MqFailLogEntityExample.Criterion criterion = criterions.get(j);
                    if (firstCriterion) {
                        firstCriterion = false;
                    } else {
                        sb.append(" and ");
                    }
                    
                    if (criterion.isNoValue()) {
                        sb.append(criterion.getCondition());
                    } else if (criterion.isSingleValue()) {
                        if (criterion.getTypeHandler() == null) {
                            sb.append(String.format(parmPhrase1, criterion.getCondition(), i, j));
                        } else {
                            sb.append(String.format(parmPhrase1_th, criterion.getCondition(), i, j,criterion.getTypeHandler()));
                        }
                    } else if (criterion.isBetweenValue()) {
                        if (criterion.getTypeHandler() == null) {
                            sb.append(String.format(parmPhrase2, criterion.getCondition(), i, j, i, j));
                        } else {
                            sb.append(String.format(parmPhrase2_th, criterion.getCondition(), i, j, criterion.getTypeHandler(), i, j, criterion.getTypeHandler()));
                        }
                    } else if (criterion.isListValue()) {
                        sb.append(criterion.getCondition());
                        sb.append(" (");
                        List<?> listItems = (List<?>) criterion.getValue();
                        boolean comma = false;
                        for (int k = 0; k < listItems.size(); k++) {
                            if (comma) {
                                sb.append(", ");
                            } else {
                                comma = true;
                            }
                            if (criterion.getTypeHandler() == null) {
                                sb.append(String.format(parmPhrase3, i, j, k));
                            } else {
                                sb.append(String.format(parmPhrase3_th, i, j, k, criterion.getTypeHandler()));
                            }
                        }
                        sb.append(')');
                    }
                }
                sb.append(')');
            }
        }
        
        if (sb.length() > 0) {
            sql.WHERE(sb.toString());
        }
    }
}