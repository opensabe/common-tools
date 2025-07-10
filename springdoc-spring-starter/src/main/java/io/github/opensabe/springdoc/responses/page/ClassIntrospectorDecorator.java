package io.github.opensabe.springdoc.responses.page;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;

/**
 * @author heng.ma
 */
public class ClassIntrospectorDecorator extends ClassIntrospector {

    private final ClassIntrospector delegate;

    public ClassIntrospectorDecorator(ClassIntrospector delegate) {
        this.delegate = delegate;
    }

    @Override
    public ClassIntrospector copy() {
        return new ClassIntrospectorDecorator(delegate);
    }

    @Override
    public BeanDescription forSerialization(SerializationConfig cfg, JavaType type, MixInResolver r) {
        return delegate.forSerialization(cfg, type, r);
    }

    @Override
    public BeanDescription forDeserialization(DeserializationConfig cfg, JavaType type, MixInResolver r) {
        return delegate.forDeserialization(cfg, type, r);
    }

    @Override
    public BeanDescription forDeserializationWithBuilder(DeserializationConfig cfg, JavaType builderType, MixInResolver r, BeanDescription valueTypeDesc) {
        return delegate.forDeserializationWithBuilder(cfg, builderType, r, valueTypeDesc);
    }

//

    @Override
    public BeanDescription forCreation(DeserializationConfig cfg, JavaType type, MixInResolver r) {
        return delegate.forCreation(cfg, type, r);
    }

    @Override
    public BeanDescription forClassAnnotations(MapperConfig<?> cfg, JavaType type, MixInResolver r) {
        return delegate.forClassAnnotations(cfg, type, r);
    }

    @Override
    public BeanDescription forDirectClassAnnotations(MapperConfig<?> cfg, JavaType type, MixInResolver r) {
        return delegate.forDirectClassAnnotations(cfg, type, r);
    }
}
