package io.github.opensabe.common.utils.mapstruct.vo;

import io.github.opensabe.mapstruct.core.Binding;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Binding(value = Node.class, cycle = true)
public class NodeDto {

    private String name;

    private NodeDto parent;
    private NodeDto parent1;

    private List<NodeDto> children;
}
