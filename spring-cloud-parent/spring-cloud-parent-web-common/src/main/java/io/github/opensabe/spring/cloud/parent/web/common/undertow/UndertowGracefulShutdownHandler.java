package io.github.opensabe.spring.cloud.parent.web.common.undertow;

import org.springframework.core.Ordered;

/**
 * 优雅关闭处理，在所有 servlet 请求处理完后，调用的
 */
public interface UndertowGracefulShutdownHandler extends Ordered {
    void gracefullyShutdown();
}
