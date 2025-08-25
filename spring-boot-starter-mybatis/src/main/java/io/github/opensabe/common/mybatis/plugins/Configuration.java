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

import org.apache.ibatis.session.SqlSession;

public class Configuration extends tk.mybatis.mapper.session.Configuration {

    private final MapperRegistry mapperRegistry = new MapperRegistry(this);

    @Override
    public org.apache.ibatis.binding.MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Override
    public void addMappers(String packageName, Class<?> superType) {
        getMapperRegistry().addMappers(packageName, superType);
    }

    @Override
    public void addMappers(String packageName) {
        getMapperRegistry().addMappers(packageName);
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        getMapperRegistry().addMapper(type);
    }

    @Override
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return getMapperRegistry().getMapper(type, sqlSession);
    }

    @Override
    public boolean hasMapper(Class<?> type) {
        return getMapperRegistry().hasMapper(type);
    }
}
