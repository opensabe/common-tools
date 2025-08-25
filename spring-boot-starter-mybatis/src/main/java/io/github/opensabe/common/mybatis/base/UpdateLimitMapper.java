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
package io.github.opensabe.common.mybatis.base;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.mapping.MappedStatement;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.mapperhelper.MapperTemplate;
import tk.mybatis.mapper.mapperhelper.SqlHelper;

@RegisterMapper
public interface UpdateLimitMapper<T> {

    @UpdateProvider(type = UpdateLimitProvider.class, method = "dynamicSQL")
    int updateByExampleSelectiveLimit(@Param("record") T record, @Param("example") Object example, @Param("limit") int limit);

    @UpdateProvider(type = UpdateLimitProvider.class, method = "dynamicSQL")
    int updateByExampleLimit(@Param("record") T record, @Param("example") Object example, @Param("limit") int limit);

    class UpdateLimitProvider extends MapperTemplate {

        public UpdateLimitProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
            super(mapperClass, mapperHelper);
        }


        public String updateByExampleSelectiveLimit (MappedStatement ms) {
            Class<?> entityClass = getEntityClass(ms);
            StringBuilder sql = new StringBuilder();
            if (isCheckExampleEntityClass()) {
                sql.append(SqlHelper.exampleCheck(entityClass));
            }
            //安全更新，Example 必须包含条件
            if (getConfig().isSafeUpdate()) {
                sql.append(SqlHelper.exampleHasAtLeastOneCriteriaCheck("example"));
            }
            sql.append(SqlHelper.updateTable(entityClass, tableName(entityClass), "example"));
            sql.append(SqlHelper.updateSetColumnsIgnoreVersion(entityClass, "record", true, isNotEmpty()));
            sql.append(SqlHelper.updateByExampleWhereClause());
            sql.append(" limit #{limit}");
            return sql.toString();
        }

        public String updateByExampleLimit (MappedStatement ms) {
            Class<?> entityClass = getEntityClass(ms);
            StringBuilder sql = new StringBuilder();
            if (isCheckExampleEntityClass()) {
                sql.append(SqlHelper.exampleCheck(entityClass));
            }
            //安全更新，Example 必须包含条件
            if (getConfig().isSafeUpdate()) {
                sql.append(SqlHelper.exampleHasAtLeastOneCriteriaCheck("example"));
            }
            sql.append(SqlHelper.updateTable(entityClass, tableName(entityClass), "example"));
            sql.append(SqlHelper.updateSetColumnsIgnoreVersion(entityClass, "record", false, false));
            sql.append(SqlHelper.updateByExampleWhereClause());
            sql.append(" limit #{limit}");
            return sql.toString();
        }
    }

}
