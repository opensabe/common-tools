/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.utils.mapstruct;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.opensabe.common.utils.mapstruct.vo.Activity;
import io.github.opensabe.common.utils.mapstruct.vo.ActivityDto;
import io.github.opensabe.mapstruct.core.CommonCopyMapper;
import io.github.opensabe.mapstruct.core.MapperRepository;

/**
 * 自定义 MapStruct 映射器注册与转换测试。
 */
@DisplayName("自定义MapStruct映射器测试")
public class CustomerTest {

    static {
        MapstructTestBootstrap.init();
    }


    private MapperRepository repository = MapperRepository.getInstance();

    /**
     * 自定义映射器应被正确注册并可获取。
     */
    @Test
    @DisplayName("测试自定义映射器注册和获取")
    void testOverride() {
        CommonCopyMapper<Activity, ActivityDto> mapper = repository.getMapper(Activity.class, ActivityDto.class);
        Assertions.assertThat(mapper)
                .isNotNull()
                .isInstanceOf(CustomerMapper.class);
    }

    /**
     * Activity 到 ActivityDto 应应用自定义 convert 逻辑。
     */
    @Test
    @DisplayName("测试自定义转换方法 - Activity到ActivityDto")
    void testCustomer() {
        CommonCopyMapper<Activity, ActivityDto> mapper = repository.getMapper(Activity.class, ActivityDto.class);
        Activity source = new Activity("a1");
        ActivityDto dto = mapper.map(source);
        Assertions.assertThat(dto.getName()).isEqualTo("a1Customer");
    }

    /**
     * ActivityDto 到 Activity 反向映射应应用自定义 convert 逻辑。
     */
    @Test
    @DisplayName("测试反向映射 - ActivityDto到Activity")
    void testRevise() {
        CommonCopyMapper<ActivityDto, Activity> mapper = repository.getMapper(ActivityDto.class, Activity.class);
        ActivityDto source = new ActivityDto("a1");
        Activity dto = mapper.map(source);
        Assertions.assertThat(dto.getName()).isEqualTo("a1Customer");
    }
}
