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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("响应工具类测试")
class RespUtilTest {

    @Test
    @DisplayName("测试成功响应 - 无数据")
    void testSucc() {
        BaseRsp<Void> response = RespUtil.succ();
        assertEquals(BizCodeEnum.SUCCESS.getVal(), response.getBizCode());
        assertEquals(BizCodeEnum.SUCCESS.getDefaultMsg(), response.getInnerMsg());
        assertEquals(BizCodeEnum.SUCCESS.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试成功响应 - 带数据")
    void testSuccWithData() {
        String testData = "测试数据";
        BaseRsp<String> response = RespUtil.succ(testData);
        assertEquals(BizCodeEnum.SUCCESS.getVal(), response.getBizCode());
        assertEquals(testData, response.getInnerMsg());
        assertEquals(BizCodeEnum.SUCCESS.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试成功响应 - 带数据到data字段")
    void testSuccess() {
        String testData = "测试数据";
        BaseRsp<String> response = RespUtil.success(testData);
        assertEquals(BizCodeEnum.SUCCESS.getVal(), response.getBizCode());
        assertEquals(BizCodeEnum.SUCCESS.getDefaultMsg(), response.getInnerMsg());
        assertEquals(BizCodeEnum.SUCCESS.getDefaultMsg(), response.getMessage());
        assertEquals(testData, response.getData());
    }

    @Test
    @DisplayName("测试成功响应 - 带消息")
    void testSuccWithMessage() {
        String succMsg = "成功消息";
        BaseRsp<Void> response = RespUtil.succ(succMsg);
        assertEquals(BizCodeEnum.SUCCESS.getVal(), response.getBizCode());
        assertEquals(succMsg, response.getInnerMsg());
        assertEquals(BizCodeEnum.SUCCESS.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试成功响应 - 带内部消息和用户消息")
    void testSuccWithInnerAndUserMessage() {
        String innerMsg = "内部成功消息";
        String userMsg = "用户成功消息";
        BaseRsp<Void> response = RespUtil.succ(innerMsg, userMsg);
        assertEquals(BizCodeEnum.SUCCESS.getVal(), response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试失败响应 - 带消息")
    void testFail() {
        String failMsg = "失败消息";
        BaseRsp<Void> response = RespUtil.fail(failMsg);
        assertEquals(BizCodeEnum.FAIL.getVal(), response.getBizCode());
        assertEquals(failMsg, response.getInnerMsg());
        assertEquals(BizCodeEnum.FAIL.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试失败响应 - 带内部消息和用户消息")
    void testFailWithInnerAndUserMessage() {
        String innerMsg = "内部失败消息";
        String userMsg = "用户失败消息";
        BaseRsp<Void> response = RespUtil.fail(innerMsg, userMsg);
        assertEquals(BizCodeEnum.FAIL.getVal(), response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试失败响应 - 带业务码")
    void testFailWithBizCode() {
        int bizCode = 10001;
        String innerMsg = "内部失败消息";
        String userMsg = "用户失败消息";
        BaseRsp<Void> response = RespUtil.fail(bizCode, innerMsg, userMsg);
        assertEquals(bizCode, response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试无效响应 - 带消息")
    void testInvalid() {
        String invalidMsg = "无效消息";
        BaseRsp<Void> response = RespUtil.invalid(invalidMsg);
        assertEquals(BizCodeEnum.INVALID.getVal(), response.getBizCode());
        assertEquals(invalidMsg, response.getInnerMsg());
        assertEquals(BizCodeEnum.INVALID.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试无效响应 - 带内部消息和用户消息")
    void testInvalidWithInnerAndUserMessage() {
        String innerMsg = "内部无效消息";
        String userMsg = "用户无效消息";
        BaseRsp<Void> response = RespUtil.invalid(innerMsg, userMsg);
        assertEquals(BizCodeEnum.INVALID.getVal(), response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试资源未找到响应 - 带消息")
    void testResNotFound() {
        String notFoundMsg = "资源未找到消息";
        BaseRsp<Void> response = RespUtil.resNotFound(notFoundMsg);
        assertEquals(BizCodeEnum.RESOURCE_NOT_FOUND.getVal(), response.getBizCode());
        assertEquals(notFoundMsg, response.getInnerMsg());
        assertEquals(BizCodeEnum.RESOURCE_NOT_FOUND.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试资源未找到响应 - 带内部消息和用户消息")
    void testResNotFoundWithInnerAndUserMessage() {
        String innerMsg = "内部资源未找到消息";
        String userMsg = "用户资源未找到消息";
        BaseRsp<Void> response = RespUtil.resNotFound(innerMsg, userMsg);
        assertEquals(BizCodeEnum.RESOURCE_NOT_FOUND.getVal(), response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试状态错误响应 - 带消息")
    void testBadState() {
        String badStateMsg = "状态错误消息";
        BaseRsp<Void> response = RespUtil.badState(badStateMsg);
        assertEquals(BizCodeEnum.BAD_STATE.getVal(), response.getBizCode());
        assertEquals(badStateMsg, response.getInnerMsg());
        assertEquals(BizCodeEnum.BAD_STATE.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试状态错误响应 - 带内部消息和用户消息")
    void testBadStateWithInnerAndUserMessage() {
        String innerMsg = "内部状态错误消息";
        String userMsg = "用户状态错误消息";
        BaseRsp<Void> response = RespUtil.badState(innerMsg, userMsg);
        assertEquals(BizCodeEnum.BAD_STATE.getVal(), response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试系统错误响应 - 带消息")
    void testError() {
        String errorMsg = "系统错误消息";
        BaseRsp<Void> response = RespUtil.error(errorMsg);
        assertEquals(BizCodeEnum.ERROR.getVal(), response.getBizCode());
        assertEquals(errorMsg, response.getInnerMsg());
        assertEquals(BizCodeEnum.ERROR.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试系统错误响应 - 带内部消息和用户消息")
    void testErrorWithInnerAndUserMessage() {
        String innerMsg = "内部系统错误消息";
        String userMsg = "用户系统错误消息";
        BaseRsp<Void> response = RespUtil.error(innerMsg, userMsg);
        assertEquals(BizCodeEnum.ERROR.getVal(), response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }
} 