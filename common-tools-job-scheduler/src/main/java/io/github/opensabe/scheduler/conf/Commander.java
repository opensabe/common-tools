package io.github.opensabe.scheduler.conf;

import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class Commander {

    private SchedulerProperties schedulerProperties;

    private final RedissonClient redissonClient;

    private final AtomicInteger exceptionCount = new AtomicInteger();

    private volatile RLock lock;

    private volatile boolean isLeader;

    public Commander(RedissonClient redissonClient, SchedulerProperties schedulerProperties) {
        this.redissonClient = redissonClient;
        this.isLeader = false;
        this.exceptionCount.set(0);
        this.schedulerProperties = schedulerProperties;
    }

    public void closeCommander() {
        stopCommander();
    }

    public void stopCommander() {
        //原来通过 interrupt 实现，是不太行的，很可能没感知到 interrupt leaderLatch就退出了。
        //不建议立刻释放锁，虽然这样体验更好，但是有以下几个问题：
        //1. 释放锁另一个进程会立刻获取锁，但是这个进程可能定时任务还在执行没有来得及感知到 isLeader 为 true，增加了出问题的风险
        //2. lock.isHeldByThread(leaderLatch.getId()) 与 lock.forceUnlock();之间可能因为 GC 或者其他原因有延迟，这期间可能锁正好释放，被其他人抢了，然后这里强制释放了别人的锁
//        try {
//            if (lock != null && lock.isHeldByThread(leaderLatch.getId())) {
//                isLeader = false;
//                lock.forceUnlock();
//            }
//        } catch (Exception e) {
//            log.error("RedissonScheduledBeanPostProcessor-destroy: {}, error:, {}", executorWrapper.name, e.getMessage(), e);
//        }
    }

    public void setUp() {
        lock = redissonClient.getLock(schedulerProperties.getBusinessLine() + "taskCenter-commander:leader:lock");
        //直接忽略
        //查看这个锁是否被此子线程获取，实际就是判断objectMonitor的_owner的值和此线程的id是否同一个
        //获取到锁，则把标识赋值为true
        //连续 5 次异常才会考虑释放 leader
        //捕获所有 RedisException，
        //子线程专门抢锁
        Thread leaderLatch = new Thread(() -> {
            lock.lock();
            log.info("Commander-setUp-{} get redisson lock! Will become a leader!", Thread.currentThread().getId());
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    //直接忽略
                }
                //查看这个锁是否被此子线程获取，实际就是判断objectMonitor的_owner的值和此线程的id是否同一个
                try {
                    if (lock.isHeldByCurrentThread()) {
                        //获取到锁，则把标识赋值为true
                        isLeader = true;
                    } else {
                        isLeader = false;
                        lock.lock();
                    }
                    //连续 5 次异常才会考虑释放 leader
                    exceptionCount.set(0);
                } catch (RedisException redisException) {
                    //捕获所有 RedisException，
                    exceptionCount.getAndIncrement();
                    if (exceptionCount.get() > 5) {
                        log.error("Commander-setUp-leader exception pops up: {}, the cause maybe the redis connection error or command error or redis master-slave switch", redisException.getMessage(), redisException);
                        exceptionCount.set(0);
                        isLeader = false;
                    }
                } catch (Throwable e) {
                    log.fatal("Commander-setUp-leader exception, please check: {}", e.getMessage(), e);
                }
            }
        });
        //主线程让子线程开始运作，主线程结束
        leaderLatch.start();
    }

    public boolean isLeader() {
        return isLeader;
    }
}
