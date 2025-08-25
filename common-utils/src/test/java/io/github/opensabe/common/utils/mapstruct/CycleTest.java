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

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.opensabe.common.utils.mapstruct.vo.Node;
import io.github.opensabe.common.utils.mapstruct.vo.NodeDto;
import io.github.opensabe.mapstruct.core.CommonCopyMapper;
import io.github.opensabe.mapstruct.core.CycleAvoidingMappingContext;
import io.github.opensabe.mapstruct.core.FromMapMapper;
import io.github.opensabe.mapstruct.core.MapperRepository;
import io.github.opensabe.mapstruct.core.ObjectConverter;
import io.github.opensabe.mapstruct.core.SelfConvertor;
import io.github.opensabe.mapstruct.core.SelfCopyMapper;

@DisplayName("MapStruct循环引用测试")
public class CycleTest {

    private Node node;

    @BeforeEach
    void before() {
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
        node.setChildren(List.of(c1, c2));
    }

    @Test
    @DisplayName("测试DTO映射器 - 验证循环引用处理")
    void testOriginDto() {
        DtoMapper mapper = Mappers.getMapper(DtoMapper.class);
        NodeDto newNode = mapper.map(node);

        Assertions.assertEquals("current", newNode.getName());
        Assertions.assertEquals("parent", newNode.getParent().getName());
        Assertions.assertEquals("parent", newNode.getParent1().getName());
        assertThat(newNode.getChildren()).extracting(NodeDto::getName)
                .containsExactly("child1", "child2");
    }

    @Test
    @DisplayName("测试节点自映射 - 验证循环引用处理")
    void testOrigin() {

        NodeM mapper = Mappers.getMapper(NodeM.class);
        Node newNode = mapper.map(node);
        Assertions.assertEquals("current", newNode.getName());
        Assertions.assertEquals("parent", newNode.getParent().getName());
        assertThat(newNode.getChildren()).extracting(Node::getName)
                .containsExactly("child1", "child2");
    }

    @Test
    @DisplayName("测试Map到对象映射 - 验证循环引用处理")
    void testOriginMap() {
        var c1 = new Node("child1");
        var c2 = new Node("child2");
        var parent = new Node("parent");
        var map = new HashMap<String, Object>();
        map.put("name", "current");
        map.put("children", List.of(c1, c2));
        map.put("parent", parent);

        NodeMa mapper = Mappers.getMapper(NodeMa.class);
        Node node = mapper.fromMap(map);

        Assertions.assertEquals("current", node.getName());
        Assertions.assertEquals("parent", node.getParent().getName());
        assertThat(node.getChildren()).extracting(Node::getName)
                .containsExactly("child1", "child2");
    }

    @Test
    @DisplayName("测试自复制映射器 - 验证循环引用处理")
    void test() {

        SelfCopyMapper<Node> mapper = MapperRepository.getInstance().getMapper(Node.class);
        Node newNode = mapper.map(node);
        Assertions.assertEquals("current", newNode.getName());
        Assertions.assertEquals("parent", newNode.getParent().getName());
        assertThat(newNode.getChildren()).extracting(Node::getName)
                .containsExactly("child1", "child2");
    }

    @Test
    @DisplayName("测试通用复制映射器 - 验证循环引用处理")
    void testNormal() {

        CommonCopyMapper<Node, NodeDto> mapper = MapperRepository.getInstance().getMapper(Node.class, NodeDto.class);

        NodeDto newNode = mapper.map(node);

        Assertions.assertEquals("current", newNode.getName());
        Assertions.assertEquals("parent", newNode.getParent().getName());
        assertThat(newNode.getChildren()).extracting(NodeDto::getName)
                .containsExactly("child1", "child2");
    }

    @Mapper(uses = SelfConvertor.class)
    public interface NodeM extends SelfCopyMapper<Node> {


        Node map(Node node, @Context CycleAvoidingMappingContext context);

        default Node map(Node node) {
            return map(node, new CycleAvoidingMappingContext());
        }

    }

    @Mapper(uses = ObjectConverter.class, disableSubMappingMethodsGeneration = true)
    public interface NodeMa extends FromMapMapper<Node> {


    }

    @Mapper(uses = CycleAvoidingMappingContext.class)
    public interface DtoMapper {


        NodeDto map(Node node);

        Node from(NodeDto nodeDto);

    }
}
