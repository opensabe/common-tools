package io.github.opensabe.common.redisson.test;

import io.github.opensabe.common.redisson.annotation.FairLock;
import io.github.opensabe.common.redisson.annotation.Lock;
import io.github.opensabe.common.redisson.annotation.SLock;
import io.github.opensabe.common.redisson.annotation.SpinLock;
import io.github.opensabe.common.redisson.config.RedissonAopConfiguration;
import io.github.opensabe.common.redisson.exceptions.RedissonClientException;
import io.github.opensabe.common.redisson.exceptions.RedissonLockException;
import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Import(SLockTest.Config.class)
public class SLockTest extends BaseRedissonTest {
    private static final int THREAD_COUNT = 10;
    private static final int ADD_COUNT = 10000;

    @Autowired
    private RedissonAopConfiguration redissonAopConfiguration;

    public static class Config {
        @Autowired
        private RedissonClient redissonClient;

        @Bean
        @Primary
        public TestRedissonLockClass testRedissonLockClass() {
            return new TestRedissonLockClass(redissonClient);
        }

        @Bean
        public TestRedissonLockClassExtends testRedissonLockClassExtends() {
            return new TestRedissonLockClassExtends(redissonClient);
        }

        @Bean
        public TestRedissonLockInterfaceImpl testRedissonLockInterface() {
            return new TestRedissonLockInterfaceImpl(redissonClient);
        }
    }

    public interface TestRedissonLockInterface1 {
        @Lock(prefix = "p1", name = "#name")
        void testBlockLockWithName(String name) throws InterruptedException;
    }

    public interface TestRedissonLockInterface2 {
        @SpinLock(prefix = "p1", name = "#name")
        void testBlockSpinLock(String name) throws InterruptedException;
    }

    @Data
    public static class BaseClass {
        protected volatile int count = 0;
        protected final RedissonClient redissonClient;

        public void reset() {
            count = 0;
        }

        protected void add() throws InterruptedException {
            for (int i = 0; i < ADD_COUNT; i++) {
                count = count + 1;
            }
        }
    }


    public static class TestRedissonLockInterfaceImpl extends BaseClass implements TestRedissonLockInterface1, TestRedissonLockInterface2{
        public TestRedissonLockInterfaceImpl(RedissonClient redissonClient) {
            super(redissonClient);
        }

        @Override
        public void testBlockLockWithName(String name) throws InterruptedException {
            add();
        }

        @Override
        public void testBlockSpinLock(String name) throws InterruptedException {
            add();
        }
    }

    public static class TestRedissonLockClass extends BaseClass {
        public TestRedissonLockClass(RedissonClient redissonClient) {
            super(redissonClient);
        }

        @Lock(prefix = "p1", name = "testNoLock")
        public void testNoLock() throws InterruptedException {
            add();
        }

        @Lock(prefix = "p1", name = "testBlockLockWithNoName")
        public void testBlockLockWithNoName() throws InterruptedException {
            add();
        }

        @Lock(prefix = "p1", name = "#name")
        public void testBlockLock(String name) throws InterruptedException {
            add();
        }

        @SpinLock(prefix = "p1", name = "#name")
        public void testBlockSpinLock(String name) throws InterruptedException {
            add();
        }

        @FairLock(prefix = "p1", name = "#name")
        public void testBlockFairLock(String name) throws InterruptedException {
            add();
        }

        @Lock(prefix = "p1", name = "#name", lockType = SLock.LockType.TRY_LOCK, waitTime = 100000, timeUnit = TimeUnit.MILLISECONDS)
        public void testTryLock(String name) throws InterruptedException {
            add();
        }

        @Lock(prefix = "p1", name = "#name", lockType = SLock.LockType.TRY_LOCK_NOWAIT)
        public void testTryLockNoWait( String name) throws InterruptedException {
            add();
            //3s 肯定够100个线程都 try lock 失败
            TimeUnit.SECONDS.sleep(3);
        }

        @Lock(prefix = "test:", name = "#student.id == null? #student.name:#student.id")
        public void testRedissonLockNameProperty(Student student, String params) throws InterruptedException {
            String lockName = student.getId() == null ? student.getName() : student.getId();
            RLock lock = redissonClient.getLock("test:" + lockName);
            Assertions.assertTrue(lock.isHeldByCurrentThread());
        }

        @Lock(prefix = "p1", name = "#name", leaseTime = 1000L)
        public void testLockTime(String name) throws InterruptedException {
            RLock lock = redissonClient.getLock("p1"+name);
            //验证获取了锁
            Assertions.assertTrue(lock.isHeldByCurrentThread());
            TimeUnit.SECONDS.sleep(2);
            //过了两秒，锁应该被释放了
            Assertions.assertFalse(lock.isLocked());
        }

        //waitTime只对于 trylock 有效
        @Lock(prefix = "p1", name = "#name", lockType = SLock.LockType.TRY_LOCK, waitTime = 1000L)
        public void testWaitTime(String name) throws InterruptedException {
            RLock lock = redissonClient.getLock("p1"+name);
            //验证获取了锁
            Assertions.assertTrue(lock.isHeldByCurrentThread());
            TimeUnit.SECONDS.sleep(10);
        }

        @Lock(prefix = "p1", name = "#errorExpression")
        public void testErrorExpression (String name) {
        }
        @Lock(prefix = "p1", name = "suppressedExpression")
        public void testSuppressedExpression (String name) throws InterruptedException {
            RLock lock = redissonClient.getLock("p1suppressedExpression");
            Assertions.assertTrue(lock.isHeldByCurrentThread());
            TimeUnit.SECONDS.sleep(10);
        }
    }
    public static class TestRedissonLockClassExtends extends TestRedissonLockClass {

        public TestRedissonLockClassExtends(RedissonClient redissonClient) {
            super(redissonClient);
        }

        @Override
        public void testBlockLockWithNoName() throws InterruptedException {
            add();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Student {
        private String name;
        private String id;
        private int age;
    }

    @Autowired
    private TestRedissonLockClass testRedissonLockClass;
    @Autowired
    private TestRedissonLockClassExtends testRedissonLockClassExtends;
    @Autowired
    private TestRedissonLockInterfaceImpl testRedissonLockInterfaceImpl;
    @Autowired
    private TestRedissonLockInterface1 testRedissonLockInterface1;
    @Autowired
    private TestRedissonLockInterface2 testRedissonLockInterface2;

    @Test
    public void testAopConfiguration() {
        Assertions.assertEquals(redissonAopConfiguration.getOrder(), BaseRedissonTest.AOP_ORDER);
    }

    @Test
    public void testMultipleLock() throws InterruptedException {
        testRedissonLockClass.reset();
        //首先测无锁试多线程更新，这样最后的值肯定小于等于期望
        Thread[] threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockClass.testNoLock();
                } catch (Exception e) {
                }
            });
            threads[i].start();
        }
        for (Thread item : threads) {
            item.join();
        }
        Assertions.assertTrue(testRedissonLockClass.getCount() <= THREAD_COUNT * ADD_COUNT);
        //测试阻塞锁，最后的值应该等于期望值
        testRedissonLockClass.reset();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockClass.testBlockLock("same");
                } catch (Exception e) {
                }
            });
            threads[i].start();
        }
        for (Thread value : threads) {
            value.join();
        }
        Assertions.assertEquals(testRedissonLockClass.getCount(), THREAD_COUNT * ADD_COUNT);
        //测试阻塞锁，最后的值应该等于期望值
        testRedissonLockClass.reset();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockClass.testBlockLockWithNoName();
                } catch (Exception e) {
                }
            });
            threads[i].start();
        }
        for (Thread value : threads) {
            value.join();
        }
        Assertions.assertEquals(testRedissonLockClass.getCount(), THREAD_COUNT * ADD_COUNT);

        testRedissonLockClass.reset();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockClass.testBlockSpinLock("same");
                } catch (Exception e) {
                    
                }
            });
            threads[i].start();
        }
        for (Thread value : threads) {
            value.join();
        }
        Assertions.assertEquals(testRedissonLockClass.getCount(), THREAD_COUNT * ADD_COUNT);

        testRedissonLockClass.reset();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockClass.testBlockFairLock("same");
                } catch (Exception e) {
                    
                }
            });
            threads[i].start();
        }
        for (Thread value : threads) {
            value.join();
        }
        Assertions.assertEquals(testRedissonLockClass.getCount(), THREAD_COUNT * ADD_COUNT);

        //测试 tryLock锁 + 等待时间，由于是本地 redis 这个 10s 等待时间应该足够，最后的值应该等于期望值
        testRedissonLockClass.reset();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockClass.testTryLock("same");
                } catch (Exception e) {
                    
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        Assertions.assertEquals(testRedissonLockClass.getCount(), THREAD_COUNT * ADD_COUNT);
        //测试 tryLock锁，不等待
        testRedissonLockClass.reset();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockClass.testTryLockNoWait("same");
                } catch (Exception e) {
                    
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        //由于锁住的时间比较久，只有一个线程执行了 add()
        Assertions.assertEquals(testRedissonLockClass.getCount(), ADD_COUNT);

        testRedissonLockInterfaceImpl.reset();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockInterfaceImpl.testBlockLockWithName("same");
                } catch (Exception e) {
                    
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        //由于锁住的时间比较久，只有一个线程执行了 add()
        Assertions.assertEquals(testRedissonLockInterfaceImpl.getCount(), THREAD_COUNT * ADD_COUNT);

        testRedissonLockInterfaceImpl.reset();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockInterfaceImpl.testBlockSpinLock("same");
                } catch (Exception e) {
                    
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        //由于锁住的时间比较久，只有一个线程执行了 add()
        Assertions.assertEquals(testRedissonLockInterfaceImpl.getCount(), THREAD_COUNT * ADD_COUNT);

        testRedissonLockInterfaceImpl.reset();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockInterface1.testBlockLockWithName("same");
                } catch (Exception e) {
                    
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        //由于锁住的时间比较久，只有一个线程执行了 add()
        Assertions.assertEquals(testRedissonLockInterfaceImpl.getCount(), THREAD_COUNT * ADD_COUNT);

        testRedissonLockInterfaceImpl.reset();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockInterface2.testBlockSpinLock("same");
                } catch (Exception e) {
                    
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        //由于锁住的时间比较久，只有一个线程执行了 add()
        Assertions.assertEquals(testRedissonLockInterfaceImpl.getCount(), THREAD_COUNT * ADD_COUNT);

        testRedissonLockClassExtends.reset();
        //测试阻塞锁，最后的值应该等于期望值
        testRedissonLockClassExtends.reset();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockClassExtends.testBlockLock("same");
                } catch (Exception e) {
                    
                }
            });
            threads[i].start();
        }
        for (Thread value : threads) {
            value.join();
        }
        Assertions.assertEquals(testRedissonLockClassExtends.getCount(), THREAD_COUNT * ADD_COUNT);
        //测试阻塞锁，最后的值应该等于期望值
        testRedissonLockClassExtends.reset();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonLockClassExtends.testBlockLockWithNoName();
                } catch (Exception e) {
                    
                }
            });
            threads[i].start();
        }
        for (Thread value : threads) {
            value.join();
        }
        Assertions.assertEquals(testRedissonLockClassExtends.getCount(), THREAD_COUNT * ADD_COUNT);
    }

    @Test
    public void testBlockProperty() throws InterruptedException {
        testRedissonLockClass.reset();
        testRedissonLockClass.testRedissonLockNameProperty(Student.builder().name("zhx").build(), "zhx");
        testRedissonLockClass.testRedissonLockNameProperty(Student.builder().id("111111").build(), "zhx");
    }

    @Test
    public void testLockTime() throws InterruptedException {
        testRedissonLockClass.reset();
        testRedissonLockClass.testLockTime("same");
    }

    @Test
    public void testWaitTime() throws InterruptedException {
        testRedissonLockClass.reset();
        Thread thread = new Thread(() -> {
            try {
                testRedissonLockClass.testWaitTime("same");
            } catch (Exception e) {
                
            }
        });
        thread.start();
        TimeUnit.SECONDS.sleep(3);
        //在等待时间内获取不到锁，抛异常
        assertThrows(RedissonClientException.class, () -> testRedissonLockClass.testWaitTime("same"));
    }

    @Test
    public void testMultiNameLock() throws InterruptedException {
        testRedissonLockClass.reset();
        //首先测无锁试多线程更新，这样最后的值肯定小于等于期望
        Thread[] threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < threads.length; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    //相当于没有锁住
                    testRedissonLockClass.testBlockLock(threads[finalI].getName());
                } catch (Exception e) {
                    
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        Assertions.assertTrue(testRedissonLockClass.getCount() <= THREAD_COUNT * ADD_COUNT);
    }

    @Test
    void testErrorExpression () {
        RedissonLockException exception = assertThrows(RedissonLockException.class, () -> testRedissonLockClass.testErrorExpression("aa"));
        System.out.println(exception.getMessage());
    }

    @Test
    void testSuppressedExpression () throws InterruptedException {
        testRedissonLockClass.testSuppressedExpression("addd");
    }
}
