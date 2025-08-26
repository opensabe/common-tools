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
package io.github.opensabe.common.mybatis.plugins;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.util.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class CryptTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("before encrypt parameter {}", parameter);
        }
        var encryptStr = encrypt(parameter);
        if (log.isDebugEnabled()) {
            log.debug("after encrpyt parameter {}", encryptStr);
        }
        ps.setString(i, encryptStr);

    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        var s = rs.getString(columnName);
        if (!StringUtils.hasText(s)) return null;
        if (log.isDebugEnabled()) {
            log.debug("before decrypt parameter {}", s);
        }
        var r = decrypt(s);
        if (log.isDebugEnabled()) {
            log.debug("after decrypt parameter {}", r);
        }
        return r;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        var s = rs.getString(columnIndex);
        if (!StringUtils.hasText(s)) return null;
        if (log.isDebugEnabled()) {
            log.debug("before decrypt parameter {}", s);
        }
        var r = decrypt(s);
        if (log.isDebugEnabled()) {
            log.debug("after decrypt parameter {}", r);
        }
        return r;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        var s = cs.getString(columnIndex);
        if (!StringUtils.hasText(s)) return null;
        if (log.isDebugEnabled()) {
            log.debug("before decrypt parameter {}", s);
        }
        var r = decrypt(s);
        if (log.isDebugEnabled()) {
            log.debug("after decrypt parameter {}", r);
        }
        return r;
    }

    protected abstract String encrypt(String origin);

    protected abstract String decrypt(String origin);

}
