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
package io.github.opensabe.common.mybatis.types;

import io.github.opensabe.common.mybatis.configuration.TypeHandlerSpringHolderConfiguration;
import io.github.opensabe.common.typehandler.OBSService;
import io.github.opensabe.common.typehandler.OBSTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public abstract class OBSTypeHandler extends JSONTypeHandler {

    public OBSTypeHandler(Class<?> type) {
        super(type);
    }



    /**
     * 先将json保存到其他位置，将json对应的Key保存到数据库,
     * 这里只要能通过key判断出是s3还是dynamodb就行
     * 因为每次都是插入，不需要考虑保存json成功，修改mysql失败，但会造成垃圾数据
     * @param ps
     * @param i
     * @param parameter json对象
     * @param jdbcType
     * @throws SQLException
     * @throws NullPointerException 当没有依赖common-id-generator或者没有依赖相应的s3或者dynamodb
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException,NullPointerException {
        if (Objects.nonNull(parameter)) {
            var key = genId();
            getObsService().insert(key, toString(parameter));
            super.setNonNullParameter(ps, i, key, jdbcType);
        }else {
            super.setNonNullParameter(ps, i, parameter, jdbcType);
        }
    }

    public OBSService getObsService() {
        return TypeHandlerSpringHolderConfiguration.getService(type());
    }

    public String genId() {
        return TypeHandlerSpringHolderConfiguration.getUniqueID().getUniqueId(type().getIdShortName());

    }
    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        var key = rs.getString(columnName);
        return transform(key);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        var key = rs.getString(columnIndex);
        return transform(key);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        var key = cs.getString(columnIndex);
        return transform(key);
    }

    protected abstract OBSTypeEnum type ();


    private Object transform (String key) {
        if (StringUtils.isNotBlank(key)) {
            var json = getObsService().select(key);
            return toJavaBean(json);
        }
        return null;
    }

}
