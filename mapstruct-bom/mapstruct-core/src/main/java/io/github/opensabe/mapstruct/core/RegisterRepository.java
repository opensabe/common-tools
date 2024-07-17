package io.github.opensabe.mapstruct.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Auto register to {@link MapperRepository}
 * if the mapper with this annotation and implements {@link CommonCopyMapper}, {@link SelfCopyMapper}, {@link FromMapMapper}
 * @author heng.ma
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RegisterRepository {
}
