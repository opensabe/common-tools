package io.github.opensabe.common.utils.mapstruct.vo.mapper;

import org.mapstruct.Mapper;
import java.util.Map;
import io.github.opensabe.mapstruct.core.ObjectConverter;
import io.github.opensabe.mapstruct.core.FromMapMapper;
import io.github.opensabe.common.utils.mapstruct.vo.Person;

@Mapper(uses=ObjectConverter.class )
public interface PersonMapMapper extends FromMapMapper<Person> {

}
