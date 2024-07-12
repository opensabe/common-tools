package io.github.opensabe.mapstruct.core;

/**
 * map转对象时，map value跟filed value转换方式，强转
 * @author heng.ma
 */
public class ObjectConverter {

    @SuppressWarnings("unchecked")
    public <T> T covernt (Object ob) {
        return (T) ob;
    }
}
