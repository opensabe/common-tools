package io.github.opensabe.common.utils.mapstruct.vo.mapper;

import org.mapstruct.Mapper;
import java.util.Map;
import io.github.opensabe.mapstruct.core.ObjectConverter;
import io.github.opensabe.mapstruct.core.FromMapMapper;
import io.github.opensabe.common.utils.mapstruct.vo.Types;

@Mapper(uses=ObjectConverter.class )
public interface TypesMapMapper extends FromMapMapper<Types> {

}
