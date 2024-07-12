package io.github.opensabe.common.core;

import lombok.SneakyThrows;

import java.util.List;

/**
 * replace with {@link io.github.opensabe.spring.cloud.parent.common.validation.FrontendCheckUtils}
 * and {@link io.github.opensabe.spring.cloud.parent.common.validation.BackendCheckUtils}
 * @author musaxi on 2017/9/21.
 */
@Deprecated(forRemoval = true)
public final class CheckUtil {

    @SneakyThrows
    public static void checkBiz(boolean expression, ErrorCode errorCode) {
        if (!expression) {
            throw new AppException(errorCode);
        }
    }

    @SneakyThrows
    public static void checkBiz(boolean expression, ErrorCode errorCode, String causeMsg) {
        if (!expression) {
            throw new AppException(errorCode, causeMsg);
        }
    }

    @SneakyThrows
    public static void checkBiz(boolean expression, ErrorCode errorCode, String causeMsgTemplate, Object... objects) {
        if (!expression) {
            throw new AppException(errorCode, FormatUtil.format(causeMsgTemplate, objects));
        }
    }

    @SneakyThrows
    public static <T> T checkNN(T reference, String causeMsgTemplate, Object... objects) {
        if (reference == null) {
            throw new AppException(ErrorCode.INVALID, FormatUtil.format(causeMsgTemplate, objects));
        } else {
            return reference;
        }
    }

    @SneakyThrows
    public static <T> T checkNN(T reference, ErrorCode errorCode, String causeMsgTemplate, Object... objects) {
        if (reference == null) {
            throw new AppException(errorCode, FormatUtil.format(causeMsgTemplate, objects));
        } else {
            return reference;
        }
    }

    @SneakyThrows
    public static <T> T checkOnly(List<T> reference, String causeMsgTemplate, Object... objects) {
        if (reference.size() != 1) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, FormatUtil.format(causeMsgTemplate, objects));
        } else {
            return reference.get(0);
        }
    }

    @SneakyThrows
    public static void checkEmpty(List<?> reference, String causeMsgTemplate, Object... objects) {
        if (reference.size() != 0) {
            throw new AppException(ErrorCode.INVALID, FormatUtil.format(causeMsgTemplate, objects));
        }
    }
}