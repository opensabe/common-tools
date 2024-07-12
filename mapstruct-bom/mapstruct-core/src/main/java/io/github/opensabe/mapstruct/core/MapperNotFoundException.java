package io.github.opensabe.mapstruct.core;

/**
 * @author heng.ma
 */
public class MapperNotFoundException extends RuntimeException {

    public MapperNotFoundException (Class<?> source, Class<?> target) {
        super("%s to %s mapper not found, please add @Binding in %s".formatted(source.getName(), target.getName(), source.getName()));
    }
    public MapperNotFoundException (Class<?> target) {
        super("%s mapper not found, please add @Binding in %s".formatted(target.getName(), target.getName()));
    }

}
