package io.github.opensabe.mapstruct.core;

import io.github.opensabe.mapstruct.core.MapperRegister;
import org.mapstruct.factory.Mappers;
<#list mappers as m>
import ${m.mapperClass};
import ${m.targetClass};
<#if m.sourceClass??>
import ${m.sourceClass};
</#if>
</#list>
public class MapperRegisterImpl extends MapperRegister {

    public MapperRegisterImpl(MapperRepository repository) {
        super(repository);
    }

    @Override
    public void register () {
        <#list mappers as mapper>
        <#if mapper.source??>
        register(${mapper.source}.class, ${mapper.target}.class, Mappers.getMapper(${mapper.mapper}.class));
        <#else>
        register(null, ${mapper.target}.class, Mappers.getMapper(${mapper.mapper}.class));
        </#if>
        </#list>
    }
}