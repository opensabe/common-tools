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
package io.github.opensabe.base.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.opensabe.base.code.BizCodeEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("基础响应对象测试")
class BaseRspTest {

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        BaseRsp<String> response = new BaseRsp<>();
        assertNotNull(response);
        assertEquals(0, response.getBizCode());
        assertNull(response.getInnerMsg());
        assertNull(response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试Builder模式构建对象")
    void testBuilder() {
        BaseRsp<String> response = BaseRsp.<String>builder()
                .bizCode(10000)
                .innerMsg("内部消息")
                .message("用户消息")
                .data("测试数据")
                .build();

        assertEquals(10000, response.getBizCode());
        assertEquals("内部消息", response.getInnerMsg());
        assertEquals("用户消息", response.getMessage());
        assertEquals("测试数据", response.getData());
    }

    @Test
    @DisplayName("测试成功状态判断")
    void testIsSuccess() {
        // 成功情况
        BaseRsp<String> successResponse = new BaseRsp<>();
        successResponse.setBizCode(BizCodeEnum.SUCCESS.getVal());
        assertTrue(successResponse.isSuccess());

        // 失败情况
        BaseRsp<String> failResponse = new BaseRsp<>();
        failResponse.setBizCode(BizCodeEnum.FAIL.getVal());
        assertFalse(failResponse.isSuccess());
    }

    @Test
    @DisplayName("测试数据解析功能 - 成功和失败场景")
    void testResolveData() {
        // 成功情况
        BaseRsp<String> successResponse = new BaseRsp<>();
        successResponse.setBizCode(BizCodeEnum.SUCCESS.getVal());
        successResponse.setData("测试数据");

        String result = successResponse.resolveData((code, msg) -> new RuntimeException("不应该抛出异常"));
        assertEquals("测试数据", result);

        // 失败情况
        BaseRsp<String> failResponse = new BaseRsp<>();
        failResponse.setBizCode(BizCodeEnum.FAIL.getVal());
        failResponse.setMessage("失败消息");

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                failResponse.resolveData((code, msg) -> new RuntimeException("业务失败: " + msg))
        );
        assertTrue(exception.getMessage().contains("业务失败"));
    }

    @Test
    @DisplayName("测试equals和hashCode方法")
    void testEqualsAndHashCode() {
        BaseRsp<String> response1 = BaseRsp.<String>builder()
                .bizCode(10000)
                .innerMsg("内部消息")
                .message("用户消息")
                .data("测试数据")
                .build();

        BaseRsp<String> response2 = BaseRsp.<String>builder()
                .bizCode(10000)
                .innerMsg("内部消息")
                .message("用户消息")
                .data("测试数据")
                .build();

        BaseRsp<String> response3 = BaseRsp.<String>builder()
                .bizCode(10001)
                .innerMsg("不同消息")
                .message("不同用户消息")
                .data("不同数据")
                .build();

        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        BaseRsp<String> response = BaseRsp.<String>builder()
                .bizCode(10000)
                .innerMsg("内部消息")
                .message("用户消息")
                .data("测试数据")
                .build();

        String toString = response.toString();
        assertTrue(toString.contains("10000"));
        assertTrue(toString.contains("内部消息"));
        assertTrue(toString.contains("用户消息"));
        assertTrue(toString.contains("测试数据"));
    }
} 