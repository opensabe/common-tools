package io.github.opensabe.common.executor.forkjoin;

import io.micrometer.observation.Observation;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 单个任务也是批量处理，仅仅是将大的列表切成小列表处理
 * @author heng.ma
 * @param <T>   要处理的集合里的数据类型
 */
public class BatchRecursiveAction<T> extends SegmentRecursiveTask<T,Void> {
    protected Consumer<List<T>> batchConsumer;
    protected BatchRecursiveAction(int capacity, List<T> list, Consumer<List<T>> batchConsumer, Observation observation) {
        super(capacity, list, null, null, null,observation);
        this.batchConsumer = batchConsumer;
    }

    @Override
    protected Void compute0() {
        var tasks = segmentation();
        if (CollectionUtils.isEmpty(tasks)) {
            batchConsumer.accept(list);
        }else {
            invokeAll(tasks).forEach(ListableRecursiveTask::join);
        }
        return null;
    }

    @Override
    protected Void aggregate(Stream<Void> result) {
        return null;
    }

    @Override
    protected SegmentRecursiveTask<T, Void> clone(List<T> current) {
        return new BatchRecursiveAction<>(capacity,current,batchConsumer,observation);
    }
}
