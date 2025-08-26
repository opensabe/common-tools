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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 滑动窗口类
 */
public class SlidingWindow {
    /* 队列的总长度  */
    private final int timeSliceSize;
    /* 每个时间片的时长 */
    private final long timeMillisPerSlice;
    /* 窗口长度 */
    private final int windowSize;
    private AtomicInteger[] timeSlices;
    private Semaphore semaphore = new Semaphore(0);
    /* 当前所使用的时间片位置 */
    private AtomicInteger cursor = new AtomicInteger(0);

    public SlidingWindow(int windowSize, Time timeSlice) {
        this.timeMillisPerSlice = timeSlice.millis;
        this.windowSize = windowSize;
        // 保证存储在至少两个window
        this.timeSliceSize = windowSize * 2 + 1;

        init();
    }

    public static void main(String[] args) throws InterruptedException {
        SlidingWindow window = new SlidingWindow(5, Time.SECONDS);
        long start = System.currentTimeMillis();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    window.await(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getId() + "在" + (System.currentTimeMillis() - start) + "ms 获取到了");
            }
        };

        while (true) {
            new Thread(runnable).start();
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    /**
     * 初始化
     */
    private void init() {
        AtomicInteger[] localTimeSlices = new AtomicInteger[timeSliceSize];
        for (int i = 0; i < timeSliceSize; i++) {
            localTimeSlices[i] = new AtomicInteger(0);
        }
        timeSlices = localTimeSlices;
    }

    private int locationIndex() {
        long time = System.currentTimeMillis();
        return (int) ((time / timeMillisPerSlice) % timeSliceSize);
    }

    /**
     * 判断是否允许进行访问，未超过阈值的话才会对某个时间片+1
     *
     * @param threshold
     * @return
     */
    private boolean allow(int threshold) {
        int index = locationIndex();
        int sum = 0;
        int oldCursor = cursor.getAndSet(index);
        if (oldCursor != index) {
            timeSlices[index].set(0);
            clearBetween(oldCursor, index);
        }
        for (int i = 0; i < windowSize; i++) {
            sum += timeSlices[(index - i + timeSliceSize) % timeSliceSize].get();
        }
        // 阈值判断
        if (sum < threshold) {
            // 未超过阈值才+1
            timeSlices[index].incrementAndGet();
            semaphore.release(threshold - 1 - sum);
            return true;
        }
        return false;
    }

    public void await(int threshold) throws InterruptedException {
        while (!allow(threshold)) {
            semaphore.tryAcquire(50, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 将fromIndex~toIndex之间的时间片计数都清零
     * 极端情况下，当循环队列已经走了超过1个timeSliceSize以上，这里的清零并不能如期望的进行
     *
     * @param fromIndex 不包含
     * @param toIndex   不包含
     */
    private void clearBetween(int fromIndex, int toIndex) {
        for (int index = (fromIndex + 1) % timeSliceSize; index != toIndex; index = (index + 1) % timeSliceSize) {
            timeSlices[index].set(0);
        }
    }

    public static enum Time {
        MILLISECONDS(1),
        SECONDS(1000),
        MINUTES(SECONDS.getMillis() * 60),
        HOURS(MINUTES.getMillis() * 60),
        DAYS(HOURS.getMillis() * 24),
        WEEKS(DAYS.getMillis() * 7);

        private long millis;

        Time(long millis) {
            this.millis = millis;
        }

        public long getMillis() {
            return millis;
        }
    }
}

