package io.github.opensabe.common.utils.mapstruct.vo;

import io.github.opensabe.mapstruct.core.Binding;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Binding(cycle = true)
@NoArgsConstructor
public class Node {

    private String name;

    public Node(String name) {
        this.name = name;
    }

    private Node parent;
    private Node parent1;

    private List<Node> children;
}
