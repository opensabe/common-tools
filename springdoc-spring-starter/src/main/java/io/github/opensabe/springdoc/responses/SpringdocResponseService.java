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
package io.github.opensabe.springdoc.responses;

import io.github.opensabe.spring.cloud.parent.common.handler.ErrorMessage;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.service.GenericResponseService;
import org.springdoc.core.service.OperationService;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 生成swagger文档时处理{@link BizResponse}
 * @author heng.ma
 */
@Useless
public class SpringdocResponseService extends GenericResponseService {


    private final Useless useless;

    public SpringdocResponseService(OperationService operationService, SpringDocConfigProperties springDocConfigProperties, PropertyResolverUtils propertyResolverUtils) {
        super(operationService, springDocConfigProperties, propertyResolverUtils);
        this.useless = SpringdocResponseService.class.getDeclaredAnnotation(Useless.class);
    }

    @Override
    public Set<ApiResponse> getApiResponses(Method method) {
        var origin = super.getApiResponses(method);
        Set<ApiResponse> responses = findBizResponse(method).stream()
                .flatMap(this::responseStream)
                .collect(Collectors.toSet());
        // 合并逻辑示例，只需一次性过滤添加即可
        responses.addAll(origin.stream()
                .filter(o -> responses.stream().noneMatch(r -> r.responseCode().equals(o.responseCode())))
                .toList());

        return responses;
    }



    private Set<BizResponse> findBizResponse (Method method) {
        Class<?> declaringClass = method.getDeclaringClass();

        Set<BizResponses> apiResponsesDoc = AnnotatedElementUtils.findAllMergedAnnotations(method, BizResponses.class);
        Set<BizResponse> responses = apiResponsesDoc.stream()
                .flatMap(x -> Stream.of(x.value())).collect(Collectors.toSet());

        Set<BizResponses> apiResponsesDocDeclaringClass = AnnotatedElementUtils.findAllMergedAnnotations(declaringClass, BizResponses.class);
        responses.addAll(
                apiResponsesDocDeclaringClass.stream().flatMap(x -> Stream.of(x.value())).collect(Collectors.toSet()));

        Set<BizResponse> apiResponseDoc = AnnotatedElementUtils.findMergedRepeatableAnnotations(method, BizResponse.class);
        responses.addAll(apiResponseDoc);

        Set<BizResponse> apiResponseDocDeclaringClass = AnnotatedElementUtils.findMergedRepeatableAnnotations(declaringClass, BizResponse.class);
        responses.addAll(apiResponseDocDeclaringClass);
        return responses;
    }



    private Stream<ApiResponse> responseStream (BizResponse response) {
        return Arrays.stream(response.value().getEnumConstants())
                .filter(message -> response.exclude().length == 0 || !ArrayUtils.contains(response.exclude(), message.code()))
                .filter(message -> response.include().length == 0 || ArrayUtils.contains(response.include(), message.code()))
                .map(message -> createApiResponse(response, message));

    }

    private ApiResponse createApiResponse (BizResponse response, ErrorMessage message) {
        Content content = createContent(message);
        InvocationHandler invocationHandler = (Object proxy, Method method, Object[] args) -> {
            String methodName = method.getName();
            return switch (methodName) {
                case "description" ->  response.condition();
                case "responseCode" -> message.code()+"";
                case "content" ->  response.useReturnType()? new Content[0]: new Content[]{content};
                case "ref" -> "";
                case "useReturnTypeSchema" -> response.useReturnType();
                case "extensions" -> useless.extensions();
                case "header" -> useless.headers();
                case "links" -> useless.links();
                case "hashCode" -> hashCode(response);
                default -> null;
            };
        };
        return (ApiResponse) Proxy.newProxyInstance(ApiResponse.class.getClassLoader(),new Class[]{ApiResponse.class}, invocationHandler);
    }
    private Content createContent (ErrorMessage errorCode) {

        InvocationHandler invocationHandler = (Object proxy, Method method, Object[] args) -> {
                String methodName = method.getName();
                return switch (methodName) {
                    case "schemaProperties" ->  new SchemaProperty[] {
                            createSchemaProperty("bizCode", errorCode.code()+"","int"),
                            createSchemaProperty("message", errorCode.message(), ""),
                            createSchemaProperty("data", String.valueOf(errorCode.data()), "")
                        };
                    case "schema", "_if", "_then", "_else", "additionalPropertiesSchema", "contentSchema",
                         "propertyNames" -> useless.schema();
                    case "array", "additionalPropertiesArraySchema" -> useless.array();
                    case "encoding" -> useless.encoding();
                    case "extensions" -> useless.extensions();
                    case "examples" -> useless.examples();
                    case "dependentSchemas" -> useless.dependentSchemas();
                    case "oneOf", "anyOf", "allOf" -> useless.schemas();

                    default -> null;
                };
            };

        return (Content) Proxy.newProxyInstance(Content.class.getClassLoader(),new Class[]{Content.class}, invocationHandler);
    }
    private SchemaProperty createSchemaProperty (String name, String example, String type) {

        InvocationHandler invocationHandler = (Object proxy, Method method, Object[] args) -> {
                String methodName = method.getName();
                return switch (methodName) {
                    case "name" ->  name;
//                    case "schema" -> createSchema(example, type);
                    case "schema" -> useless.schema();
                    case "array" -> useless.array();
                    default -> null;
                };
            };

        return (SchemaProperty) Proxy.newProxyInstance(SchemaProperty.class.getClassLoader(),new Class[]{SchemaProperty.class}, invocationHandler);
    }
//    private Schema createSchema (String example, String type) {
//
//        InvocationHandler invocationHandler = (Object proxy, Method method, Object[] args) -> {
//                String methodName = method.getName();
//                return switch (methodName) {
//                    case "type" -> type;
//                    case "example", "defaultValue" -> example;
//
//                    case "implementation", "not", "contains", "contentSchema", "propertyNames", "additionalItems",
//                         "unevaluatedItems", "_if", "_else", "then", "exampleClasses", "unevaluatedProperties",
//                         "additionalPropertiesSchema" -> Void.class;
//                    case "oneOf", "anyOf", "allOf", "subTypes", "prefixItems" -> new Class[0];
//                    case "name", "title", "maximum", "minimum", "pattern", "description", "format", "ref",
//                         "discriminatorProperty", "$id", "$schema", "$anchor", "$vocabulary", "$dynamicAnchor",
//                         "contentEncoding", "contentMediaType", "_const", "$comment" -> "";
//                    case "multipleOf" -> 0d;
//                    case "exclusiveMaximum", "exclusiveMinimum", "nullable", "deprecated", "hidden", "enumAsRef",
//                         "readOnly", "writeOnly", "required" -> false;
//                    case "maxLength", "maxContains" -> Integer.MAX_VALUE;
//                    case "minLength", "maxProperties", "exclusiveMaximumValue", "exclusiveMinimumValue",
//                         "minContains", "minProperties" -> 0;
//                    case "requiredProperties", "allowableValues", "types", "examples" -> new String[0];
//                    case "requiredMode" -> Schema.RequiredMode.AUTO;
//                    case "accessMode" -> Schema.AccessMode.AUTO;
//                    case "externalDocs" -> useless.externalDocs();
//                    case "discriminatorMapping" -> useless.discriminatorMapping();
//                    case "extensions" -> useless.extensions();
//                    case "additionalProperties" -> Schema.AdditionalPropertiesValue.USE_ADDITIONAL_PROPERTIES_ANNOTATION;
//                    case "dependentRequiredMap" -> useless.dependentRequiredMap();
//                    case "dependentSchemas", "patternProperties", "properties" -> useless.patternProperties();
//
//
//                    default -> null;
//                };
//
//            };
//
//        return  (Schema) Proxy.newProxyInstance(Schema.class.getClassLoader(),new Class[]{Schema.class}, invocationHandler);
//    }


    private int hashCode (Object ... os) {
        return Objects.hash(os) & Integer.MAX_VALUE;
    }

}
