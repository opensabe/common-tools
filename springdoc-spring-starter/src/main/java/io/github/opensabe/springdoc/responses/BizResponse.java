package io.github.opensabe.springdoc.responses;


import io.github.opensabe.spring.cloud.parent.common.handler.ErrorMessage;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.*;

/**
 *
 * <pre><code class="java">
 *  &#64;BizResponse(condition = "返回结果Base64加密", value = PactolusErrorCode.class, useReturnType = true)
 *  &#64;GetMapping(value = "/register/device", produces = MediaType.TEXT_PLAIN_VALUE)
 *  public String registerDevice () {
 *      return Base64.encode(JsonUtil.toJSONBytes(RespUtil.success("密钥")));
 *  }
 * </code></pre>
 * 等价于
 * <pre><code class="java">
 *     &#64;ApiResponse(description = "返回结果Base64加密", responseCode = "10000", content = Content(schemaProperties = {
 *     &#64;SchemaProperty(name = "bizCode", schema = @Schema(example = "10000", type = "int")),
 *     &#64;SchemaProperty(name = "message", schema = @Schema(example = "success"))}))
 * </code></pre>
 * @author heng.ma
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(BizResponses.class)
@ApiResponse(description = "请求成功", responseCode = "10000", useReturnTypeSchema = true)
public @interface BizResponse {

    /**
     *  返回的ErrorMessage,将来开源改为 Class<? extends ErrorMessage>
     */
    Class<? extends ErrorMessage> value();

    /**
     * 排除掉的bizCode
     */
    int[] exclude() default {};

    /**
     * 指定的枚举值
     */
    int[] include() default {};

    /**
     * 是否使用Controller方法返回的类型作为文档
     * @see ApiResponse#useReturnTypeSchema()
     */
    boolean useReturnType() default false;

    /**
     * 什么情况下返回 value() 对应的枚举值
     * @see ApiResponse#description()
     */
    String condition() default "";


}
