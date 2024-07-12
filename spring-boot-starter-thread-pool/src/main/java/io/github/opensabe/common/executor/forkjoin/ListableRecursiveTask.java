package io.github.opensabe.common.executor.forkjoin;

import io.micrometer.observation.Observation;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;
/**
 * 根据列表切分任务的task，并将列表里的元素转换成新的类型返回
 * @author heng.ma
 * @param <T>   列表的数据类型
 */
public abstract class ListableRecursiveTask<T,R> extends TraceableRecursiveTask<R> {

    @Getter
    protected int capacity;
    @Getter
    protected List<T> list;
    protected Function<T,R> transformer;
    protected Function<List<R>,R> combiner;
    protected BinaryOperator<R> reducer;

    protected ListableRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, Function<List<R>, R> combiner, BinaryOperator<R> reducer, Observation observation) {
        super(observation);
        this.capacity = capacity;
        this.list = list;
        this.transformer = transformer;
        this.combiner = combiner;
        this.reducer = reducer;
    }
    public ListableRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, Function<List<R>, R> combiner, Observation observation) {
        this(capacity,list,transformer,combiner,null,observation);
    }
    public ListableRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, BinaryOperator<R> reducer, Observation observation) {
        this(capacity,list,transformer,null,reducer,observation);
    }


    @Override
    protected R compute0() {
        var tasks = segmentation();
        if (CollectionUtils.isEmpty(tasks))
            return aggregate(list.stream().map(transformer));
        var stream = invokeAll(tasks)
                        .stream()
                        .map(ListableRecursiveTask::join);
        return aggregate(stream);
    }
    protected abstract List<ListableRecursiveTask<T,R>> segmentation ();

    protected abstract R aggregate (Stream<R> result);
}
