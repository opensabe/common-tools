package io.github.opensabe.spring.cloud.parent.web.common.feign.preheating;

import org.springframework.web.bind.annotation.GetMapping;

public interface FeignPreheatingBase {
    @GetMapping("/actuator/health")
    String heartbeat();
}
