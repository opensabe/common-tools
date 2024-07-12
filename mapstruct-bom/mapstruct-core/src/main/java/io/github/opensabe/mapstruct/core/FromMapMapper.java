package io.github.opensabe.mapstruct.core;

import java.util.Map;

/**
 * Map转成对象Mapper，浅复制
 * @author heng.ma
 * @param <T>   target类型
 */
public interface FromMapMapper <T> {

    /**
     * 通过Map转成对象
     * <p>
     *     该转换不会deepClone, 只把Map中的key跟target中的field最对应，并把value强转成field类型
     * </p>
     * @param map map实例
     * @return  instance of target
     * @throws ClassCastException 如果key跟filed name相同，但是value跟filed类型不同
     */
    T fromMap (Map<String, Object> map);

}
