package ${packageName};

import org.mapstruct.Mapper;
import java.util.Map;
import io.github.opensabe.mapstruct.core.ObjectConverter;
import io.github.opensabe.mapstruct.core.FromMapMapper;
<#list imports as i>
import ${i};
</#list>

@Mapper(uses=ObjectConverter.class <#if cycle>, disableSubMappingMethodsGeneration = true </#if>)
public interface ${mapperName} extends FromMapMapper<${sourceName}> {

}