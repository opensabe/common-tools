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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.introspect.AnnotatedAndMetadata;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.introspect.PotentialCreators;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.Converter;

/**
 * @author heng.ma
 */
public class BeanDescriptionDecorator extends BeanDescription {

    private final BeanDescription delegate;

    protected BeanDescriptionDecorator(BeanDescription delegate) {
        super(null);
        this.delegate = delegate;
    }

    @Override
    public JavaType getType() {
        return delegate.getType();
    }

    @Override
    public Class<?> getBeanClass() {
        return delegate.getBeanClass();
    }

    @Override
    public boolean isNonStaticInnerClass() {
        return delegate.isNonStaticInnerClass();
    }

    @Override
    public AnnotatedMember findJsonKeyAccessor() {
        return delegate.findJsonKeyAccessor();
    }

//    @Override
//    public AnnotatedMethod findAnySetter() {
//        return delegate.findAnySetter();
//    }

//    @Override
//    public AnnotatedMember findAnySetterField() {
//        return delegate.findAnySetterField();
//    }

    @Override
    public String findClassDescription() {
        return delegate.findClassDescription();
    }

    @Override
    public AnnotatedClass getClassInfo() {
        return delegate.getClassInfo();
    }

    @Override
    public ObjectIdInfo getObjectIdInfo() {
        return delegate.getObjectIdInfo();
    }

    @Override
    public boolean hasKnownClassAnnotations() {
        return delegate.hasKnownClassAnnotations();
    }

//    @Override
//    public TypeBindings bindingsForBeanType() {
//        return delegate.bindingsForBeanType();
//    }

//    @Override
//    public JavaType resolveType(Type jdkType) {
//        return delegate.resolveType(jdkType);
//    }

    @Override
    public Annotations getClassAnnotations() {
        return delegate.getClassAnnotations();
    }

    @Override
    public List<BeanPropertyDefinition> findProperties() {
        return delegate.findProperties();
    }

    @Override
    public Set<String> getIgnoredPropertyNames() {
        return delegate.getIgnoredPropertyNames();
    }

    @Override
    public List<BeanPropertyDefinition> findBackReferences() {
        return delegate.findBackReferences();
    }

//    @Override
//    public Map<String, AnnotatedMember> findBackReferenceProperties() {
//        return delegate.findBackReferenceProperties();
//    }

    @Override
    public List<AnnotatedConstructor> getConstructors() {
        return delegate.getConstructors();
    }

    @Override
    public List<AnnotatedAndMetadata<AnnotatedConstructor, JsonCreator.Mode>> getConstructorsWithMode() {
        return delegate.getConstructorsWithMode();
    }

    @Override
    public List<AnnotatedMethod> getFactoryMethods() {
        return delegate.getFactoryMethods();
    }

    @Override
    public List<AnnotatedAndMetadata<AnnotatedMethod, JsonCreator.Mode>> getFactoryMethodsWithMode() {
        return delegate.getFactoryMethodsWithMode();
    }

    @Override
    public AnnotatedConstructor findDefaultConstructor() {
        return delegate.findDefaultConstructor();
    }

    @Override
    public PotentialCreators getPotentialCreators() {
        return delegate.getPotentialCreators();
    }
//
//    @Override
//    public Constructor<?> findSingleArgConstructor(Class<?>... argTypes) {
//        return delegate.findSingleArgConstructor(argTypes);
//    }
//
//    @Override
//    public Method findFactoryMethod(Class<?>... expArgTypes) {
//        return delegate.findFactoryMethod(expArgTypes);
//    }

    @Override
    public AnnotatedMember findJsonValueAccessor() {
        return delegate.findJsonValueAccessor();
    }

    @Override
    public AnnotatedMember findAnyGetter() {
        return delegate.findAnyGetter();
    }

    @Override
    public AnnotatedMember findAnySetterAccessor() {
        return delegate.findAnySetterAccessor();
    }

    @Override
    public AnnotatedMethod findMethod(String name, Class<?>[] paramTypes) {
        return delegate.findMethod(name, paramTypes);
    }

//    @Override
//    public AnnotatedMethod findJsonValueMethod() {
//        return delegate.findJsonValueMethod();
//    }

    @Override
    public JsonInclude.Value findPropertyInclusion(JsonInclude.Value defValue) {
        return delegate.findPropertyInclusion(defValue);
    }

    @Override
    public JsonFormat.Value findExpectedFormat() {
        return delegate.findExpectedFormat();
    }

    @Override
    public JsonFormat.Value findExpectedFormat(JsonFormat.Value defValue) {
        return delegate.findExpectedFormat(defValue);
    }

    @Override
    public Converter<Object, Object> findSerializationConverter() {
        return delegate.findSerializationConverter();
    }

    @Override
    public Converter<Object, Object> findDeserializationConverter() {
        return delegate.findDeserializationConverter();
    }

    @Override
    public Map<Object, AnnotatedMember> findInjectables() {
        return delegate.findInjectables();
    }

    @Override
    public Class<?> findPOJOBuilder() {
        return delegate.findPOJOBuilder();
    }

    @Override
    public JsonPOJOBuilder.Value findPOJOBuilderConfig() {
        return delegate.findPOJOBuilderConfig();
    }

    @Override
    public Object instantiateBean(boolean fixAccess) {
        return delegate.instantiateBean(fixAccess);
    }

    @Override
    public Class<?>[] findDefaultViews() {
        return delegate.findDefaultViews();
    }
}
