package io.github.opensabe.common.location.observation;

import io.micrometer.observation.Observation;
import lombok.*;

/**
 * @author changhongwei
 * @date 2025/1/21 16:06
 * @description:
 */
@Getter
@Setter
@NoArgsConstructor
public class LocationContext extends Observation.Context  {

    // 方法名称
    private String methodName;

    // 请求参数
    private Object requestParams;

    // 响应结果
    private Object response;

    // 执行时间（毫秒）
    private long executionTime;

    private boolean setSuccessful ;
    private Throwable throwable;

    public LocationContext(String methodName, Object requestParams, Object response, long executionTime, Throwable throwable) {
        this.methodName = methodName;
        this.requestParams = requestParams;
        this.response = response;
        this.executionTime = executionTime;
        this.throwable = throwable;
    }
}