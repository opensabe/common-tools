package io.github.opensabe.common.utils.mapstruct.vo;

import io.github.opensabe.mapstruct.core.Binding;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Binding(ActivityDto.class)
@Getter
@AllArgsConstructor
public class Activity {

    private String name;
}
