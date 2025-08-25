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
package io.github.opensabe.common.mybatis.test;

import com.alibaba.fastjson.JSONObject;
import io.github.opensabe.common.mybatis.test.common.BaseMybatisTest;
import io.github.opensabe.common.mybatis.test.mapper.user.ActivityMapper;
import io.github.opensabe.common.mybatis.test.po.Activity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@DisplayName("活动实体测试")
public class ActivityTest extends BaseMybatisTest {
    @Autowired
    private ActivityMapper activityMapper;

    @Test
    @DisplayName("测试活动实体的插入和查询功能 - 验证复杂数据类型的序列化和反序列化")
    @Transactional
    public void testInsertSelect () {
        var id = "activity1";
        //insert
        var displayStr = "{\"displayInAz\":true,\"displayInList\":true,\"endTime\":1577487600000,\"image\":\"https://opensabe.com/up/a961bd01-31d3-4212-aa58.png\",\"link\":\"1\",\"startTime\":1574290800000,\"title\":\"1\"}";
        var activity = new Activity();
        activity.setActivityId(id);
        activity.setBizType(List.of(1,2,3));
        var config = new Activity.Config();
        config.setVal("test");
        var disBean = JSONObject.parseObject(displayStr, Activity.DisplaySetting.class);
        config.setDisplaySettings(List.of(disBean));
        activity.setConfigSetting(new Activity.Configs(List.of(config)));
        var displaySetting = new Activity.Display();
        displaySetting.put("wap",disBean);
        displaySetting.put("ios",disBean);
        activity.setDisplaySetting(displaySetting);
        activityMapper.insertSelective(activity);

        //select
        var db = activityMapper.selectByPrimaryKey(id);
        //simple type
        Assertions.assertIterableEquals(db.getBizType(),activity.getBizType());

        //list
        Assertions.assertEquals(db.getConfigSetting().get(0).getVal(),activity.getConfigSetting().get(0).getVal());
        Assertions.assertEquals(db.getConfigSetting().get(0).getDisplaySettings().get(0).getImage(),activity.getConfigSetting().get(0).getDisplaySettings().get(0).getImage());

        //map
        Assertions.assertEquals(db.getDisplaySetting().get("wap").getStartTime(),activity.getDisplaySetting().get("wap").getStartTime());
    }
}
