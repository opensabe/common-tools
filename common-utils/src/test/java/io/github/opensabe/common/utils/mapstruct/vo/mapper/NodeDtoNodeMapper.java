package io.github.opensabe.common.utils.mapstruct.vo.mapper;

import org.mapstruct.Mapper;
import io.github.opensabe.mapstruct.core.CommonCopyMapper;
import io.github.opensabe.mapstruct.core.CycleAvoidingMappingContext;
import org.mapstruct.Context;

import io.github.opensabe.common.utils.mapstruct.vo.NodeDto;
import io.github.opensabe.common.utils.mapstruct.vo.Node;

@Mapper(uses = CycleAvoidingMappingContext.class) 
public interface NodeDtoNodeMapper extends CommonCopyMapper<NodeDto, Node> {

}
