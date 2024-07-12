package io.github.opensabe.spring.boot.starter.socketio;

import io.github.opensabe.base.code.BizCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseAck<T> {
    /**
     * BizCode
     * @see BizCodeEnum
     */
    private Integer b;
    /**
     * msg
     */
    private String m;
    /**
     * payLoad
     */
    private T d;
    /**
     * flag
     * 请求标识,前端传入，后端直接返回，前端根据此标识找到对应的异步响应
     */
    private String f;
}
