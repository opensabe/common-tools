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

    @Override
/**
 * getType 方法。
 */
    public JavaType getType() {
        return delegate.getType();
    }

    @Override
/**
 * getBeanClass 方法。
 */
    public Class<?> getBeanClass() {
        return delegate.getBeanClass();
    }

    @Override
/**
 * isNonStaticInnerClass 方法。
 */
    public boolean isNonStaticInnerClass() {
        return delegate.isNonStaticInnerClass();
    }

    @Override
/**
 * findJsonKeyAccessor 方法。
 */
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
/**
 * findClassDescription 方法。
 */
    public String findClassDescription() {
        return delegate.findClassDescription();
    }

    @Override
/**
 * getClassInfo 方法。
 */
    public AnnotatedClass getClassInfo() {
        return delegate.getClassInfo();
    }

    @Override
/**
 * getObjectIdInfo 方法。
 */
    public ObjectIdInfo getObjectIdInfo() {
        return delegate.getObjectIdInfo();
    }

    @Override
/**
 * hasKnownClassAnnotations 方法。
 */
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
/**
 * getClassAnnotations 方法。
 */
    public Annotations getClassAnnotations() {
        return delegate.getClassAnnotations();
    }

    @Override
/**
 * findProperties 方法。
 */
    public List<BeanPropertyDefinition> findProperties() {
        return delegate.findProperties();
    }

    @Override
/**
 * getIgnoredPropertyNames 方法。
 */
    public Set<String> getIgnoredPropertyNames() {
        return delegate.getIgnoredPropertyNames();
    }

    @Override
/**
 * findBackReferences 方法。
 */
    public List<BeanPropertyDefinition> findBackReferences() {
        return delegate.findBackReferences();
    }

//    @Override
//    public Map<String, AnnotatedMember> findBackReferenceProperties() {
//        return delegate.findBackReferenceProperties();
//    }

    @Override
/**
 * getConstructors 方法。
 */
    public List<AnnotatedConstructor> getConstructors() {
        return delegate.getConstructors();
    }

    @Override
    public List<AnnotatedAndMetadata<AnnotatedConstructor, JsonCreator.Mode>> getConstructorsWithMode() {
        return delegate.getConstructorsWithMode();
    }

    @Override
/**
 * getFactoryMethods 方法。
 */
    public List<AnnotatedMethod> getFactoryMethods() {
        return delegate.getFactoryMethods();
    }

    @Override
    public List<AnnotatedAndMetadata<AnnotatedMethod, JsonCreator.Mode>> getFactoryMethodsWithMode() {
        return delegate.getFactoryMethodsWithMode();
    }

    @Override
/**
 * findDefaultConstructor 方法。
 */
    public AnnotatedConstructor findDefaultConstructor() {
        return delegate.findDefaultConstructor();
    }

    @Override
/**
 * getPotentialCreators 方法。
 */
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
/**
 * findJsonValueAccessor 方法。
 */
    public AnnotatedMember findJsonValueAccessor() {
        return delegate.findJsonValueAccessor();
    }

    @Override
/**
 * findAnyGetter 方法。
 */
    public AnnotatedMember findAnyGetter() {
        return delegate.findAnyGetter();
    }

    @Override
/**
 * findAnySetterAccessor 方法。
 */
    public AnnotatedMember findAnySetterAccessor() {
        return delegate.findAnySetterAccessor();
    }

    @Override
/**
 * findMethod 方法。
 */
    public AnnotatedMethod findMethod(String name, Class<?>[] paramTypes) {
        return delegate.findMethod(name, paramTypes);
    }

//    @Override
//    public AnnotatedMethod findJsonValueMethod() {
//        return delegate.findJsonValueMethod();
//    }

    @Override
/**
 * findPropertyInclusion 方法。
 */
    public JsonInclude.Value findPropertyInclusion(JsonInclude.Value defValue) {
        return delegate.findPropertyInclusion(defValue);
    }

    @Override
/**
 * findExpectedFormat 方法。
 */
    public JsonFormat.Value findExpectedFormat() {
        return delegate.findExpectedFormat();
    }

    @Override
/**
 * findExpectedFormat 方法。
 */
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
/**
 * findPOJOBuilder 方法。
 */
    public Class<?> findPOJOBuilder() {
        return delegate.findPOJOBuilder();
    }

    @Override
/**
 * findPOJOBuilderConfig 方法。
 */
    public JsonPOJOBuilder.Value findPOJOBuilderConfig() {
        return delegate.findPOJOBuilderConfig();
    }

    @Override
/**
 * instantiateBean 方法。
 */
    public Object instantiateBean(boolean fixAccess) {
        return delegate.instantiateBean(fixAccess);
    }

    @Override
/**
 * findDefaultViews 方法。
 */
    public Class<?>[] findDefaultViews() {
        return delegate.findDefaultViews();
    }
}
