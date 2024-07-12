package ${packageName};

import org.mapstruct.Mapper;
import io.github.opensabe.mapstruct.core.SelfCopyMapper;
<#list imports as i>
import ${i};
</#list>
<#if cycle>
import io.github.opensabe.mapstruct.core.SelfConvertor;
</#if>

@Mapper<#if cycle>(uses = SelfConvertor.class) </#if>
public interface ${mapperName} extends SelfCopyMapper<${sourceName}> {

}