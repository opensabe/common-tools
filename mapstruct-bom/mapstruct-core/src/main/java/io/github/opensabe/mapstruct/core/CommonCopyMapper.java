package io.github.opensabe.mapstruct.core;


/**
 * 包含source跟target的Mapper
 * @author heng.ma
 * @param <S>   Type of source
 * @param <T>   Type of target
 */
public interface CommonCopyMapper<S, T> {

    /**
     * source对象转换成target对象
     * @param source    instance of source
     * @return  instance of target
     */
    T map (S source);

    /**
     * target 对象转 source 对象
     * @param target    instance of target
     * @return  instance of source
     */
    S from (T target);

}
