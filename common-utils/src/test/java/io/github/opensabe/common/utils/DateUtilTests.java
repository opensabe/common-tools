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
import org.junit.jupiter.api.Test;

class DateUtilTests {
    @Test
    void changeTimeZoneStringToStamp() throws ParseException {
        Assertions.assertEquals(DateUtil.changeTimeZoneStringToStamp("2019-03-13 15:54:00 GMT+0800"), 1552463640000L);
        Assertions.assertEquals(DateUtil.changeTimeZoneStringToStamp("2019-03-13 05:02:46 UTC"), 1552453366000L);
        Assertions.assertEquals(DateUtil.changeTimeZoneStringToStamp("2019-03-13 15:54:00 BST"), 1552470840000L);
        Assertions.assertEquals(DateUtil.changeTimeZoneStringToStamp("2019-03-13 15:54:00 Asia/Dhaka"), 1552470840000L);
    }
}