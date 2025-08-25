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
package io.github.opensabe.spring.cloud.parent.common.validation;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;

import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.spring.cloud.parent.common.handler.BackendException;
import io.github.opensabe.spring.cloud.parent.common.handler.ErrorMessage;

/**
 * 管理后台校验参数，不考虑国际化
 *
 * @author maheng
 */
@SuppressWarnings("unused")
public final class BackendCheckUtils {

    /**
     * 判断表达式必须为true
     *
     * @param expression 要判断的表达式
     * @param message    如果为false,抛出的异常信息
     * @param data       返回给前端的结果
     * @throws BackendException 如果表达式为false
     */
    @Contract("false, _, _ -> fail")
    public static <T extends ErrorMessage> void isTrue(boolean expression, T message, Object data) {
        if (!expression) {
            throw new BackendException(message, data);
        }
    }

    @Contract("false, _ -> fail")
    public static <T extends ErrorMessage> void isTrue(boolean expression, T message) {
        if (!expression) {
            throw new BackendException(message);
        }
    }

    @Contract("false, _ -> fail")
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message);
        }
    }

    @Contract("false, _, _ -> fail")
    public static void isTrue(boolean expression, String message, Object data) {
        if (!expression) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message, data);
        }
    }

    /**
     * 判断表达式必须为false
     *
     * @param expression 要判断的表达式
     * @param message    如果为false,抛出的异常信息
     * @param data       返回给前端的结果
     * @throws BackendException 如果表达式为true
     */
    @Contract("true, _, _ -> fail")
    public static <T extends ErrorMessage> void isFalse(boolean expression, T message, Object data) {
        if (expression) {
            throw new BackendException(message, data);
        }
    }

    @Contract("true, _ -> fail")
    public static <T extends ErrorMessage> void isFalse(boolean expression, T message) {
        if (expression) {
            throw new BackendException(message);
        }
    }

    @Contract("true, _, _ -> fail")
    public static void isFalse(boolean expression, String message, Object data) {
        if (expression) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message, data);
        }
    }


    /**
     * 判断对象不能为空
     *
     * @param src     要判断的对象
     * @param message 如果对象为空,抛出的异常信息
     * @throws BackendException 如果对象为空
     * @see Objects#isNull(Object)
     */
    @Contract("null, _ -> fail")
    public static void notNull(Object src, ErrorMessage message) {
        if (Objects.isNull(src)) {
            throw new BackendException(message);
        }
    }

    @Contract("null, _ -> fail")
    public static void notNull(Object src, String message) {
        if (Objects.isNull(src)) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message);
        }
    }

    @Contract("null, _, _ -> fail")
    public static void notNull(Object src, ErrorMessage message, Object data) {
        if (Objects.isNull(src)) {
            throw new BackendException(message, data);
        }
    }

    @Contract("null, _, _ -> fail")
    public static void notNull(Object src, String message, Object data) {
        if (Objects.isNull(src)) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message, data);
        }
    }

    /**
     * 判断集合不能为空
     *
     * @param collection 要判断的集合
     * @param message    如果集合为空,抛出的异常信息
     * @throws BackendException 如果集合为空
     * @see CollectionUtils#isEmpty(Collection)
     */
    @Contract("null, _ -> fail")
    public static <T extends ErrorMessage> void notNull(Collection<?> collection, T message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BackendException(message);
        }
    }

    @Contract("null, _ -> fail")
    public static void notNull(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message);
        }
    }

    @Contract("null, _, _ -> fail")
    public static <T extends ErrorMessage> void notNull(Collection<?> collection, T message, Object data) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BackendException(message, data);
        }
    }

    @Contract("null, _, _ -> fail")
    public static void notNull(Collection<?> collection, String message, Object data) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message, data);
        }
    }

    /**
     * 判断map不能为空
     *
     * @param map     要判断的map
     * @param message 如果map为空,抛出的异常信息
     * @throws BackendException 如果map为空
     * @see MapUtils#isEmpty(Map)
     */
    @Contract("null, _ -> fail")
    public static <T extends ErrorMessage> void notNull(Map<?, ?> map, T message) {
        if (MapUtils.isEmpty(map)) {
            throw new BackendException(message);
        }
    }

    @Contract("null, _ -> fail")
    public static void notNull(Map<?, ?> map, String message) {
        if (MapUtils.isEmpty(map)) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message);
        }
    }

    @Contract("null, _, _ -> fail")
    public static <T extends ErrorMessage> void notNull(Map<?, ?> map, T message, Object data) {
        if (MapUtils.isEmpty(map)) {
            throw new BackendException(message, data);
        }
    }

    @Contract("null, _, _ -> fail")
    public static void notNull(Map<?, ?> map, String message, Object data) {
        if (MapUtils.isEmpty(map)) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message, data);
        }
    }

    /**
     * 判断字符串不能为空
     *
     * @param src     要判断的字符串
     * @param message 如果字符串为空,抛出的异常信息
     * @throws BackendException 如果字符串为空
     * @see StringUtils#isBlank(CharSequence)
     */
    @Contract("null, _ -> fail")
    public static <T extends ErrorMessage> void notNull(String src, T message) {
        if (StringUtils.isBlank(src)) {
            throw new BackendException(message);
        }
    }

    @Contract("null, _ -> fail")
    public static void notNull(String src, String message) {
        if (StringUtils.isBlank(src)) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message);
        }
    }

    @Contract("null, _, _ -> fail")
    public static <T extends ErrorMessage> void notNull(String src, T message, Object data) {
        if (StringUtils.isBlank(src)) {
            throw new BackendException(message, data);
        }
    }

    @Contract("null, _, _ -> fail")
    public static void notNull(String src, String message, Object data) {
        if (StringUtils.isBlank(src)) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message, data);
        }
    }
}
