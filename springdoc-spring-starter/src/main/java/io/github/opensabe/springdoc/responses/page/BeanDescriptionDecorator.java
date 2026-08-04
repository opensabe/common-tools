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

/** delegate 字段。 */
    private final BeanDescription delegate;

    protected BeanDescriptionDecorator(BeanDescription delegate) {
        super(null);
        this.delegate = delegate;
    }

/**
 * getType 方法。
 */
    @Override
    public JavaType getType() {
        return delegate.getType();
    }

/**
 * getBeanClass 方法。
 */
    @Override
    public Class<?> getBeanClass() {
        return delegate.getBeanClass();
    }

/**
 * isNonStaticInnerClass 方法。
 */
    @Override
    public boolean isNonStaticInnerClass() {
        return delegate.isNonStaticInnerClass();
    }

/**
 * findJsonKeyAccessor 方法。
 */
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

/**
 * findClassDescription 方法。
 */
    @Override
    public String findClassDescription() {
        return delegate.findClassDescription();
    }

/**
 * getClassInfo 方法。
 */
    @Override
    public AnnotatedClass getClassInfo() {
        return delegate.getClassInfo();
    }

/**
 * getObjectIdInfo 方法。
 */
    @Override
    public ObjectIdInfo getObjectIdInfo() {
        return delegate.getObjectIdInfo();
    }

/**
 * hasKnownClassAnnotations 方法。
 */
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

/**
 * getClassAnnotations 方法。
 */
    @Override
    public Annotations getClassAnnotations() {
        return delegate.getClassAnnotations();
    }

/**
 * findProperties 方法。
 */
    @Override
    public List<BeanPropertyDefinition> findProperties() {
        return delegate.findProperties();
    }

/**
 * getIgnoredPropertyNames 方法。
 */
    @Override
    public Set<String> getIgnoredPropertyNames() {
        return delegate.getIgnoredPropertyNames();
    }

/**
 * findBackReferences 方法。
 */
    @Override
    public List<BeanPropertyDefinition> findBackReferences() {
        return delegate.findBackReferences();
    }

//    @Override
//    public Map<String, AnnotatedMember> findBackReferenceProperties() {
//        return delegate.findBackReferenceProperties();
//    }

/**
 * getConstructors 方法。
 */
    @Override
    public List<AnnotatedConstructor> getConstructors() {
        return delegate.getConstructors();
    }

    @Override
    public List<AnnotatedAndMetadata<AnnotatedConstructor, JsonCreator.Mode>> getConstructorsWithMode() {
        return delegate.getConstructorsWithMode();
    }

/**
 * getFactoryMethods 方法。
 */
    @Override
    public List<AnnotatedMethod> getFactoryMethods() {
        return delegate.getFactoryMethods();
    }

    @Override
    public List<AnnotatedAndMetadata<AnnotatedMethod, JsonCreator.Mode>> getFactoryMethodsWithMode() {
        return delegate.getFactoryMethodsWithMode();
    }

/**
 * findDefaultConstructor 方法。
 */
    @Override
    public AnnotatedConstructor findDefaultConstructor() {
        return delegate.findDefaultConstructor();
    }

/**
 * getPotentialCreators 方法。
 */
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

/**
 * findJsonValueAccessor 方法。
 */
    @Override
    public AnnotatedMember findJsonValueAccessor() {
        return delegate.findJsonValueAccessor();
    }

/**
 * findAnyGetter 方法。
 */
    @Override
    public AnnotatedMember findAnyGetter() {
        return delegate.findAnyGetter();
    }

/**
 * findAnySetterAccessor 方法。
 */
    @Override
    public AnnotatedMember findAnySetterAccessor() {
        return delegate.findAnySetterAccessor();
    }

/**
 * findMethod 方法。
 */
    @Override
    public AnnotatedMethod findMethod(String name, Class<?>[] paramTypes) {
        return delegate.findMethod(name, paramTypes);
    }

//    @Override
//    public AnnotatedMethod findJsonValueMethod() {
//        return delegate.findJsonValueMethod();
//    }

/**
 * findPropertyInclusion 方法。
 */
    @Override
    public JsonInclude.Value findPropertyInclusion(JsonInclude.Value defValue) {
        return delegate.findPropertyInclusion(defValue);
    }

/**
 * findExpectedFormat 方法。
 */
    @Override
    public JsonFormat.Value findExpectedFormat() {
        return delegate.findExpectedFormat();
    }

/**
 * findExpectedFormat 方法。
 */
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

/**
 * findPOJOBuilder 方法。
 */
    @Override
    public Class<?> findPOJOBuilder() {
        return delegate.findPOJOBuilder();
    }

/**
 * findPOJOBuilderConfig 方法。
 */
    @Override
    public JsonPOJOBuilder.Value findPOJOBuilderConfig() {
        return delegate.findPOJOBuilderConfig();
    }

/**
 * instantiateBean 方法。
 */
    @Override
    public Object instantiateBean(boolean fixAccess) {
        return delegate.instantiateBean(fixAccess);
    }

/**
 * findDefaultViews 方法。
 */
    @Override
    public Class<?>[] findDefaultViews() {
        return delegate.findDefaultViews();
    }
}
