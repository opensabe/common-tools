package io.github.opensabe.common.utils.mapstruct.vo.mapper;

import org.mapstruct.Mapper;
import io.github.opensabe.mapstruct.core.CommonCopyMapper;

import io.github.opensabe.common.utils.mapstruct.vo.Person;
import io.github.opensabe.common.utils.mapstruct.vo.PersonRecord;

@Mapper
public interface PersonPersonRecordMapper extends CommonCopyMapper<Person, PersonRecord> {

}
