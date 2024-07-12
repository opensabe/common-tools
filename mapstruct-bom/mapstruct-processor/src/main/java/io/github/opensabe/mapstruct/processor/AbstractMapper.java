package io.github.opensabe.mapstruct.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * mapper ftl中元数据基类
 * @author heng.ma
 */
public abstract class AbstractMapper {

    /**
     * 包名
     */
    private final String packageName;

    /**
     * 需要导入的包
     */
    private final Set<String> imports = new HashSet<>();

    /**
     * source class simple name
     */
    private final String sourceName;

    /**
     * source class name
     */
    private final String sourceClass;

    /**
     * 生成的Mapper接口名称
     * <ol>
     *     <Li>如果是source跟target SourceTargetMapper</Li>
     *     <Li>如果是没有target SourceMapper</Li>
     *     <Li>如果是MapMapper SourceMapMapper</Li>
     * </ol>
     */
    private final String mapperName;

    /**
     * 是否有循环引用(属性为该对象本身，或者list map中的元素为该对象本身)
     */
    private final boolean cycle;

    protected AbstractMapper(String packageName,  String sourceClass, String mapperName, boolean cycle) {
        this.packageName = packageName;
        this.sourceClass = sourceClass;
        this.mapperName = mapperName;
        this.sourceName = classSimpleName(sourceClass);
        this.cycle = cycle;
        addImports(sourceClass);
    }

    public String getTargetClass () {
        return "";
    }

    protected void addImports (String imports) {
        this.imports.add(imports);
    }
    protected void addImports (List<String> imports) {
        this.imports.addAll(imports);
    }

    public String getPackageName() {
        return packageName;
    }

    public Set<String> getImports() {
        return imports;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getMapperName() {
        return mapperName;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public boolean getCycle() {
        return cycle;
    }

    protected String classSimpleName (String className) {
        return className.substring(className.lastIndexOf(".")+1);
    }

    /**
     * 获取ftl模版
     * @return
     */
    String template() {
        return this.getClass().getSimpleName()+"Generator.ftl";
    }

}