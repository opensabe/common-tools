package io.github.opensabe.spring.cloud.parent.gateway.filter;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 某些时候前端发的某些请求不规范，不符合 HTTP URL 编码，在这里对其修正
 */
@Log4j2
@Component
public class QueryNormalizationFilter extends AbstractTracedFilter {
    @Override
    @SneakyThrows
    protected Mono<Void> traced(ServerWebExchange exchange, GatewayFilterChain chain) {
        String originUriString = exchange.getRequest().getURI().toString();
        if (originUriString.contains("%23")) {
            //将编码后的 %23 替换为 #，重新用这个字符串生成 URI
            URI replaced = new URI(originUriString.replace("%23", "#"));
            return chain.filter(
                    exchange.mutate()
                            .request(
                                    new ServerHttpRequestDecorator(exchange.getRequest()) {
                                        /**
                                         * 这个是影响转发到后台服务的 uri
                                         *
                                         * @return
                                         */
                                        @Override
                                        public URI getURI() {
                                            return replaced;
                                        }

                                        /**
                                         * 修改这个主要为了后面的 Filter 获取查询参数是准确的
                                         *
                                         * @return
                                         */
                                        @Override
                                        public MultiValueMap<String, String> getQueryParams() {
                                            return UriComponentsBuilder.fromUri(replaced).build().getQueryParams();
                                        }
                                    }
                            ).build()
            );
        } else {
            return chain.filter(exchange);
        }
    }

    @Override
    protected int ordered() {
        return TraceIdFilter.ORDER + 1;
    }
}
