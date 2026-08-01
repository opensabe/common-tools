package io.github.opensabe.common.utils.mapstruct.vo.mapper;

import org.mapstruct.Mapper;
import java.util.Map;
import io.github.opensabe.mapstruct.core.ObjectConverter;
import io.github.opensabe.mapstruct.core.FromMapMapper;
import io.github.opensabe.common.utils.mapstruct.vo.NodeDto;

@Mapper(uses=ObjectConverter.class , disableSubMappingMethodsGeneration = true )
public interface NodeDtoMapMapper extends FromMapMapper<NodeDto> {

}
