/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
