package io.github.opensabe.mapstruct.core;

import java.lang.reflect.InvocationTargetException;

/**
 * Mapper Repository, 保存所有的Mapper
 * <p>
 *     如果对象被{@link Binding}标记，则生成的Mapper会保存到这里面
 * </p>
 * @author heng.ma
 */
public interface MapperRepository {

    /**
     * 获取MapperRepository实例，在获取实例之前必须用maven编译一下项目
     * @return  instance of MapperRepository
     */
    @SuppressWarnings("unchecked")
    static MapperRepository getInstance() {
        MapperRepository repository = null;
        try {
            repository = (MapperRepository) Class.forName(MapperRepository.class.getName()+"Impl")
                    .getConstructor().newInstance();
        } catch (Throwable ignore) {

        }
        try {
            Class<MapperRegister>  customerRegister = (Class<MapperRegister>)Class.forName(MapperRegister.class.getName() + "Impl");
            customerRegister.getConstructor(MapperRepository.class).newInstance(repository).register();
        } catch (Throwable ignore) {

        }
        return repository;
    }


    /**
     * 获取Mapper,如果source跟target类型相同，则返回{@link SelfCopyMapper}
     * @param source    source class
     * @param target    target class
     * @return          instance of CommonCopyMapper
     * @param <S>       type of source
     * @param <T>       type of target
     */
    <S, T> CommonCopyMapper<S, T> getMapper (Class<S> source, Class<T> target);

    /**
     * @see #getMapper(Class, Class)
     */
    default <S> SelfCopyMapper<S> getMapper (Class<S> source) {
        return (SelfCopyMapper<S>)getMapper(source, source);
    }

    /**
     * 获取map转对象Mapper
     * @param target    target class
     * @return          instance of FromMapMapper
     * @param <T>       type of target
     */
    <T> FromMapMapper<T> getMapMapper (Class<T> target);


    /**
     * register custom mapper to repository
     * @param source    source class
     * @param target    target class
     * @param mapper    mapper from source type to target type
     * @param <S>       type of source
     * @param <T>       type of target
     */
    <S, T> void register (Class<S> source, Class<T> target, CommonCopyMapper<S, T> mapper);
    <T> void register (Class<T> target, FromMapMapper<T> mapper);
    <T> void register (Class<T> target, SelfCopyMapper<T> mapper);
}
