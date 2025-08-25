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
 * util for construct response data
 * @author musaxi on 2017/8/13.
 */
public class RespUtil {

    // default successful result
    public static<T> BaseRsp<T> succ() {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.SUCCESS.getVal());
        baseRsp.setInnerMsg(BizCodeEnum.SUCCESS.getDefaultMsg());
        baseRsp.setMessage(BizCodeEnum.SUCCESS.getDefaultMsg());
        return baseRsp;
    }

    public static<T> BaseRsp<T> succ(T data) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.SUCCESS.getVal());
        baseRsp.setInnerMsg(BizCodeEnum.SUCCESS.getDefaultMsg());
        baseRsp.setMessage(BizCodeEnum.SUCCESS.getDefaultMsg());
        baseRsp.setData(data);
        return baseRsp;
    }
    public static BaseRsp<String> success(String data) {
        BaseRsp<String> baseRsp = new BaseRsp<>();
        baseRsp.setBizCode(BizCodeEnum.SUCCESS.getVal());
        baseRsp.setInnerMsg(BizCodeEnum.SUCCESS.getDefaultMsg());
        baseRsp.setMessage(BizCodeEnum.SUCCESS.getDefaultMsg());
        baseRsp.setData(data);
        return baseRsp;
    }

    public static BaseRsp succ(String succMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(BizCodeEnum.SUCCESS.getVal());
        baseRsp.setInnerMsg(succMsg);
        baseRsp.setMessage(BizCodeEnum.SUCCESS.getDefaultMsg());
        return baseRsp;
    }

    public static BaseRsp succ(String succMsg, String userMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(BizCodeEnum.SUCCESS.getVal());
        baseRsp.setInnerMsg(succMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }

    // business failed, which is foreseeable (different from error)
    public static BaseRsp fail(String failMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(BizCodeEnum.FAIL.getVal());
        baseRsp.setInnerMsg(failMsg);
        baseRsp.setMessage(BizCodeEnum.FAIL.getDefaultMsg());
        return baseRsp;
    }

    public static BaseRsp fail(String failMsg, String userMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(BizCodeEnum.FAIL.getVal());
        baseRsp.setInnerMsg(failMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }

    // specific business failed, which is foreseeable and represented as unique bizCode
    public static BaseRsp fail(int bizCode, String failMsg, String userMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(bizCode);
        baseRsp.setInnerMsg(failMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }

    // request not illegal, parameter validation check or resource not found or bad state
    public static<T> BaseRsp<T> invalid(String invalidMsg) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.INVALID.getVal());
        baseRsp.setInnerMsg(invalidMsg);
        baseRsp.setMessage(BizCodeEnum.INVALID.getDefaultMsg());
        return baseRsp;
    }

    public static<T> BaseRsp<T> invalid(String invalidMsg, String userMsg) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.INVALID.getVal());
        baseRsp.setInnerMsg(invalidMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }

    // resource not found, especially for representing resource manipulation
    public static<T> BaseRsp<T> resNotFound(String resNotFoundMsg) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.RESOURCE_NOT_FOUND.getVal());
        baseRsp.setInnerMsg(resNotFoundMsg);
        baseRsp.setMessage(BizCodeEnum.RESOURCE_NOT_FOUND.getDefaultMsg());
        return baseRsp;
    }

    public static<T> BaseRsp<T> resNotFound(String resNotFoundMsg, String userMsg) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.RESOURCE_NOT_FOUND.getVal());
        baseRsp.setInnerMsg(resNotFoundMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }

    // bad state, especially for representing state illegal
    public static BaseRsp badState(String badStateMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(BizCodeEnum.BAD_STATE.getVal());
        baseRsp.setInnerMsg(badStateMsg);
        baseRsp.setMessage(BizCodeEnum.BAD_STATE.getDefaultMsg());
        return baseRsp;
    }

    public static BaseRsp badState(String badStateMsg, String userMsg) {
        BaseRsp baseRsp = new BaseRsp();
        baseRsp.setBizCode(BizCodeEnum.BAD_STATE.getVal());
        baseRsp.setInnerMsg(badStateMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }

    // systematic error, probably caused by unexpected exception captured
    public static<T> BaseRsp<T> error(String errMsg) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.ERROR.getVal());
        baseRsp.setInnerMsg(errMsg);
        baseRsp.setMessage(BizCodeEnum.ERROR.getDefaultMsg());
        return baseRsp;
    }

    public static<T> BaseRsp<T> error(String errMsg, String userMsg) {
        BaseRsp<T> baseRsp = new BaseRsp<T>();
        baseRsp.setBizCode(BizCodeEnum.ERROR.getVal());
        baseRsp.setInnerMsg(errMsg);
        baseRsp.setMessage(userMsg);
        return baseRsp;
    }
}
