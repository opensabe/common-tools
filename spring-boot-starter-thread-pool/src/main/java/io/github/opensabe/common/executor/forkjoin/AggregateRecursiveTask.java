package io.github.opensabe.common.executor.forkjoin;

import io.micrometer.observation.Observation;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AggregateRecursiveTask<T,R> extends SegmentRecursiveTask<T,R> {
    @Override
    protected SegmentRecursiveTask<T, R> clone(List<T> current) {
        return new AggregateRecursiveTask<>(capacity,current,transformer,combiner,reducer,observation);
    }

    protected AggregateRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, Function<List<R>, R> combiner, BinaryOperator<R> reducer, Observation observation) {
        super(capacity, list, transformer, combiner, reducer,observation);
    }

    public AggregateRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, Function<List<R>, R> combiner, Observation observation) {
        super(capacity, list, transformer, combiner,observation);
    }

    public AggregateRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, BinaryOperator<R> reducer, Observation observation) {
        super(capacity, list, transformer, reducer,observation);
    }

    @Override
    protected R aggregate(Stream<R> result) {
        return reducer == null ?
                combiner.apply(result.collect(Collectors.toList()))
                : result.reduce(reducer).orElse(null);
    }
}
