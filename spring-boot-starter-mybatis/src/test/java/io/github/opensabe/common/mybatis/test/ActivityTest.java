package io.github.opensabe.common.mybatis.test;

import com.alibaba.fastjson.JSONObject;
import io.github.opensabe.common.mybatis.test.mapper.user.ActivityMapper;
import io.github.opensabe.common.mybatis.test.po.Activity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ActivityTest extends BaseDataSourceTest{
    @Autowired
    private ActivityMapper activityMapper;

    @Test
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
