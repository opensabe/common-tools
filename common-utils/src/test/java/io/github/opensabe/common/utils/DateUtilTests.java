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
package io.github.opensabe.common.utils;

import java.text.ParseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link DateUtil} 时区字符串解析测试。
 */
@DisplayName("DateUtil 时区解析测试")
class DateUtilTests {

    /**
     * 各时区格式字符串应正确转换为 epoch 毫秒。
     */
    @Test
    @DisplayName("时区字符串转时间戳")
    void changeTimeZoneStringToStamp() throws ParseException {
        Assertions.assertEquals(DateUtil.changeTimeZoneStringToStamp("2019-03-13 15:54:00 GMT+0800"), 1552463640000L);
        Assertions.assertEquals(DateUtil.changeTimeZoneStringToStamp("2019-03-13 05:02:46 UTC"), 1552453366000L);
        Assertions.assertEquals(DateUtil.changeTimeZoneStringToStamp("2019-03-13 15:54:00 BST"), 1552470840000L);
        Assertions.assertEquals(DateUtil.changeTimeZoneStringToStamp("2019-03-13 15:54:00 Asia/Dhaka"), 1552470840000L);
    }
}
