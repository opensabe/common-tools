package io.github.opensabe.spring.cloud.parent.common.validation;

import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.spring.cloud.parent.common.handler.BackendException;
import io.github.opensabe.spring.cloud.parent.common.handler.ErrorMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 管理后台校验参数，不考虑国际化
 * @author maheng
 */
@SuppressWarnings("unused")
public final class BackendCheckUtils {

    /**
     * 判断表达式必须为true
     * @param expression    要判断的表达式
     * @param message       如果为false,抛出的异常信息
     * @throws BackendException 如果表达式为false
     */
    @Contract("false, _ -> fail")
    public static <T extends ErrorMessage> void isTrue (boolean expression, T message) {
        if (!expression) {
            throw new BackendException(message);
        }
    }

    /**
     * 判断表达式必须为true
     * @param expression    要判断的表达式
     * @param message       如果为false,抛出的异常信息
     * @throws BackendException 如果表达式为false
     */
    @Contract("false, _ -> fail")
    public static void isTrue (boolean expression, String message) {
        if (!expression) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message);
        }
    }

    /**
     * 判断对象不能为空
     * @see Objects#isNull(Object) 
     * @param src           要判断的对象
     * @param message       如果对象为空,抛出的异常信息
     * @throws BackendException 如果对象为空
     */
    @Contract("null, _ -> fail")
    public static void notNull (Object src, ErrorMessage message) {
        if (Objects.isNull(src)) {
            throw new BackendException(message);
        }
    }

    /**
     * 判断对象不能为空
     * @see Objects#isNull(Object) 
     * @param src           要判断的对象
     * @param message       如果对象为空,抛出的异常信息
     * @throws BackendException 如果对象为空
     */
    @Contract("null, _ -> fail")
    public static void notNull (Object src, String message) {
        if (Objects.isNull(src)) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message);
        }
    }

    /**
     * 判断集合不能为空
     * @see CollectionUtils#isEmpty(Collection) 
     * @param collection    要判断的集合
     * @param message       如果集合为空,抛出的异常信息
     * @throws BackendException 如果集合为空
     */
    @Contract("null, _ -> fail")
    public static <T extends ErrorMessage> void notNull (Collection<?> collection, T message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BackendException(message);
        }
    }

    /**
     * 判断对象不能为空
     * @see CollectionUtils#isEmpty(Collection) 
     * @param collection           要判断的对象
     * @param message       如果对象为空,抛出的异常信息
     * @throws BackendException 如果对象为空
     */
    @Contract("null, _ -> fail")
    public static void notNull (Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message);
        }
    }

    /**
     * 判断map不能为空
     * @see MapUtils#isEmpty(Map) 
     * @param map           要判断的map
     * @param message       如果map为空,抛出的异常信息
     * @throws BackendException 如果map为空
     */
    @Contract("null, _ -> fail")
    public static <T extends ErrorMessage> void notNull (Map<?, ?> map, T message) {
        if (MapUtils.isEmpty(map)) {
            throw new BackendException(message);
        }
    }

    /**
     * 判断map不能为空
     * @see MapUtils#isEmpty(Map)
     * @param map           要判断的map
     * @param message       如果map为空,抛出的异常信息
     * @throws BackendException 如果map为空
     */
    @Contract("null, _ -> fail")
    public static void notNull (Map<?, ?> map, String message) {
        if (MapUtils.isEmpty(map)) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message);
        }
    }

    /**
     * 判断字符串不能为空
     * @see StringUtils#isBlank(CharSequence)
     * @param src           要判断的字符串
     * @param message       如果字符串为空,抛出的异常信息
     * @throws BackendException 如果字符串为空
     */
    @Contract("null, _ -> fail")
    public static <T extends ErrorMessage> void notNull (String src, T message) {
        if (StringUtils.isBlank(src)) {
            throw new BackendException(message);
        }
    }

    /**
     * 判断字符串不能为空
     * @see StringUtils#isBlank(CharSequence)
     * @param src           要判断的字符串
     * @param message       如果字符串为空,抛出的异常信息
     * @throws BackendException 如果字符串为空
     */
    @Contract("null, _ -> fail")
    public static void notNull (String src, String message) {
        if (StringUtils.isBlank(src)) {
            throw new BackendException(BizCodeEnum.INVALID.getVal(), message);
        }
    }
}
