package io.github.opensabe.mapstruct.core;

/**
 * target source Mapper
 * @author heng.ma
 * @param <S>   target类型
 * @param <T>   source类型
 */
public class ReverseMapper<S, T> implements CommonCopyMapper<S, T> {

    private final CommonCopyMapper<T, S> delegate;

    public ReverseMapper(CommonCopyMapper<T, S> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T map(S source) {
        return delegate.from(source);
    }

    @Override
    public S from(T target) {
        return delegate.map(target);
    }
}
