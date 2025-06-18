package io.github.opensabe.spring.cloud.parent.gateway.common;

import io.github.opensabe.base.vo.BaseRsp;
import io.github.opensabe.common.utils.json.JsonUtil;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class CommonFilterUtil {
    /**
     * 路径匹配，包括原始的 * 匹配，以及扩展的正则匹配
     */
    public static final AntPathMatcher MATCHER = new AntPathMatcher() {
        @Override
        public boolean match(String pattern, String path) {
            //判断原始 * 匹配
            var flag = super.match(pattern, path);
            //不匹配就匹配正则
            if (!flag) {
                try {
                    return path.matches(pattern);
                } catch (Exception e) {
                    return false;
                }
            }
            return flag;
        }
    };

    /**
     * 将 dataBuffer 转化为 UTF8 编码的 String
     *
     * @param dataBuffer
     * @return dataBuffer 对应的字符串
     */
    public static String dataBufferToString(DataBuffer dataBuffer) {
        byte[] content = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(content);
        //一定要 release，否则某些情况会有内存泄漏
        //例如这里使用请求的 body 作为输入的 dataBuffer，读取完了必须释放
        // 因为参与后续 Filter 的已经不是原始的 dataBuffer，无法释放了
        DataBufferUtils.release(dataBuffer);
        return new String(content, StandardCharsets.UTF_8);
    }

    public static Mono<Void> errorResponse(ServerHttpResponse response, HttpStatus httpStatus, BaseRsp baseRsp, DataBufferFactory dataBufferFactory) {
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().remove(HttpHeaders.CONTENT_LENGTH);
        return response.writeWith(Mono.just(
                dataBufferFactory.wrap(
                        JsonUtil.toJSONString(baseRsp).getBytes()
                )
        ));
    }
    public static Mono<Void> errorResponse(ServerHttpResponse response, HttpStatus httpStatus, BaseRsp baseRsp) {
        return errorResponse(response,httpStatus,baseRsp,response.bufferFactory());
    }
}
