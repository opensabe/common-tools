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
package io.github.opensabe.spring.cloud.parent.common.handler;

/**
 * ErrorCode 枚举父类
 * @author maheng
 */
@SuppressWarnings("unused")
public interface ErrorMessage {

    /**
     * 错误编码
     * @return success 10000
     */
    int code ();

    /**
     * 错误信息
     * @return  错误信息
     */
    String message ();

    /**
     * 有些错误提示需要返回数据
     * @return  BaseRsp中的data
     */
    default Object data () {
        return null;
    };

}
