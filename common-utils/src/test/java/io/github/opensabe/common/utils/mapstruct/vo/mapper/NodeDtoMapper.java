package io.github.opensabe.common.utils.mapstruct.vo.mapper;

import org.mapstruct.Mapper;
import io.github.opensabe.mapstruct.core.SelfCopyMapper;
import io.github.opensabe.common.utils.mapstruct.vo.NodeDto;
import io.github.opensabe.mapstruct.core.SelfConvertor;

@Mapper(uses = SelfConvertor.class) 
public interface NodeDtoMapper extends SelfCopyMapper<NodeDto> {

}
