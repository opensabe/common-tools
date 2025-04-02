package io.github.opensabe.springdoc.converters;

import com.fasterxml.jackson.databind.type.SimpleType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Iterator;

/**
 * 生成swagger文档时，忽略Void类型，官方默认将Void类型显示为Object
 * @author heng.ma
 */
public class VoidModelResolver implements ModelConverter {

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

    @Override
    public boolean isOpenapi31() {
        return ModelConverter.super.isOpenapi31();
    }
}
