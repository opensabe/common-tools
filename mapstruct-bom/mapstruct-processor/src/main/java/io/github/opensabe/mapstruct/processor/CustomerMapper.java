package io.github.opensabe.mapstruct.processor;

import java.util.Optional;

/**
 * customer mapper meta info
 * @author heng.ma
 */
public class CustomerMapper {

    private final String mapperClass;

    private final String mapper;

    private final String sourceClass;

    private final String source;

    private final String targetClass;

    private final String target;

    public CustomerMapper(String mapperClass, String sourceClass, String targetClass) {
        this.mapperClass = mapperClass;
        this.mapper = getName(mapperClass);
        this.sourceClass = sourceClass;
        this.source = Optional.ofNullable(sourceClass).map(this::getName).orElse(null);
        this.targetClass = targetClass;
        this.target = getName(targetClass);
    }

    private String getName (String className) {
        return className.substring(className.lastIndexOf(".")+1);
    }

    public String getMapperClass() {
        return mapperClass;
    }

    public String getMapper() {
        return mapper;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public String getSource() {
        return source;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public String getTarget() {
        return target;
    }
}
