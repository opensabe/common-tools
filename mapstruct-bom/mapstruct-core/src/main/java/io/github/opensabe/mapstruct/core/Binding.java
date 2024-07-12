package io.github.opensabe.mapstruct.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 两个对象绑定复制关系，只有标记以后才可以用BeanUtils来复制对象
 * <p>
 *     放到vo类上，会在maven编译时，自动生成MapStruct mapper
 * </p>
 * @author heng.ma
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Binding {

    /**
     * 需要和哪些vo绑定关系，
     * <p>
     *   如果为空，则只可以自己复制自己，以及Map转成该对象
     * </p>
     */
    Class<?> [] value () default {};

    /**
     *  是否有循环引用，
     * <p>
     *  对象中有个属性类型为该类型本身（树状结构），或者Collection等容器中的元素类型为该类型本身，
     *  如果有需要设置成true,否则构建时会{@link StackOverflowError}
     * </p>
     */
    boolean cycle () default false;
}
