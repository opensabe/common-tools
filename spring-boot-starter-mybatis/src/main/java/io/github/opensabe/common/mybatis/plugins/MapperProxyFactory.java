package io.github.opensabe.common.mybatis.plugins;

import org.apache.ibatis.binding.MapperProxy;
import org.apache.ibatis.session.SqlSession;

/**
 * 自定义Mapper代理
 * @author maheng
 * @param <T>
 */
public class MapperProxyFactory<T> extends org.apache.ibatis.binding.MapperProxyFactory<T> {
    public MapperProxyFactory(Class<T> mapperInterface) {
        super(mapperInterface);
    }

    @Override
    protected T newInstance(MapperProxy<T> mapperProxy) {
        return super.newInstance(mapperProxy);
    }

    @Override
    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = new io.github.opensabe.common.mybatis.plugins.MapperProxy<>(sqlSession, getMapperInterface(), getMethodCache());
        return newInstance(mapperProxy);
    }
}
