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
package io.github.opensabe.base.code;

import io.github.opensabe.spring.cloud.parent.common.handler.ErrorMessage;
import lombok.Getter;

/**
 * Business code in basic response
 */
@Getter
public enum BizCodeEnum implements ErrorMessage {
    /**
     * Standard response code for backend system
     */
    SUCCESS(10000, "success"),                          // business success, default successful result
    FAIL(11000, "fail"),                                // business failed, which is foreseeable (different from error)
    INVALID(19000, "invalid"),                          // request parameter not valid
    RESOURCE_NOT_FOUND(19001, "resource not found"),    // resource not found, especially for representing resource manipulation
    BAD_STATE(19002, "bad state"),                      // bad state, especially for representing state illegal
    FORBIDDEN(19003, "Forbidden"),                      // bad state, especially for representing state illegal
    ERROR(19999, "error"),                              // unexpected system error
    /**
     * Specified BizCode {SrcEnum.id + 4 digits}
     */
    ;

    private int val;
    private String defaultMsg;
    BizCodeEnum(int val, String defaultMsg) {
        this.val = val;
        this.defaultMsg = defaultMsg;
    }

    @Override
    public int code() {
        return val;
    }

    @Override
    public String message() {
        return defaultMsg;
    }
}