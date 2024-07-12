package io.github.opensabe.base.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Basic response format
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseRsp<T> {
    private int bizCode;                // response code RspCodeEnum.val
    private String innerMsg;            // response msg in systematic level
    private String message;             // response user msg
    private T data;                     // response content data
}