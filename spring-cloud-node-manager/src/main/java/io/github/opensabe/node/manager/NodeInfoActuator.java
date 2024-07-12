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