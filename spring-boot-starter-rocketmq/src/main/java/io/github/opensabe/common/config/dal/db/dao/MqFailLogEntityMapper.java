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

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntity;

public interface MqFailLogEntityMapper {

    @InsertProvider(type = MqFailLogEntitySqlProvider.class, method = "insertSelective")
    int insertSelective(MqFailLogEntity record);

    @Select({
            "select id as id, topic as topic, ",
            "hash_key as hashKey, trace_id as traceId, ",
            "send_config as sendConfig, retry_num as retryNum, ",
            "send_status as sendStatus, body as body ",
            "from t_common_mq_fail_log ",
            "where send_status = ",
            MqFailLogEntity.STATUS_PENDING + " ",
            "order by id ",
            "limit #{limit} "
    })
    List<MqFailLogEntity> selectPendingMessages(int limit);

    @Update({
            "update t_common_mq_fail_log ",
            "set send_status = #{sendStatus}, ",
            "retry_num = #{retryNum} ",
            "where id = #{id} "
    })
    int updateStatusAndRetryNum(
            @Param("id") String msgId,
            @Param("sendStatus") int sendStatus,
            @Param("retryNum") int retryNum
    );
}