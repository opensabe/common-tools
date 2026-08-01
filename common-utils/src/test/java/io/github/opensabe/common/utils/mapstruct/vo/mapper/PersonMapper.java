package io.github.opensabe.common.utils.mapstruct.vo.mapper;

import org.mapstruct.Mapper;
import io.github.opensabe.mapstruct.core.SelfCopyMapper;
import io.github.opensabe.common.utils.mapstruct.vo.Person;

@Mapper
public interface PersonMapper extends SelfCopyMapper<Person> {

}
