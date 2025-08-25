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

import lombok.Getter;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.session.SqlSession;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义Mapper代理类，加上observation,因此MapperRegistry需要自定义，使用
 * 自定义的MapperProxyFactory
 * @author maheng
 */
public class MapperRegistry extends org.apache.ibatis.binding.MapperRegistry {
    @Getter
    private final Configuration config;
    @Getter
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();
    public MapperRegistry(Configuration config) {
        super(config);
        this.config = config;
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        if (type.isInterface()) {
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            MapperAnnotationBuilder parser = new MapperAnnotationBuilder(getConfig(), type);
            parser.parse();
            getKnownMappers().put(type, new MapperProxyFactory<>(type));
        }
    }

    @Override
    public <T> boolean hasMapper(Class<T> type) {
        return getKnownMappers().containsKey(type);
    }

    @Override
    public Collection<Class<?>> getMappers() {
        return Collections.unmodifiableCollection(getKnownMappers().keySet());
    }

    @Override
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory =  (MapperProxyFactory<T>) getKnownMappers().get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }
}
