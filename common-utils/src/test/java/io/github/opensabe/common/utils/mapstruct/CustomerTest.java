package io.github.opensabe.common.utils.mapstruct;

import io.github.opensabe.common.utils.mapstruct.vo.Activity;
import io.github.opensabe.common.utils.mapstruct.vo.ActivityDto;
import io.github.opensabe.mapstruct.core.CommonCopyMapper;
import io.github.opensabe.mapstruct.core.MapperRepository;
import io.github.opensabe.mapstruct.core.RegisterRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;

@DisplayName("自定义MapStruct映射器测试")
public class CustomerTest {

    private MapperRepository repository = MapperRepository.getInstance();

    @Mapper
    @RegisterRepository
    public interface CustomerMapper extends CommonCopyMapper<Activity, ActivityDto> {

        default String convert (String src) {
            return src+"Customer";
        }
    }

    @Test
    @DisplayName("测试自定义映射器注册和获取")
    void testOverride () {
        CommonCopyMapper<Activity, ActivityDto> mapper = repository.getMapper(Activity.class, ActivityDto.class);
        Assertions.assertThat(mapper)
                .isNotNull()
                .isInstanceOf(CustomerMapper.class);
    }

    @Test
    @DisplayName("测试自定义转换方法 - Activity到ActivityDto")
    void testCustomer () {
        CommonCopyMapper<Activity, ActivityDto> mapper = repository.getMapper(Activity.class, ActivityDto.class);
        Activity source = new Activity("a1");
        ActivityDto dto = mapper.map(source);
        Assertions.assertThat(dto.getName()).isEqualTo("a1Customer");
    }

    @Test
    @DisplayName("测试反向映射 - ActivityDto到Activity")
    void testRevise () {
        CommonCopyMapper<ActivityDto, Activity> mapper = repository.getMapper(ActivityDto.class, Activity.class);
        ActivityDto source = new ActivityDto("a1");
        Activity dto = mapper.map(source);
        Assertions.assertThat(dto.getName()).isEqualTo("a1Customer");
    }
}
