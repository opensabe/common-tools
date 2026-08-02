/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.web.config.base;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.base.vo.BaseRsp;
import io.github.opensabe.common.web.config.exception.RESTFull2xxBaseException;
import io.github.opensabe.common.web.config.exception.RESTFull4xxBaseException;
import io.github.opensabe.common.web.config.exception.RESTFullBaseException;
import lombok.extern.log4j.Log4j2;

/**
 * REST 异常与错误响应工具类。
 * <p>
 * 负责将 {@link ErrResponse} 序列化为 JSON、解析业务异常消息中的嵌套 JSON，
 * 以及根据异常类型设置 {@link BaseRsp} 业务码。
 */
@Log4j2
public class ErrorUtil {

    /**
     * {@link ErrResponse} JSON 中 data 字段名。
     */
    private static final String REST_DATA_NAME = "data";

    /**
     * 将错误消息写入 {@link ErrResponse} 并序列化为 JSON 字符串。
     *
     * @param message 错误消息
     * @param error   错误响应体
     * @return JSON 字符串；序列化失败时退回原始 message
     */
    public static String appendError(String message, ErrResponse error) {
        error.setMessage(message);
        ObjectMapper mapper = JacksonUtil.createMapper();
        String jsonString = message;
        try {
            jsonString = mapper.writeValueAsString(error);
        } catch (Throwable e) {
            log.error("exceptionWithErrorObjectTranslateFail,{}", e);
        }
        return jsonString;
    }

    /**
     * 拼接异常调试信息：消息、类型、cause 与堆栈。
     *
     * @param e 异常
     * @return 调试字符串
     */
    public static String getDebug(Exception e) {
        String cause = e.getCause() == null ? "" : e.getCause().toString() + ";";
        return e.getMessage() + ";" + e.getClass().toString() + ";" + cause
                + stackTraceElementFormat(e.getStackTrace());
    }

    /**
     * 格式化堆栈元素为逗号分隔的多行字符串。
     *
     * @param elements 堆栈元素数组
     * @return 格式化结果；无堆栈时返回 {@code null}
     */
    private static String stackTraceElementFormat(StackTraceElement[] elements) {
        StringBuilder sb = new StringBuilder();
        if (elements == null || elements.length == 0) {
            return null;
        }
        for (StackTraceElement element : elements) {
            sb.append(",\n ");
            sb.append(element);
        }
        return sb.toString();
    }

    /**
     * 解析 REST 业务异常消息：若 message 为含 {@code data} 字段的 JSON，则提取内层 message 与 data。
     *
     * @param builder       响应构建器
     * @param restException REST 基础异常
     */
    public static void handleBizExceptionMsg(final BaseRsp.BaseRspBuilder<Object> builder, RESTFullBaseException restException) {
        ObjectMapper mapper = JacksonUtil.createMapper();
        String msg = restException.getMessage();
        JsonNode errorJsonNode = null;
        try {
            errorJsonNode = mapper.readTree(msg);
        } catch (Throwable e) {
            // not return json string
        }
        if (errorJsonNode != null && errorJsonNode.get(REST_DATA_NAME) != null) {
            JsonNode innerMessage = errorJsonNode.get("message");
            if (innerMessage == null) {
                builder.message("");
            } else {
                builder.message(innerMessage.textValue());
            }
            JsonNode errorDate = errorJsonNode.get("data");
            builder.data(errorDate);
            return;
        }
        if (msg != null) {
            builder.message(msg);
            return;
        }
        builder.message("");
    }

    /**
     * 根据 REST 异常类型设置 {@link BaseRsp} 业务码并记录日志。
     *
     * @param uri     请求 URI
     * @param method  HTTP 方法
     * @param builder 响应构建器
     * @param bce     REST 基础异常
     */
    public static void handleBizCode(String uri, String method, BaseRsp.BaseRspBuilder<Object> builder, RESTFullBaseException bce) {
        if (bce instanceof RESTFull2xxBaseException) {
            builder.bizCode(bce.getCode() == null ? BizCodeEnum.SUCCESS.getVal() : bce.getCode());
            return;
        }
        if (bce instanceof RESTFull4xxBaseException) {
            log.info("URI:[{}], method:[{}], RESTFull 4xx: exception:{}", uri, method, bce);
            builder.bizCode(bce.getCode() == null ? BizCodeEnum.INVALID.getVal() : bce.getCode());
            return;
        }
        builder.bizCode(BizCodeEnum.ERROR.getVal());
        log.error("URI:[{}], method:[{}], RESTFull base exception:", uri, method, bce);
    }
}
