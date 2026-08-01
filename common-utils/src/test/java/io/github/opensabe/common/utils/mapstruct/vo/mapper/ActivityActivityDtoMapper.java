package io.github.opensabe.common.utils.mapstruct.vo.mapper;

import org.mapstruct.Mapper;
import io.github.opensabe.mapstruct.core.CommonCopyMapper;

import io.github.opensabe.common.utils.mapstruct.vo.Activity;
import io.github.opensabe.common.utils.mapstruct.vo.ActivityDto;

@Mapper
public interface ActivityActivityDtoMapper extends CommonCopyMapper<Activity, ActivityDto> {

}
