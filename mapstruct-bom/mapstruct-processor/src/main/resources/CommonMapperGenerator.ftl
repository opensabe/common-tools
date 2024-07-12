package ${packageName};

import org.mapstruct.Mapper;
import io.github.opensabe.mapstruct.core.CommonCopyMapper;
<#if cycle>
import io.github.opensabe.mapstruct.core.CycleAvoidingMappingContext;
import org.mapstruct.Context;
</#if>

<#list imports as i>
import ${i};
</#list>

@Mapper<#if cycle>(uses = CycleAvoidingMappingContext.class) </#if>
public interface ${mapperName} extends CommonCopyMapper<${sourceName}, ${targetName}> {

}