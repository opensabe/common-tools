package io.github.opensabe.mapstruct;

import io.github.opensabe.mapstruct.core.FromMapMapper;
import io.github.opensabe.mapstruct.core.MapperRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class CustomerMapperTest {

    MapperRepository repository = MapperRepository.getInstance();

    @Test
    void test () {
        FromMapMapper<Job> mapMapper = repository.getMapMapper(Job.class);
        Assertions.assertThat(mapMapper)
                .isNotNull()
                .isInstanceOf(JobMapper.class);
    }
}
