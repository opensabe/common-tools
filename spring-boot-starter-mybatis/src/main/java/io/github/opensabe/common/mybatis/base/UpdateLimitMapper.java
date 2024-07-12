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
