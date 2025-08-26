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
package io.github.opensabe.common.web.config.exception;


import java.io.Serializable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.github.opensabe.common.web.config.base.ErrResponse;
import io.github.opensabe.common.web.config.base.ErrorUtil;

@ResponseStatus(HttpStatus.OK)
public class C200Exception extends RESTFull2xxBaseException implements Serializable {
    private static final long serialVersionUID = -2312393803704717855L;

    public C200Exception(String message) {
        super(message);
    }

    public C200Exception(int code, String message) {
        super(message, code);
    }

    public C200Exception(int code, String msg, ErrResponse err) {
        super(ErrorUtil.appendError(msg, err), code);
    }
}
