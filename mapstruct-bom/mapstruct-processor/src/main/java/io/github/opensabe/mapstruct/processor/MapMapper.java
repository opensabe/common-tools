package io.github.opensabe.mapstruct.processor;

import java.util.Map;

/**
 * Map转对象Mapper元数据
 * @author heng.ma
 */
public class MapMapper extends AbstractMapper {

    protected MapMapper(String packageName, String sourceClass, String mapperName, boolean cycle) {
        super(packageName, sourceClass, mapperName, cycle);
    }


    @Override
    public String getTargetClass() {
        return Map.class.getName();
    }
}
