package io.github.opensabe.base.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.opensabe.base.code.BizCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.function.BiFunction;

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

    //考虑这里是否要加JsonIgnore
    private String innerMsg;            // response msg in systematic level

    @Schema(example = "success")
    private String message;             // response user msg

    private T data;                     // response content data

    @JsonIgnore
    public boolean isSuccess () {
        return Objects.equals(bizCode, BizCodeEnum.SUCCESS.getVal());
    }

    @JsonIgnore
    public T resolveData (BiFunction<Integer, String, RuntimeException> supplier) {
        if (isSuccess()) {
            return data;
        }
        throw supplier.apply(bizCode, message);
    }
}