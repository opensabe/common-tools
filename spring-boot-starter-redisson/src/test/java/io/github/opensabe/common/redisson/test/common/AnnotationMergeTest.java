package io.github.opensabe.common.redisson.test.common;

import io.github.opensabe.common.redisson.annotation.FairLock;
import io.github.opensabe.common.redisson.annotation.SLock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * @author heng.ma
 */
public class AnnotationMergeTest {

    @FairLock(name = "myname")
    public static class App {


        public void doSomething () {

        }
    }

    @Test
    void testOverrideName () {
        SLock lock = AnnotatedElementUtils.getMergedAnnotation(App.class, SLock.class);
        Assertions.assertEquals("myname", lock.name());
    }
}
