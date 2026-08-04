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
package io.github.opensabe.common.redisson.test.common;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

import io.github.opensabe.common.redisson.annotation.slock.RedissonLock;
import io.github.opensabe.common.redisson.annotation.slock.SLock;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redisson 锁注解在类/方法继承与合并场景下的解析测试。
 *
 * @author heng.ma
 */
@DisplayName("锁注解合并解析测试")
public class AnnotationMergeTest {

    @DisplayName("方法级合并注解 name 解析")
    @Test
    void testAnnotationName() throws NoSuchMethodException {
        Method method = App.class.getMethod("doSomething");
        SLock lock = AnnotatedElementUtils.findMergedAnnotation(method, SLock.class);
        assertThat(lock.name())
                .containsExactly("parentMethod");
    }

    @DisplayName("子类继承父类类级注解")
    @Test
    void testChildClass() throws NoSuchMethodException {
        SLock set = AnnotatedElementUtils.findMergedAnnotation(App.class, SLock.class);

        SLock set1 = AnnotatedElementUtils.findMergedAnnotation(Child.class, SLock.class);

        assertThat(set).isEqualTo(set1);

    }

    @DisplayName("子类方法覆盖父类方法注解合并")
    @Test
    void testChildMethod() throws NoSuchMethodException {
        Method method = Child.class.getMethod("doSomething");
        SLock locks = AnnotatedElementUtils.findMergedAnnotation(method, SLock.class);
        Assertions.assertNotNull(locks);
    }

    @RedissonLock(name = "myname1")
    public static class App {


        @RedissonLock(name = "parentMethod")
        public void doSomething() {

        }
    }

    public static class Child extends App {

        @RedissonLock(name = "childMethod")
        @Override
        public void doSomething() {

        }
    }
}
