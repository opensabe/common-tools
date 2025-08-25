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
package io.github.opensabe.common.utils.mapstruct.vo;

import java.util.List;
import io.github.opensabe.mapstruct.core.Binding;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Binding(value = Node.class, cycle = true)
public class NodeDto {

    private String name;

    private NodeDto parent;
    private NodeDto parent1;

    private List<NodeDto> children;
}
