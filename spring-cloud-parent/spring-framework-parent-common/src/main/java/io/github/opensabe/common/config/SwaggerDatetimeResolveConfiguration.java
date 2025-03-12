package io.github.opensabe.common.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.core.util.PrimitiveType;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author heng.ma
 */
public class SwaggerDatetimeResolveConfiguration {




    @Bean
    @ConditionalOnMissingBean
    public DateTimeModelConverter dateTimeModelConverter () {
        return new DateTimeModelConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public VoidModelResolver voidModelResolver () {
        return new VoidModelResolver();
    }


    public static class VoidModelResolver implements ModelConverter {

        private final SimpleType voidType;

        public VoidModelResolver() {
            this.voidType = SimpleType.constructUnsafe(Void.class);
        }

        @Override
        public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
            if (voidType.equals(type.getType())) {
                return null;
            }
            if (chain.hasNext()) {
                return chain.next().resolve(type, context, chain);
            }
            return null;
        }
    }



    public static class DateTimeModelConverter implements ModelConverter {
        private final JavaType javaType;
        private final Set<PrimitiveType> times;
        public DateTimeModelConverter() {
            this.javaType = SimpleType.constructUnsafe(LocalDateTime.class);
            this.times = Set.of(PrimitiveType.DATE, PrimitiveType.DATE_TIME, PrimitiveType.PARTIAL_TIME);
        }

        @Override
        public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
            if (isTime(type.getType())) {
                Schema<?> schema = PrimitiveType.LONG.createProperty();
                schema.setDefault(System.currentTimeMillis());
                Optional.ofNullable(AnnotationsUtils.mergeSchemaAnnotations(type.getCtxAnnotations(), javaType))
                        .map(resolvedSchemaOrArrayAnnotation ->
                                resolvedSchemaOrArrayAnnotation instanceof io.swagger.v3.oas.annotations.media.ArraySchema arr?
                                        arr.schema() :
                                        (io.swagger.v3.oas.annotations.media.Schema) resolvedSchemaOrArrayAnnotation
                        ).map(io.swagger.v3.oas.annotations.media.Schema::description)
                        .ifPresent(schema::setDescription);
                return schema;
            }
            return chain.hasNext()? chain.next().resolve(type, context, chain) : null;
        }



        private boolean isTime (Type rawType) {
            PrimitiveType primitiveType = PrimitiveType.fromName(rawType.getTypeName());
            if (Objects.isNull(primitiveType)) {
                try {
                    primitiveType = PrimitiveType.fromType(rawType);
                }catch (Throwable ignore) {

                }
            }
            return primitiveType != null && times.contains(primitiveType);
        }
    }

}
