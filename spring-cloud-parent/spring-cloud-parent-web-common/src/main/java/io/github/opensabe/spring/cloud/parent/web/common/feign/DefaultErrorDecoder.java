package io.github.opensabe.spring.cloud.parent.web.common.feign;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import io.github.opensabe.spring.cloud.parent.web.common.misc.SpecialHttpStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;

import static feign.FeignException.errorStatus;

@Log4j2
public class DefaultErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        boolean queryRequest = OpenfeignUtil.isRetryableRequest(response.request());
        boolean shouldThrowRetryable = queryRequest
                //503 是负载均衡器或者调用的服务优雅关闭的时候返回的，应该重试
                || response.status() == HttpStatus.SERVICE_UNAVAILABLE.value()
                || response.status() == SpecialHttpStatus.CIRCUIT_BREAKER_ON.getValue()
                || response.status() == SpecialHttpStatus.BULKHEAD_FULL.getValue()
                || response.status() == SpecialHttpStatus.RETRYABLE_IO_EXCEPTION.getValue();
        log.info("{} response: {}-{}, should retry: {}", methodKey, response.status(), response.reason(), shouldThrowRetryable);
        //对于查询请求以及可以重试的响应码的异常，进行重试，即抛出可重试异常 RetryableException
        if (shouldThrowRetryable) {
                Long retry = null;
                throw new RetryableException(response.status(), response.reason(), response.request().httpMethod(), retry, response.request());
        } else {
            throw errorStatus(methodKey, response);
        }
    }
}
