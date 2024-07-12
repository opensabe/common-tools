package io.github.opensabe.mapstruct.core;

/**
 * 自己复制自己mapper, 浅复制
 * @author heng.ma
 * @param <S>
 */
public interface SelfCopyMapper<S> extends CommonCopyMapper<S, S> {

    @Override
    default S from(S target) {
        return map(target);
    }
}
