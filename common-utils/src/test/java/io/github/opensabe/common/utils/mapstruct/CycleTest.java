package io.github.opensabe.common.utils.mapstruct;

import io.github.opensabe.common.utils.mapstruct.vo.Node;
import io.github.opensabe.common.utils.mapstruct.vo.NodeDto;
import io.github.opensabe.mapstruct.core.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CycleTest {

    private Node node;

    @Mapper(uses = SelfConvertor.class)
    public interface NodeM  extends SelfCopyMapper<Node> {


        Node map (Node node, @Context CycleAvoidingMappingContext context);

        default Node map (Node node) {
            return map(node, new CycleAvoidingMappingContext());
        }

    }
    @Mapper(uses = ObjectConverter.class, disableSubMappingMethodsGeneration = true)
    public interface NodeMa  extends FromMapMapper<Node> {


    }


    @Mapper(uses = CycleAvoidingMappingContext.class)
    public interface DtoMapper {


        NodeDto map (Node node);
        Node from (NodeDto nodeDto);

    }


    @BeforeEach
    void before () {
        var c1 = new Node("child1");
        var c2 = new Node("child2");
        var p = new Node("parent");
        node = new Node("current");
        c1.setParent(node);
        c1.setParent1(node);
        c2.setParent(node);
        c2.setParent1(node);
        node.setParent(p);
        node.setParent1(p);
        node.setChildren(List.of(c1,c2));
    }

    @Test
    void testOriginDto () {
        DtoMapper mapper = Mappers.getMapper(DtoMapper.class);
        NodeDto newNode = mapper.map(node);

        Assertions.assertEquals("current", newNode.getName());
        Assertions.assertEquals("parent", newNode.getParent().getName());
        Assertions.assertEquals("parent", newNode.getParent1().getName());
        assertThat(newNode.getChildren()).extracting(NodeDto::getName)
                .containsExactly("child1", "child2");
    }

    @Test
    void testOrigin () {

        NodeM mapper = Mappers.getMapper(NodeM.class);
        Node newNode = mapper.map(node);
        Assertions.assertEquals("current", newNode.getName());
        Assertions.assertEquals("parent", newNode.getParent().getName());
        assertThat(newNode.getChildren()).extracting(Node::getName)
                .containsExactly("child1", "child2");
    }

    @Test
    void testOriginMap () {
        var c1 = new Node("child1");
        var c2 = new Node("child2");
        var parent = new Node("parent");
        var map = new HashMap<String,Object>();
        map.put("name", "current");
        map.put("children", List.of(c1,c2));
        map.put("parent", parent);

        NodeMa mapper = Mappers.getMapper(NodeMa.class);
        Node node = mapper.fromMap(map);

        Assertions.assertEquals("current", node.getName());
        Assertions.assertEquals("parent", node.getParent().getName());
        assertThat(node.getChildren()).extracting(Node::getName)
                .containsExactly("child1", "child2");
    }
    @Test
    void test () {

        SelfCopyMapper<Node> mapper = MapperRepository.getInstance().getMapper(Node.class);
        Node newNode = mapper.map(node);
        Assertions.assertEquals("current", newNode.getName());
        Assertions.assertEquals("parent", newNode.getParent().getName());
        assertThat(newNode.getChildren()).extracting(Node::getName)
                .containsExactly("child1", "child2");
    }

    @Test
    void testNormal () {

        CommonCopyMapper<Node, NodeDto> mapper = MapperRepository.getInstance().getMapper(Node.class, NodeDto.class);

        NodeDto newNode = mapper.map(node);

        Assertions.assertEquals("current", newNode.getName());
        Assertions.assertEquals("parent", newNode.getParent().getName());
        assertThat(newNode.getChildren()).extracting(NodeDto::getName)
                .containsExactly("child1", "child2");
    }
}
