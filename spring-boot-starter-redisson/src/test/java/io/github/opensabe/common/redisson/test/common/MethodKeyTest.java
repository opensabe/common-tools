package io.github.opensabe.common.redisson.test.common;

import io.github.opensabe.common.redisson.annotation.slock.FencedLock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试Map以Method为key，遇到方法重载会不会有问题
 * @author heng.ma
 */
public class MethodKeyTest {


    public static class Animal {

        @FencedLock(name = "eat default")
        public void eat() {
            System.out.println("Animal eat");
        }

        @FencedLock(name = "eat food")
        public void eat(String food) {
            System.out.println("Animal eat" + food);
        }

    }

    @Test
    void testHash () throws NoSuchMethodException {
        Method method1 = Animal.class.getMethod("eat");
        Method method2 = Animal.class.getMethod("eat", String.class);

        FencedLock f1 = method1.getAnnotation(FencedLock.class);

        FencedLock f2 = method2.getAnnotation(FencedLock.class);

        System.out.println(f1.name()[0]);
        System.out.println(f2.name()[0]);

        Map<Method, FencedLock> map = new HashMap<>(2);

        map.put(method1, f1);
        map.put(method2, f2);

        System.out.println(map.size());
        System.out.println(map.get(method1).name()[0]);
        System.out.println(map.get(method2).name()[0]);
        Assertions.assertEquals(f1, map.get(method1));
        Assertions.assertEquals("eat default", map.get(method1).name()[0]);
        Assertions.assertEquals(f2, map.get(method2));
        Assertions.assertEquals("eat food", map.get(method2).name()[0]);
    }
}
