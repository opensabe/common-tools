package io.github.opensabe.common.config.dal.db.dao;

import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntity;
import org.apache.ibatis.annotations.InsertProvider;

public interface MqFailLogEntityMapper {

    @InsertProvider(type=MqFailLogEntitySqlProvider.class, method="insertSelective")
    int insertSelective(MqFailLogEntity record);
}