package io.github.opensabe.mapstruct.processor;

/**
 * 生成相同类型的对象Mapper元数据
 * @author heng.ma
 */
public class SelfMapper extends AbstractMapper {

    protected SelfMapper(String packageName, String sourceClass, String mapperName, boolean cycle) {
        super(packageName, sourceClass, mapperName, cycle);
    }

    @Override
    public String getTargetClass() {
        return getSourceClass();
    }
}
