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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.base.vo.BaseRsp;
import io.github.opensabe.common.web.config.exception.RESTFull2xxBaseException;
import io.github.opensabe.common.web.config.exception.RESTFull4xxBaseException;
import io.github.opensabe.common.web.config.exception.RESTFullBaseException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ErrorUtil {
    private static final String REST_DATA_NAME = "data";

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

    public static String getDebug(Exception e) {
        String cause = e.getCause() == null ? "" : e.getCause().toString() + ";";
        return e.getMessage() + ";" + e.getClass().toString() + ";" + cause
                + stackTraceElementFormat(e.getStackTrace());
    }

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
     * Handle Biz Exception message
     * <p>
     * Sometimes, exception has a  response entity for api data:
     *
     * @param builder       ResponseModel
     * @param restException RESTFullBaseException
     */
    public static void handleBizExceptionMsg(final BaseRsp.BaseRspBuilder<Object> builder, RESTFullBaseException restException) {
        ObjectMapper mapper = JacksonUtil.createMapper();
        String msg = restException.getMessage();
        JsonNode errorJsonNode = null;
        try {
            errorJsonNode = mapper.readTree(msg);
        } catch (Throwable e) {
            // not return  json string
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
