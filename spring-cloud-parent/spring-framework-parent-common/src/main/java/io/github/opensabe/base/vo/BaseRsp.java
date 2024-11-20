package io.github.opensabe.base.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(example = "10000")
    private int bizCode;                // response code RspCodeEnum.val
    private String innerMsg;            // response msg in systematic level
    @Schema(example = "success")
    private String message;             // response user msg
    private T data;                     // response content data
}