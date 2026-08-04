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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.base.vo.BaseRsp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        String testData = "test-data";
        BaseRsp<String> response = RespUtil.succ(testData);
        assertEquals(BizCodeEnum.SUCCESS.getVal(), response.getBizCode());
        assertEquals(testData, response.getInnerMsg());
        assertEquals(BizCodeEnum.SUCCESS.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试成功响应 - 带数据到data字段")
    void testSuccess() {
        String testData = "test-data";
        BaseRsp<String> response = RespUtil.success(testData);
        assertEquals(BizCodeEnum.SUCCESS.getVal(), response.getBizCode());
        assertEquals(BizCodeEnum.SUCCESS.getDefaultMsg(), response.getInnerMsg());
        assertEquals(BizCodeEnum.SUCCESS.getDefaultMsg(), response.getMessage());
        assertEquals(testData, response.getData());
    }

    @Test
    @DisplayName("测试成功响应 - 带消息")
    void testSuccWithMessage() {
        String succMsg = "success-msg";
        BaseRsp<Void> response = RespUtil.succ(succMsg);
        assertEquals(BizCodeEnum.SUCCESS.getVal(), response.getBizCode());
        assertEquals(succMsg, response.getInnerMsg());
        assertEquals(BizCodeEnum.SUCCESS.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试成功响应 - 带内部消息和用户消息")
    void testSuccWithInnerAndUserMessage() {
        String innerMsg = "inner-success-msg";
        String userMsg = "user-success-msg";
        BaseRsp<Void> response = RespUtil.succ(innerMsg, userMsg);
        assertEquals(BizCodeEnum.SUCCESS.getVal(), response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试失败响应 - 带消息")
    void testFail() {
        String failMsg = "fail-msg";
        BaseRsp<Void> response = RespUtil.fail(failMsg);
        assertEquals(BizCodeEnum.FAIL.getVal(), response.getBizCode());
        assertEquals(failMsg, response.getInnerMsg());
        assertEquals(BizCodeEnum.FAIL.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试失败响应 - 带内部消息和用户消息")
    void testFailWithInnerAndUserMessage() {
        String innerMsg = "inner-fail-msg";
        String userMsg = "user-fail-msg";
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
        String innerMsg = "inner-fail-msg";
        String userMsg = "user-fail-msg";
        BaseRsp<Void> response = RespUtil.fail(bizCode, innerMsg, userMsg);
        assertEquals(bizCode, response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试无效响应 - 带消息")
    void testInvalid() {
        String invalidMsg = "invalid-msg";
        BaseRsp<Void> response = RespUtil.invalid(invalidMsg);
        assertEquals(BizCodeEnum.INVALID.getVal(), response.getBizCode());
        assertEquals(invalidMsg, response.getInnerMsg());
        assertEquals(BizCodeEnum.INVALID.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试无效响应 - 带内部消息和用户消息")
    void testInvalidWithInnerAndUserMessage() {
        String innerMsg = "inner-invalid-msg";
        String userMsg = "user-invalid-msg";
        BaseRsp<Void> response = RespUtil.invalid(innerMsg, userMsg);
        assertEquals(BizCodeEnum.INVALID.getVal(), response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试资源未找到响应 - 带消息")
    void testResNotFound() {
        String notFoundMsg = "not-found-msg";
        BaseRsp<Void> response = RespUtil.resNotFound(notFoundMsg);
        assertEquals(BizCodeEnum.RESOURCE_NOT_FOUND.getVal(), response.getBizCode());
        assertEquals(notFoundMsg, response.getInnerMsg());
        assertEquals(BizCodeEnum.RESOURCE_NOT_FOUND.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试资源未找到响应 - 带内部消息和用户消息")
    void testResNotFoundWithInnerAndUserMessage() {
        String innerMsg = "inner-not-found-msg";
        String userMsg = "user-not-found-msg";
        BaseRsp<Void> response = RespUtil.resNotFound(innerMsg, userMsg);
        assertEquals(BizCodeEnum.RESOURCE_NOT_FOUND.getVal(), response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试状态错误响应 - 带消息")
    void testBadState() {
        String badStateMsg = "bad-state-msg";
        BaseRsp<Void> response = RespUtil.badState(badStateMsg);
        assertEquals(BizCodeEnum.BAD_STATE.getVal(), response.getBizCode());
        assertEquals(badStateMsg, response.getInnerMsg());
        assertEquals(BizCodeEnum.BAD_STATE.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试状态错误响应 - 带内部消息和用户消息")
    void testBadStateWithInnerAndUserMessage() {
        String innerMsg = "inner-bad-state-msg";
        String userMsg = "user-bad-state-msg";
        BaseRsp<Void> response = RespUtil.badState(innerMsg, userMsg);
        assertEquals(BizCodeEnum.BAD_STATE.getVal(), response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试系统错误响应 - 带消息")
    void testError() {
        String errorMsg = "error-msg";
        BaseRsp<Void> response = RespUtil.error(errorMsg);
        assertEquals(BizCodeEnum.ERROR.getVal(), response.getBizCode());
        assertEquals(errorMsg, response.getInnerMsg());
        assertEquals(BizCodeEnum.ERROR.getDefaultMsg(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试系统错误响应 - 带内部消息和用户消息")
    void testErrorWithInnerAndUserMessage() {
        String innerMsg = "inner-error-msg";
        String userMsg = "user-error-msg";
        BaseRsp<Void> response = RespUtil.error(innerMsg, userMsg);
        assertEquals(BizCodeEnum.ERROR.getVal(), response.getBizCode());
        assertEquals(innerMsg, response.getInnerMsg());
        assertEquals(userMsg, response.getMessage());
        assertNull(response.getData());
    }
} 