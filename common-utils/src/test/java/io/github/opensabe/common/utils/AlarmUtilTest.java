package io.github.opensabe.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

@DisplayName("告警工具类测试")
public class AlarmUtilTest {

    @Test
    @DisplayName("测试Set转字符串功能")
    public void toStringSet() {
        System.out.println(Set.of("rd", "op").toString());
    }
}
