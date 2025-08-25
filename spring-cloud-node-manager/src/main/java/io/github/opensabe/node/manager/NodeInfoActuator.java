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
package io.github.opensabe.node.manager;

import io.github.opensabe.base.RespUtil;
import io.github.opensabe.base.vo.BaseRsp;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Log4j2
@Endpoint(id = NodeInfoActuator.PATH)
public class NodeInfoActuator {
    public static final String PATH = "node-id";

    private final NodeManager nodeManager;

    public NodeInfoActuator(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @ReadOperation
    public BaseRsp<Integer> getNodeId() {
        log.info("NodeInfoActuator-getNodeId {}", nodeManager.getNodeId());
        return RespUtil.succ(nodeManager.getNodeId());
    }
}