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
package io.github.opensabe.base;

import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.base.vo.BaseRsp;

/**
 * 统一 API 响应体 {@link BaseRsp} 的静态构造工具。
 * <p>
 * 按业务语义区分成功、可预期失败、参数非法、资源缺失、状态非法与系统错误等场景，
 * 分别映射到 {@link BizCodeEnum} 中的标准业务码。
 */
public class RespUtil {

    /**
     * 构造无载荷的成功响应，使用 {@link BizCodeEnum#SUCCESS} 默认文案。
     *
     * @param <T> 响应数据泛型
     * @return 成功响应
     */
    public static <T> BaseRsp<T> succ() {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.SUCCESS.getVal());
        baseRsp.setInnerMsg(BizCodeEnum.SUCCESS.getDefaultMsg());
        baseRsp.setMessage(BizCodeEnum.SUCCESS.getDefaultMsg());
        return baseRsp;
    }

    /**
     * 构造带载荷的成功响应。
     *
     * @param data 响应数据
     * @param <T>  响应数据泛型
     * @return 成功响应
     */
    public static <T> BaseRsp<T> succ(T data) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.SUCCESS.getVal());
        baseRsp.setInnerMsg(BizCodeEnum.SUCCESS.getDefaultMsg());
        baseRsp.setMessage(BizCodeEnum.SUCCESS.getDefaultMsg());
        baseRsp.setData(data);
        return baseRsp;
    }

    /**
     * 构造 {@code data} 为 {@link String} 的成功响应。
     *
     * @param data 字符串载荷
     * @return 成功响应
     */
    public static BaseRsp<String> success(String data) {
        BaseRsp<String> baseRsp = new BaseRsp<>();
        baseRsp.setBizCode(BizCodeEnum.SUCCESS.getVal());
        baseRsp.setInnerMsg(BizCodeEnum.SUCCESS.getDefaultMsg());
        baseRsp.setMessage(BizCodeEnum.SUCCESS.getDefaultMsg());
        baseRsp.setData(data);
        return baseRsp;
    }

    /**
     * 构造成功响应，自定义内部消息，用户可见消息沿用默认成功文案。
     *
     * @param succMsg 内部/系统级成功说明
     * @return 成功响应
     */
    public static BaseRsp succ(String succMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(BizCodeEnum.SUCCESS.getVal());
        baseRsp.setInnerMsg(succMsg);
        baseRsp.setMessage(BizCodeEnum.SUCCESS.getDefaultMsg());
        return baseRsp;
    }

    /**
     * 构造成功响应，分别指定内部消息与用户可见消息。
     *
     * @param succMsg 内部/系统级成功说明
     * @param userMsg 用户可见消息
     * @return 成功响应
     */
    public static BaseRsp succ(String succMsg, String userMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(BizCodeEnum.SUCCESS.getVal());
        baseRsp.setInnerMsg(succMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }

    /**
     * 构造可预期的业务失败响应（区别于系统 {@link #error}）。
     *
     * @param failMsg 内部失败说明
     * @return 失败响应
     */
    public static BaseRsp fail(String failMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(BizCodeEnum.FAIL.getVal());
        baseRsp.setInnerMsg(failMsg);
        baseRsp.setMessage(BizCodeEnum.FAIL.getDefaultMsg());
        return baseRsp;
    }

    /**
     * 构造可预期的业务失败响应，分别指定内部与用户可见消息。
     *
     * @param failMsg 内部失败说明
     * @param userMsg 用户可见消息
     * @return 失败响应
     */
    public static BaseRsp fail(String failMsg, String userMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(BizCodeEnum.FAIL.getVal());
        baseRsp.setInnerMsg(failMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }

    /**
     * 构造带自定义业务码的可预期失败响应。
     *
     * @param bizCode 业务码
     * @param failMsg 内部失败说明
     * @param userMsg 用户可见消息
     * @return 失败响应
     */
    public static BaseRsp fail(int bizCode, String failMsg, String userMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(bizCode);
        baseRsp.setInnerMsg(failMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }

    /**
     * 构造请求非法响应（参数校验失败等）。
     *
     * @param invalidMsg 非法原因说明
     * @param <T>        响应数据泛型
     * @return 非法响应
     */
    public static <T> BaseRsp<T> invalid(String invalidMsg) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.INVALID.getVal());
        baseRsp.setInnerMsg(invalidMsg);
        baseRsp.setMessage(BizCodeEnum.INVALID.getDefaultMsg());
        return baseRsp;
    }

    /**
     * 构造请求非法响应，分别指定内部与用户可见消息。
     *
     * @param invalidMsg 非法原因说明
     * @param userMsg    用户可见消息
     * @param <T>        响应数据泛型
     * @return 非法响应
     */
    public static <T> BaseRsp<T> invalid(String invalidMsg, String userMsg) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.INVALID.getVal());
        baseRsp.setInnerMsg(invalidMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }

    /**
     * 构造资源未找到响应。
     *
     * @param resNotFoundMsg 未找到说明
     * @param <T>            响应数据泛型
     * @return 资源未找到响应
     */
    public static <T> BaseRsp<T> resNotFound(String resNotFoundMsg) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.RESOURCE_NOT_FOUND.getVal());
        baseRsp.setInnerMsg(resNotFoundMsg);
        baseRsp.setMessage(BizCodeEnum.RESOURCE_NOT_FOUND.getDefaultMsg());
        return baseRsp;
    }

    /**
     * 构造资源未找到响应，分别指定内部与用户可见消息。
     *
     * @param resNotFoundMsg 未找到说明
     * @param userMsg        用户可见消息
     * @param <T>            响应数据泛型
     * @return 资源未找到响应
     */
    public static <T> BaseRsp<T> resNotFound(String resNotFoundMsg, String userMsg) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.RESOURCE_NOT_FOUND.getVal());
        baseRsp.setInnerMsg(resNotFoundMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }

    /**
     * 构造状态非法响应。
     *
     * @param badStateMsg 状态非法说明
     * @return 状态非法响应
     */
    public static BaseRsp badState(String badStateMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(BizCodeEnum.BAD_STATE.getVal());
        baseRsp.setInnerMsg(badStateMsg);
        baseRsp.setMessage(BizCodeEnum.BAD_STATE.getDefaultMsg());
        return baseRsp;
    }

    /**
     * 构造状态非法响应，分别指定内部与用户可见消息。
     *
     * @param badStateMsg 状态非法说明
     * @param userMsg     用户可见消息
     * @return 状态非法响应
     */
    public static BaseRsp badState(String badStateMsg, String userMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(BizCodeEnum.BAD_STATE.getVal());
        baseRsp.setInnerMsg(badStateMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }

    /**
     * 构造系统错误响应（非可预期业务失败）。
     *
     * @param errMsg 错误说明
     * @param <T>    响应数据泛型
     * @return 系统错误响应
     */
    public static <T> BaseRsp<T> error(String errMsg) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.ERROR.getVal());
        baseRsp.setInnerMsg(errMsg);
        baseRsp.setMessage(BizCodeEnum.ERROR.getDefaultMsg());
        return baseRsp;
    }

    /**
     * 构造系统错误响应，分别指定内部与用户可见消息。
     *
     * @param errMsg  错误说明
     * @param userMsg 用户可见消息
     * @param <T>     响应数据泛型
     * @return 系统错误响应
     */
    public static <T> BaseRsp<T> error(String errMsg, String userMsg) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.ERROR.getVal());
        baseRsp.setInnerMsg(errMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }
}
