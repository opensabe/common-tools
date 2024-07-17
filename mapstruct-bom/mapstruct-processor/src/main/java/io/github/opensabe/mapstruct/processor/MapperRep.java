package io.github.opensabe.mapstruct.processor;

import java.util.HashSet;
import java.util.Set;

/**
 * 保存已经生成过的Mapper
 * @author heng.ma
 */
@SuppressWarnings("unused")
public class MapperRep {

    private final Set<String> targetSource = new HashSet<>();

    /**
     * target 跟 source 绑定的mapper
     */
    private final Set<MapperPair> common = new HashSet<>();

    /**
     * 通过Map来生成对象的mapper
     */
    private final Set<MapperPair> map = new HashSet<>();

    /**
     * 自己复制属性的mapper
     */
    private final Set<MapperPair> self = new HashSet<>();
    private final Set<String> imports = new HashSet<>();

    void add (AbstractMapper mapper) {
        targetSource.add(mapper.getSourceClass()+ mapper.getTargetClass());

        if (mapper instanceof SelfMapper) {
            self.add(new MapperPair(mapper.getMapperName(), mapper.getSourceName()));
        }else if (mapper instanceof MapMapper) {
            map.add(new MapperPair(mapper.getMapperName(), mapper.getSourceName()));
        }else if (mapper instanceof CommonMapper commonMapper){
            common.add(new MapperPair(commonMapper.getMapperName(), commonMapper.getSourceName(), commonMapper.getTargetName()));
        }

        //导入Mapper接口的包
        imports.add(mapper.getPackageName()+"."+mapper.getMapperName());
        //导入Mapstruct生成的MaperImpl包
        imports.add(mapper.getPackageName()+"."+mapper.getMapperName()+"Impl");
        //导入source class
        imports.add(mapper.getSourceClass());
        //导入target class
        imports.add(mapper.getTargetClass());
    }

    /**
     * Processor中判断该Mapper是否已经生成,target 跟 source有一个即可
     * @param mapper    mapper元数据
     * @return  true target或者source任意一个Mapper已经生成，false 两个mapper都未生成
     */
    boolean contains (AbstractMapper mapper) {
        return targetSource.contains(mapper.getSourceClass()+mapper.getTargetClass())
                || targetSource.contains(mapper.getTargetClass()+mapper.getSourceClass());
    }

    public static class MapperPair  {
        private final String bean;
        private final String from;
        private final String to;
        public MapperPair(String bean, String from, String to) {
            this.bean = bean;
            this.from = from;
            this.to = to;
        }
        public MapperPair(String bean, String from) {
            this(bean, from ,null);
        }

        public String getBean() {
            return bean;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }
    }

    public Set<MapperPair> getCommon() {
        return common;
    }

    public Set<MapperPair> getMap() {
        return map;
    }

    public Set<MapperPair> getSelf() {
        return self;
    }

    public Set<String> getImports() {
        return imports;
    }

    public void destroy () {
        targetSource.clear();
        common.clear();
        map.clear();
        self.clear();
        imports.clear();
    }
}
