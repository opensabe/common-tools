package io.github.opensabe.common.executor.forkjoin;

import io.micrometer.observation.Observation;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 根据列表切分任务的action
 * @author heng.ma
 * @param <T>   列表的数据类型
 */
public class ListableRecursiveAction<T> extends SegmentRecursiveTask<T,Void> {


    protected ListableRecursiveAction(int capacity, List<T> list, Function<T, Void> transformer, Observation observation) {
        super(capacity, list, transformer, null, null, observation);
    }

    public ListableRecursiveAction(int capacity, List<T> list, Consumer<T> consumer, Observation observation) {
        super(capacity, list, t -> {
            consumer.accept(t);
            return null;
        }, null,null, observation);
    }

    @Override
    protected SegmentRecursiveTask<T, Void> clone(List<T> current) {
        return new ListableRecursiveAction<>(capacity,current,transformer, observation);
    }

    @Override
    protected Void aggregate(Stream<Void> result) {
        return null;
    }

    @Override
    protected Void compute0() {
        var task = segmentation();
        if (CollectionUtils.isEmpty(task)) {
            list.forEach(transformer::apply);
        }else {
            invokeAll(task).forEach(ListableRecursiveTask::join);
        }
        return null;
    }
}
