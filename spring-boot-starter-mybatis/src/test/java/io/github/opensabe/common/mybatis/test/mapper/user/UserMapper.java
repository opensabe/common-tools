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
package io.github.opensabe.common.mybatis.test.mapper.user;


import java.util.List;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import io.github.opensabe.common.mybatis.base.BaseMapper;
import io.github.opensabe.common.mybatis.test.po.User;
import io.github.opensabe.common.mybatis.types.JSONTypeHandler;

/**
 * 用户 MyBatis Mapper 测试接口。
 */
public interface UserMapper extends BaseMapper<User> {

    /** 清空 {@code t_user} 表（测试用）。 */
    @Update("truncate table t_user")
    void truncateTable();

    /**
     * 按 ID 查询用户（{@code properties} 经 {@link JSONTypeHandler}）。
     *
     * @param id 用户 ID
     * @return 用户 PO
     */
    @Select("select id, first_name, last_name, create_time, properties from t_user where id = #{id,jdbcType=VARCHAR}")
    @Results(id = "findUserById", value = {
            @Result(column = "id", property = "id", jdbcType = JdbcType.VARCHAR),
            @Result(column = "first_name", property = "firstName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "last_name", property = "lastName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.DATE),
            @Result(column = "properties", property = "properties", jdbcType = JdbcType.VARCHAR, typeHandler = JSONTypeHandler.class),
    })
    User findUserById(String id);

    /** 只读模式查询全部用户（SQL hint {@code mode=readonly}）。 */
    @Select("select /*# mode=readonly */ * from t_user")
    List<User> selectReadOnly();
}
