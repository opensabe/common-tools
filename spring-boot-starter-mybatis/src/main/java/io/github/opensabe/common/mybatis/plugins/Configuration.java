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
