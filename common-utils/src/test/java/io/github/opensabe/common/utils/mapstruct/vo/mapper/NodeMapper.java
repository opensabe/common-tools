package io.github.opensabe.common.utils.mapstruct.vo.mapper;

import org.mapstruct.Mapper;
import io.github.opensabe.mapstruct.core.SelfCopyMapper;
import io.github.opensabe.common.utils.mapstruct.vo.Node;
import io.github.opensabe.mapstruct.core.SelfConvertor;

@Mapper(uses = SelfConvertor.class) 
public interface NodeMapper extends SelfCopyMapper<Node> {

}
