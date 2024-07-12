package io.github.opensabe.mapstruct;

import io.github.opensabe.mapstruct.core.Binding;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Binding(value = Student.class)
public class User {
    private String name;

    private Integer age;

    private Job job;


}
