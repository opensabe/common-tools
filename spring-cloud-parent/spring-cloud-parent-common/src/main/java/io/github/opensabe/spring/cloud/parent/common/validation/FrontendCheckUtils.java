package io.github.opensabe.spring.cloud.parent.common.validation;

import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.spring.cloud.parent.common.handler.ErrorMessage;
import io.github.opensabe.spring.cloud.parent.common.handler.FrontendException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class FrontendCheckUtils {

    /**
     * 判断表达式必须为true
     * @param expression    要判断的表达式
     * @param message       返回前端的message
     * @param args          message国际化所需的占位符参数
     * @throws FrontendException 如果表达式为false
     */
    @Contract("false, _, _ -> fail")
    public static <T extends ErrorMessage> void isTrue (boolean expression, T message, Object ... args) {
        if (!expression) {
            throw new FrontendException(message, args);
        }
    }

    /**
     * 判断表达式必须为true
     * @param expression    要判断的表达式
     * @param message       返回前端的message
     * @param args          message国际化所需的占位符参数
     * @param innerMessage  后台打印的日志信息
     * @throws FrontendException 如果表达式为false
     */
    @Contract("false, _, _, _ -> fail")
    public static <T extends ErrorMessage> void isTrue (boolean expression, T message, String innerMessage, Object ... args) {
        if (!expression) {
            throw new FrontendException(message, innerMessage, args);
        }
    }

    /**
     * 判断表达式必须为true
     * @param expression    要判断的表达式
     * @param message       返回前端的message
     * @throws FrontendException 如果表达式为false
     */
    @Contract("false, _, _ -> fail")
    public static void isTrue (boolean expression, String message, Object ... args) {
        if (!expression) {
            throw new FrontendException(BizCodeEnum.INVALID.getVal(), message, args);
        }
    }

    /**
     * 判断表达式必须为true
     * @param expression    表达式
     * @param message       返回前端的message
     * @param innerMessage  后台打印的日志信息
     * @param args          message 国际化所需占位符参数
     */
    @Contract("false, _, _, _ -> fail")
    public static void isTrue (boolean expression, String message, String innerMessage, Object ... args) {
        if (!expression) {
            throw new FrontendException(BizCodeEnum.INVALID.getVal(), message, innerMessage, args);
        }
    }

    /**
     * 判断对象不能为空
     * @see Objects#isNull(Object)
     * @param src           要判断的对象
     * @param message       返回前端的message
     * @throws FrontendException 如果对象为空
     */
    @Contract("null, _, _ -> fail")
    public static <T extends ErrorMessage> void notNull (Object src, T message, Object ... args) {
        if (Objects.isNull(src)) {
            throw new FrontendException(message, args);
        }
    }
    @Contract("null, _, _, _ -> fail")
    public static <T extends ErrorMessage> void notNull (Object src, T message, String innerMessage, Object ... args) {
        if (Objects.isNull(src)) {
            throw new FrontendException(message, innerMessage, args);
        }
    }

    /**
     * 判断对象不能为空
     * @see Objects#isNull(Object)
     * @param src           要判断的对象
     * @param message       返回前端的message
     * @throws FrontendException 如果对象为空
     */
    @Contract("null, _, _ -> fail")
    public static void notNull (Object src, String message, Object ... args) {
        if (Objects.isNull(src)) {
            throw new FrontendException(BizCodeEnum.INVALID.getVal(), message, args);
        }
    }
    @Contract("null, _, _, _ -> fail")
    public static void notNull (Object src, String message, String innerMessage, Object ... args) {
        if (Objects.isNull(src)) {
            throw new FrontendException(BizCodeEnum.INVALID.getVal(), message, innerMessage, args);
        }
    }

    /**
     * 判断集合不能为空
     * @see CollectionUtils#isEmpty(Collection)
     * @param collection    要判断的集合
     * @param message       返回前端的message
     * @throws FrontendException 如果集合为空
     */
    @Contract("null, _, _ -> fail")
    public static <T extends ErrorMessage> void notNull (Collection<?> collection, T message, Object ... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new FrontendException(message, args);
        }
    }
    @Contract("null, _, _, _ -> fail")
    public static <T extends ErrorMessage> void notNull (Collection<?> collection, T message, String innerMessage, Object ... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new FrontendException(message, innerMessage, args);
        }
    }

    /**
     * 判断对象不能为空
     * @see CollectionUtils#isEmpty(Collection)
     * @param collection           要判断的对象
     * @param message       返回前端的message
     * @throws FrontendException 如果对象为空
     */
    @Contract("null, _ , _-> fail")
    public static void notNull (Collection<?> collection, String message, Object ... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new FrontendException(BizCodeEnum.INVALID.getVal(), message, args);
        }
    }
    @Contract("null, _, _, _ -> fail")
    public static void notNull (Collection<?> collection, String message, String innerMessage, Object ... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new FrontendException(BizCodeEnum.INVALID.getVal(), message, innerMessage, args);
        }
    }

    /**
     * 判断map不能为空
     * @see MapUtils#isEmpty(Map)
     * @param map           要判断的map
     * @param message       返回前端的message
     * @throws FrontendException 如果map为空
     */
    @Contract("null, _, _ -> fail")
    public static <T extends ErrorMessage> void notNull (Map<?, ?> map, T message, Object ... args) {
        if (MapUtils.isEmpty(map)) {
            throw new FrontendException(message, args);
        }
    }
    @Contract("null, _, _, _ -> fail")
    public static <T extends ErrorMessage> void notNull (Map<?, ?> map, T message, String innerMessage, Object ... args) {
        if (MapUtils.isEmpty(map)) {
            throw new FrontendException(message, innerMessage, args);
        }
    }

    /**
     * 判断map不能为空
     * @see MapUtils#isEmpty(Map)
     * @param map           要判断的map
     * @param message       返回前端的message
     * @throws FrontendException 如果map为空
     */
    @Contract("null, _, _ -> fail")
    public static void notNull (Map<?, ?> map, String message, Object ... args) {
        if (MapUtils.isEmpty(map)) {
            throw new FrontendException(BizCodeEnum.INVALID.getVal(), message, args);
        }
    }
    @Contract("null, _, _, _ -> fail")
    public static void notNull (Map<?, ?> map, String message, String innerMessage, Object ... args) {
        if (MapUtils.isEmpty(map)) {
            throw new FrontendException(BizCodeEnum.INVALID.getVal(), message, innerMessage, args);
        }
    }

    /**
     * 判断字符串不能为空
     * @see StringUtils#isBlank(CharSequence)
     * @param src           要判断的字符串
     * @param message       返回前端的message
     * @throws FrontendException 如果字符串为空
     */
    @Contract("null, _, _ -> fail")
    public static <T extends ErrorMessage> void notNull (String src, T message, Object ... args) {
        if (StringUtils.isBlank(src)) {
            throw new FrontendException(message, args);
        }
    }
    @Contract("null, _, _, _ -> fail")
    public static <T extends ErrorMessage> void notNull (String src, T message, String innerMessage, Object ... args) {
        if (StringUtils.isBlank(src)) {
            throw new FrontendException(message, innerMessage, args);
        }
    }

    /**
     * 判断字符串不能为空
     * @see StringUtils#isBlank(CharSequence)
     * @param src           要判断的字符串
     * @param message       返回前端的message
     * @throws FrontendException 如果字符串为空
     */
    @Contract("null, _ , _-> fail")
    public static void notNull (String src, String message, Object ... args) {
        if (StringUtils.isBlank(src)) {
            throw new FrontendException(BizCodeEnum.INVALID.getVal(), message, args);
        }
    }
    @Contract("null, _, _, _ -> fail")
    public static void notNull (String src, String message, String innerMessage, Object ... args) {
        if (StringUtils.isBlank(src)) {
            throw new FrontendException(BizCodeEnum.INVALID.getVal(), message, innerMessage, args);
        }
    }
}
