package io.github.opensabe.common.redisson.test.common;

import io.github.opensabe.common.redisson.annotation.Lock;
import io.github.opensabe.common.redisson.annotation.SLock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author heng.ma
 */
public class AnnotationMergeTest {

    @Lock(name = "myname1")
    public static class App {


        @Lock(name = "parentMethod")
        public void doSomething () {

        }
    }

    public static class Child extends App {

        @Lock(name = "childMethod")
        @Override
        public void doSomething () {

        }
    }


    @Test
    void testAnnotationName () throws NoSuchMethodException {
        Method method = App.class.getMethod("doSomething");
        SLock lock = AnnotatedElementUtils.findMergedAnnotation(method, SLock.class);
        Assertions.assertEquals("parentMethod", lock.name());
    }

    @Test
    void testChildClass () throws NoSuchMethodException {
        Set<SLock> set = AnnotatedElementUtils.findMergedRepeatableAnnotations(App.class, SLock.class);

        Set<SLock> set1 = AnnotatedElementUtils.findMergedRepeatableAnnotations(Child.class, SLock.class);

        assertThat(set).isEqualTo(set1);

    }

    @Test
    void testChildMethod () throws NoSuchMethodException {
        Method method = Child.class.getMethod("doSomething");
        Set<SLock> locks = AnnotatedElementUtils.findMergedRepeatableAnnotations(method, SLock.class);
        Assertions.assertEquals(1, locks.size());
    }
}
