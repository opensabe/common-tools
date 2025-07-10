package io.github.opensabe.common.redisson.test.common;

import io.github.opensabe.common.redisson.annotation.slock.RedissonLock;
import io.github.opensabe.common.redisson.annotation.slock.SLock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author heng.ma
 */
public class AnnotationMergeTest {

    @RedissonLock(name = "myname1")
    public static class App {


        @RedissonLock(name = "parentMethod")
        public void doSomething () {

        }
    }

    public static class Child extends App {

        @RedissonLock(name = "childMethod")
        @Override
        public void doSomething () {

        }
    }


    @Test
    void testAnnotationName () throws NoSuchMethodException {
        Method method = App.class.getMethod("doSomething");
        SLock lock = AnnotatedElementUtils.findMergedAnnotation(method, SLock.class);
        assertThat(lock.name())
                .containsExactly("parentMethod");
    }

    @Test
    void testChildClass () throws NoSuchMethodException {
        SLock set = AnnotatedElementUtils.findMergedAnnotation(App.class, SLock.class);

        SLock set1 = AnnotatedElementUtils.findMergedAnnotation(Child.class, SLock.class);

        assertThat(set).isEqualTo(set1);

    }

    @Test
    void testChildMethod () throws NoSuchMethodException {
        Method method = Child.class.getMethod("doSomething");
        SLock locks = AnnotatedElementUtils.findMergedAnnotation(method, SLock.class);
        Assertions.assertNotNull(locks);
    }
}
