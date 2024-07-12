package io.github.opensabe.common.executor.forkjoin;


import io.micrometer.observation.Observation;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public abstract class SegmentRecursiveTask<T,R> extends ListableRecursiveTask<T,R> {

    protected abstract SegmentRecursiveTask<T,R> clone(List<T> current) ;

    protected SegmentRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, Function<List<R>, R> combiner, BinaryOperator<R> reducer, Observation observation) {
        super(capacity, list, transformer, combiner, reducer,observation);
    }

    public SegmentRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, Function<List<R>, R> combiner, Observation observation) {
        super(capacity, list, transformer, combiner, observation);
    }

    public SegmentRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, BinaryOperator<R> reducer, Observation observation) {
        super(capacity, list, transformer, reducer, observation);
    }

    @Override
    protected List<ListableRecursiveTask<T, R>> segmentation() {
        if (list.size() > capacity) {
            var left = clone(list.subList(0,capacity));
            var right = clone(list.subList(capacity,list.size()));
            return List.of(left,right);
        }
        return null;
    }

}
