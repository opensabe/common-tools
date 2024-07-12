package io.github.opensabe.mapstruct.core;

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
    static MapperRepository getInstance() {
        try {
            return  (MapperRepository) Class.forName(MapperRepository.class.getName()+"Impl")
                    .getConstructor().newInstance();
        } catch (Throwable ignore) {
//            throw new RuntimeException(e);
        }
        return null;
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
    @SuppressWarnings("unchecked")
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


}
