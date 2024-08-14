package io.github.opensabe.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.PrimitiveType;
import io.swagger.v3.oas.models.media.Schema;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * @author heng.ma
 */
public class SwaggerDatetimeResolveConfiguration {

    public SwaggerDatetimeResolveConfiguration() {
        ModelConverters.getInstance().addConverter(new DateTimeModelConverter(Json.mapper()));
    }

    public static class DateTimeModelConverter extends ModelResolver {

        public DateTimeModelConverter(ObjectMapper mapper) {
            super(mapper);
        }

        /**
         * update the return result while the type matches
         * {@link PrimitiveType#DATE}
         * {@link PrimitiveType#DATE_TIME}
         * {@link PrimitiveType#PARTIAL_TIME}x
         * and replace it with {@link PrimitiveType#LONG}
         */
        @Override
        public Schema resolve(AnnotatedType annotatedType, ModelConverterContext context, Iterator<ModelConverter> next) {
            if (isTime(annotatedType.getType())) {
                Schema schema = PrimitiveType.LONG.createProperty();
                resolveSchemaMembers(schema, annotatedType);
                schema.setDefault(System.currentTimeMillis());
                return schema;
            }
            return super.resolve(annotatedType, context, next);
        }

//
//        @Override
//        protected Object resolveDefaultValue(Annotated a, Annotation[] annotations, io.swagger.v3.oas.annotations.media.Schema schema) {
//            if (Objects.isNull(schema)) {
//                if (isTime(a.getRawType())) {
//                    return System.currentTimeMillis();
//                }
//            }else {
//                try {
//                    ObjectMapper mapper = ObjectMapperFactory.buildStrictGenericObjectMapper();
//                    mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//                    return mapper.readTree(schema.defaultValue());
//                } catch (IOException e) {
//                    return schema.defaultValue();
//                }
//            }
//            return super.resolveDefaultValue(a, annotations, schema);
//        }
//
//        @Override
//        protected Object resolveExample(Annotated a, Annotation[] annotations, io.swagger.v3.oas.annotations.media.Schema schema) {
//            if (Objects.isNull(schema)) {
//                if (isTime(a.getRawType())) {
//                    return System.currentTimeMillis();
//                }
//            }else {
//                if (!schema.example().isEmpty()) {
//                    try {
//                        ObjectMapper mapper = ObjectMapperFactory.buildStrictGenericObjectMapper();
//                        return mapper.readTree(schema.example());
//                    } catch (IOException e) {
//                        return schema.example();
//                    }
//                }
//            }
//            return null;
//        }

        private boolean isTime (Type rawType) {
            PrimitiveType primitiveType = PrimitiveType.fromName(rawType.getTypeName());
            if (Objects.isNull(primitiveType)) {
                try {
                    primitiveType = PrimitiveType.fromType(rawType);
                }catch (Throwable e) {

                }
            }
            return primitiveType != null && times.contains(primitiveType);
        }

        private Set<PrimitiveType> times = Set.of(PrimitiveType.DATE, PrimitiveType.DATE_TIME, PrimitiveType.PARTIAL_TIME);
    }

}
