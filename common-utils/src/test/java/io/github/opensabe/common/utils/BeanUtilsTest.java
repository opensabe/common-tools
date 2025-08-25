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

import io.github.opensabe.common.utils.BeanUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Bean工具类测试")
public class BeanUtilsTest {

    public record RU (String  name, Integer age) {}


    @Data
    public static class CT {
        private String name;
        private Integer age;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CU {
        private String name;
        private int age;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CC {
        private CU cu;
    }

    @Test
    @DisplayName("测试Record到普通类的属性复制 - Integer到int类型转换")
    void testRecord_Integer_to_int() {
        var source = new RU("rrr", 10);
        var target = new CU();
        BeanUtils.copyProperties(source, target);
        Assertions.assertEquals("rrr", target.name);
        Assertions.assertEquals(10, target.age);
    }

    @Test
    @DisplayName("测试普通类属性复制 - int到Integer类型转换")
    void test_int_to_Integer() {
        var source = new CU("rrr", 10);
        var target = new CT();
        BeanUtils.copyProperties(source, target);
        Assertions.assertEquals("rrr", target.name);
        Assertions.assertEquals(10, target.age);
    }

    @Test
    @DisplayName("测试嵌套对象属性复制")
    void testMixProperty () {
        var s = new CC(new CU("SSS", 10));
        var t = new CC();
        BeanUtils.copyProperties(s, t);
        Assertions.assertEquals(s.cu, t.cu);
    }


    /**
     * 直接复制，byte buddy消耗时间会久点，但是时间主要时消耗在第一次创建beanCopier,创建玩以后，再次比赛时，byte buddy的性能优势就显现出来了，
     * 其实这并不byte buddy本身性能有多好，而是这种用字节码提前生成针对与两个对象的复制属性代码的设计优势
     */
    @Test
    @DisplayName("测试Bean复制性能对比 - ByteBuddy vs Spring BeanUtils")
    void testTime () {

        //先预热
        for (int i = 0; i < 1000; i++) {
            var source = new CU("rrr", 10);
            var target = new CT();
            BeanUtils.copyProperties(source, target);
            Assertions.assertEquals("rrr", target.name);
            Assertions.assertEquals(10, target.age);
        }
        for (int i = 0; i < 1000; i++) {
            var source = new CU("rrr", 10);
            var target = new CT();
            org.springframework.beans.BeanUtils.copyProperties(source, target);
            Assertions.assertEquals("rrr", target.name);
            Assertions.assertEquals(10, target.age);
        }
        System.out.println("预热结束");
        long s1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            var source = new CU("rrr", 10);
            var target = new CT();
            BeanUtils.copyProperties(source, target);
            Assertions.assertEquals("rrr", target.name);
            Assertions.assertEquals(10, target.age);
        }
        System.out.println("Byte buddy copyProperties 100000 times cost: %d".formatted(System.currentTimeMillis()-s1));


        long s2 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            var source = new CU("rrr", 10);
            var target = new CT();
            org.springframework.beans.BeanUtils.copyProperties(source, target);
            Assertions.assertEquals("rrr", target.name);
            Assertions.assertEquals(10, target.age);
        }
        System.out.println("Spring BeanUtils copyProperties 100000 times cost: %d".formatted(System.currentTimeMillis()-s2));
    }
}
