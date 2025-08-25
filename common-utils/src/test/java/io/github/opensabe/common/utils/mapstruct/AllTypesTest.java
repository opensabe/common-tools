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
package io.github.opensabe.common.utils.mapstruct;

import io.github.opensabe.common.utils.mapstruct.vo.Types;
import io.github.opensabe.common.utils.mapstruct.vo.TypesDto;
import io.github.opensabe.mapstruct.core.CommonCopyMapper;
import io.github.opensabe.mapstruct.core.FromMapMapper;
import io.github.opensabe.mapstruct.core.MapperRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@DisplayName("MapStruct所有类型映射测试")
public class AllTypesTest {

    @Test
    @DisplayName("测试类到Record的映射 - 验证所有数据类型转换")
    void testToRecord () {
        var m = System.currentTimeMillis();
        var allTypes = new Types();
        allTypes.setName("tom");
        allTypes.setAge(10);
        allTypes.setAge0(12);
        allTypes.setTime2(Instant.ofEpochMilli(m));
        allTypes.setTime1(LocalDateTime.ofInstant(allTypes.getTime2(), ZoneId.systemDefault()));
        allTypes.setTime3(new Date(m));
        allTypes.setFlag(true);
        allTypes.setFlag0(false);
        allTypes.setMoney(100.00d);
        allTypes.setMoney0(90.12d);
        allTypes.setCount(5L);
        allTypes.setCount0(20L);
        allTypes.setWeight(0.8f);
        allTypes.setWeight0(0.45f);
        allTypes.setHeight(Short.parseShort("3"));
        allTypes.setHeight0(Short.valueOf("2"));
        allTypes.setPrice(BigDecimal.valueOf(102.33));

        CommonCopyMapper<Types, TypesDto> mapper = MapperRepository.getInstance().getMapper(Types.class, TypesDto.class);

        TypesDto record = mapper.map(allTypes);
        System.out.println(record);
        Assertions.assertEquals("tom", record.name());
        Assertions.assertEquals(10, record.age());
        Assertions.assertEquals(12, record.age0());
        Assertions.assertEquals(m, record.time2().toEpochMilli());
        Assertions.assertEquals(m, record.time1().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        Assertions.assertEquals(m, record.time3().getTime());
        Assertions.assertTrue(record.flag());
        Assertions.assertFalse(record.flag0());
        Assertions.assertEquals(100.00d, record.money());
        Assertions.assertEquals(90.12d, record.money0());
        Assertions.assertEquals(5, record.count());
        Assertions.assertEquals(20, record.count0());
        Assertions.assertEquals(0.8f, record.weight());
        Assertions.assertEquals(0.45f, record.weight0());
        Assertions.assertEquals(Short.valueOf("3"), record.height());
        Assertions.assertEquals(Short.valueOf("2"), record.height0());
        Assertions.assertEquals(BigDecimal.valueOf(102.33), record.price());
    }


    @Test
    @DisplayName("测试Record到类的映射 - 验证所有数据类型转换")
    void testToClass () {
        var m = System.currentTimeMillis();
        var record = new TypesDto("tom", 40, 28,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(m), ZoneId.systemDefault()), Instant.ofEpochMilli(m), new Date(m),
                false, true, 500.32d, 100.01d, 100, 20L, 0.1f, 0.1f,
                Short.parseShort("0"), Short.valueOf("1"), BigDecimal.TEN);
        CommonCopyMapper<Types, TypesDto> mapper = MapperRepository.getInstance().getMapper(Types.class, TypesDto.class);
        Types type = mapper.from(record);

        Assertions.assertEquals("tom", type.getName());
        Assertions.assertEquals(40, type.getAge());
        Assertions.assertEquals(28, type.getAge0());
        Assertions.assertEquals(m, type.getTime1().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        Assertions.assertEquals(m, type.getTime2().toEpochMilli());
        Assertions.assertEquals(new Date(m), type.getTime3());
        Assertions.assertFalse(type.isFlag());
        Assertions.assertTrue(type.getFlag0());
        Assertions.assertEquals(500.32, type.getMoney());
        Assertions.assertEquals(100.01, type.getMoney0());
        Assertions.assertEquals(100, type.getCount());
        Assertions.assertEquals(20, type.getCount0());
        Assertions.assertEquals(0.1f, type.getWeight());
        Assertions.assertEquals(0.1f, type.getWeight0());
        Assertions.assertEquals(0, type.getHeight());
        Assertions.assertEquals(Short.parseShort("1"), type.getHeight0());
        Assertions.assertEquals(BigDecimal.TEN, type.getPrice());
    }

    @Test
    @DisplayName("测试Map到类的映射 - 验证所有数据类型转换")
    void testFromMap () {
        var m = System.currentTimeMillis();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "tom");
        map.put("age", 40);
        map.put("age0", 28);
        map.put("time1", LocalDateTime.ofInstant(Instant.ofEpochMilli(m), ZoneId.systemDefault()));
        map.put("time2", Instant.ofEpochMilli(m));
        map.put("time3", new Date(m));
        map.put("flag", false);
        map.put("flag0", true);
        map.put("money", 500.32d);
        map.put("money0", 100.01d);
        map.put("count", 100L);
        map.put("count0", 20L);
        map.put("weight", 0.1f);
        map.put("weight0", 0.1f);
        map.put("height", Short.parseShort("0"));
        map.put("height0", Short.valueOf("1"));
        map.put("price", BigDecimal.TEN);

        FromMapMapper<Types> mapper = MapperRepository.getInstance().getMapMapper(Types.class);
        Types type = mapper.fromMap(map);

        Assertions.assertEquals("tom", type.getName());
        Assertions.assertEquals(40, type.getAge());
        Assertions.assertEquals(28, type.getAge0());
        Assertions.assertEquals(m, type.getTime1().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        Assertions.assertEquals(m, type.getTime2().toEpochMilli());
        Assertions.assertEquals(new Date(m), type.getTime3());
        Assertions.assertFalse(type.isFlag());
        Assertions.assertTrue(type.getFlag0());
        Assertions.assertEquals(500.32, type.getMoney());
        Assertions.assertEquals(100.01, type.getMoney0());
        Assertions.assertEquals(100, type.getCount());
        Assertions.assertEquals(20, type.getCount0());
        Assertions.assertEquals(0.1f, type.getWeight());
        Assertions.assertEquals(0.1f, type.getWeight0());
        Assertions.assertEquals(0, type.getHeight());
        Assertions.assertEquals(Short.parseShort("1"), type.getHeight0());
        Assertions.assertEquals(BigDecimal.TEN, type.getPrice());
    }


}
