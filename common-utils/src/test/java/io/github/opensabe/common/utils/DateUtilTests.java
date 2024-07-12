package io.github.opensabe.common.utils;

import java.text.ParseException;

import io.github.opensabe.common.utils.DateUtil;
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