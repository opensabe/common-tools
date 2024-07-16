package io.github.opensabe.mapstruct;

import io.github.opensabe.mapstruct.core.FromMapMapper;
import io.github.opensabe.mapstruct.core.ObjectConverter;
import io.github.opensabe.mapstruct.core.RegisterRepository;
import org.mapstruct.Mapper;

@Mapper(uses= ObjectConverter.class )
@RegisterRepository
public interface JobMapper extends FromMapMapper<Job> {

}
