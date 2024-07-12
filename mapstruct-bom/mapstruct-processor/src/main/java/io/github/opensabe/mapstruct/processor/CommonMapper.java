package io.github.opensabe.mapstruct.processor;

/**
 * source target绑定关系Mapper元数据
 * @author heng.ma
 */
public class CommonMapper extends AbstractMapper {

    /**
     * target class simple name
     */
    private final String targetName;

    /**
     * target class name
     */
    private final String targetClass;


    public CommonMapper(String packageName, String sourceClass, String mapperName, String targetClass, boolean cycle) {
        super(packageName, sourceClass, mapperName, cycle);
        this.targetName = classSimpleName(targetClass);
        this.targetClass = targetClass;
        addImports(targetClass);

    }

    public String getTargetName() {
        return targetName;
    }

    @Override
    public String getTargetClass() {
        return targetClass;
    }
}