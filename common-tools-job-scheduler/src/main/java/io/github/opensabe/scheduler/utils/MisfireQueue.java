package io.github.opensabe.scheduler.utils;

import io.github.opensabe.scheduler.job.SchedulerJob;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MisfireQueue {

    private static final Queue<SchedulerJob> queue = new ConcurrentLinkedDeque<>();

    public static void enqueue(SchedulerJob job) {
        queue.add(job);
    }

    public static SchedulerJob nextJob() {
        return queue.poll();
    }

    public static boolean isEmpty() {
        return queue.isEmpty();
    }

}
