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
package io.github.opensabe.common.config.dal.db.dao;

import org.apache.ibatis.jdbc.SQL;

import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntity;

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