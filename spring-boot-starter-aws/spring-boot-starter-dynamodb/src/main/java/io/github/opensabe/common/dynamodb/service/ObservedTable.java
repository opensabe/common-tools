package io.github.opensabe.common.dynamodb.service;

import io.github.opensabe.common.dynamodb.observation.DynamodbExecuteContext;
import io.github.opensabe.common.dynamodb.observation.DynamodbExecuteDocumentation;
import io.github.opensabe.common.dynamodb.observation.DynamodbExecuteObservationConvention;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author heng.ma
 */
public class ObservedTable <T> implements DynamoDbTable<T> {
    private final DynamoDbTable<T> delegate;
    private final UnifiedObservationFactory unifiedObservationFactory;
    public ObservedTable(DynamoDbTable<T> delegate, UnifiedObservationFactory unifiedObservationFactory) {
        this.delegate = delegate;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Override
    public DynamoDbIndex<T> index(String indexName) {
        return delegate.index(indexName);
    }

    @Override
    public DynamoDbEnhancedClientExtension mapperExtension() {
        return delegate.mapperExtension();
    }

    @Override
    public TableSchema<T> tableSchema() {
        return delegate.tableSchema();
    }

    @Override
    public String tableName() {
        return delegate.tableName();
    }

    @Override
    public Key keyFrom(T item) {
        return delegate.keyFrom(item);
    }

    @Override
    public void createTable(CreateTableEnhancedRequest request) {
        delegate.createTable(request);
    }

    @Override
    public void createTable(Consumer<CreateTableEnhancedRequest.Builder> requestConsumer) {
        delegate.createTable(requestConsumer);
    }

    @Override
    public void createTable() {
        delegate.createTable();
    }

    @Override
    public T deleteItem(DeleteItemEnhancedRequest request) {
        Key key = request.key();
        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName() + "#deleteItem", request.key());
        key.sortKeyValue().ifPresent(v -> context.setRangeKey(v.s()));
        Observation observation = DynamodbExecuteDocumentation.DELETE_ITEM.observation(null,
                DynamodbExecuteObservationConvention.DEFAULT, () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.deleteItem(request));
    }

    @Override
    public T deleteItem(Consumer<DeleteItemEnhancedRequest.Builder> requestConsumer) {
        DeleteItemEnhancedRequest.Builder builder = DeleteItemEnhancedRequest.builder();
        requestConsumer.accept(builder);
        return deleteItem(builder.build());
    }

    @Override
    public T deleteItem(Key key) {
        return deleteItem(b -> b.key(key));
    }

    @Override
    public T deleteItem(T keyItem) {
        return deleteItem(keyFrom(keyItem));
    }

    @Override
    public DeleteItemEnhancedResponse<T> deleteItemWithResponse(DeleteItemEnhancedRequest request) {
        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName() + "#deleteItemWithResponse", request.key());
        Observation observation = DynamodbExecuteDocumentation.DELETE_ITEM.observation(null,
                DynamodbExecuteObservationConvention.DEFAULT, () -> context,
                unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.deleteItemWithResponse(request));
    }

    @Override
    public DeleteItemEnhancedResponse<T> deleteItemWithResponse(Consumer<DeleteItemEnhancedRequest.Builder> requestConsumer) {
        DeleteItemEnhancedRequest.Builder builder = DeleteItemEnhancedRequest.builder();
        requestConsumer.accept(builder);
        return deleteItemWithResponse(builder.build());
    }

    @Override
    public T getItem(GetItemEnhancedRequest request) {
        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName() + "#getItem", request.key());
        Observation observation = DynamodbExecuteDocumentation.SELECT.observation(null,
                DynamodbExecuteObservationConvention.DEFAULT, () -> context,
                unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.getItem(request));
    }

    @Override
    public T getItem(Consumer<GetItemEnhancedRequest.Builder> requestConsumer) {
        GetItemEnhancedRequest.Builder builder = GetItemEnhancedRequest.builder();
        requestConsumer.accept(builder);
        return getItem(builder.build());
    }

    @Override
    public T getItem(Key key) {
        return getItem(b -> b.key(key));
    }

    @Override
    public T getItem(T keyItem) {
        return getItem(keyFrom(keyItem));
    }

    @Override
    public GetItemEnhancedResponse<T> getItemWithResponse(GetItemEnhancedRequest request) {
        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName() + "#getItemWithResponse", request.key());
        Observation observation = DynamodbExecuteDocumentation.SELECT.observation(null,
                DynamodbExecuteObservationConvention.DEFAULT, () -> context,
                unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.getItemWithResponse(request));
    }

    @Override
    public GetItemEnhancedResponse<T> getItemWithResponse(Consumer<GetItemEnhancedRequest.Builder> requestConsumer) {
        GetItemEnhancedRequest.Builder builder = GetItemEnhancedRequest.builder();
        requestConsumer.accept(builder);
        return getItemWithResponse(builder.build());
    }

    @Override
    public PageIterable<T> query(QueryEnhancedRequest request) {
        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName() + "#query");
        Observation observation = DynamodbExecuteDocumentation.SELECT.observation(null,
                DynamodbExecuteObservationConvention.DEFAULT, () -> context,
                unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.query(request.toBuilder().queryConditional(new QueryConditionalWrapper(request.queryConditional(), context::setExpression)).build()));
    }

    @Override
    public PageIterable<T> query(Consumer<QueryEnhancedRequest.Builder> requestConsumer) {
        QueryEnhancedRequest.Builder builder = QueryEnhancedRequest.builder();
        requestConsumer.accept(builder);
        return query(builder.build());
    }

    @Override
    public PageIterable<T> query(QueryConditional queryConditional) {
        return query(b -> b.queryConditional(queryConditional));
    }

    @Override
    public void putItem(PutItemEnhancedRequest<T> request) {
        Key key = keyFrom(request.item());
        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName() + "#putItem", key);
        Observation observation = DynamodbExecuteDocumentation.PUT_ITEM.observation(null, DynamodbExecuteObservationConvention.DEFAULT, () -> context, unifiedObservationFactory.getObservationRegistry());
        observation.observe(() -> delegate.putItem(request));
    }

    @Override
    public void putItem(Consumer<PutItemEnhancedRequest.Builder<T>> requestConsumer) {
        PutItemEnhancedRequest.Builder<T> builder = PutItemEnhancedRequest.builder(tableSchema().itemType().rawClass());
        requestConsumer.accept(builder);
        putItem(builder.build());
    }

    @Override
    public void putItem(T item) {
        putItem(b -> b.item(item));
    }

    @Override
    public PutItemEnhancedResponse<T> putItemWithResponse(PutItemEnhancedRequest<T> request) {
        Key key = keyFrom(request.item());
        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName() + "#putItemWithResponse", key);
        Observation observation = DynamodbExecuteDocumentation.PUT_ITEM.observation(null, DynamodbExecuteObservationConvention.DEFAULT, () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.putItemWithResponse(request));
    }

    @Override
    public PutItemEnhancedResponse<T> putItemWithResponse(Consumer<PutItemEnhancedRequest.Builder<T>> requestConsumer) {
        PutItemEnhancedRequest.Builder<T> builder = PutItemEnhancedRequest.builder(tableSchema().itemType().rawClass());
        requestConsumer.accept(builder);
        return putItemWithResponse(builder.build());
    }

    @Override
    public PageIterable<T> scan(ScanEnhancedRequest request) {
        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName() + "#scan");
        Expression expression = request.filterExpression();
        if (Objects.nonNull(expression)) {
            context.setExpression(expression);
        }
        Observation observation = DynamodbExecuteDocumentation.SELECT.observation(null, DynamodbExecuteObservationConvention.DEFAULT, () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.scan(request));
    }

    @Override
    public PageIterable<T> scan(Consumer<ScanEnhancedRequest.Builder> requestConsumer) {
        ScanEnhancedRequest.Builder builder = ScanEnhancedRequest.builder();
        requestConsumer.accept(builder);
        return scan(builder.build());
    }

    @Override
    public PageIterable<T> scan() {
        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName() + "#scan()");
        Observation observation = DynamodbExecuteDocumentation.SELECT.observation(null, DynamodbExecuteObservationConvention.DEFAULT, () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.scan());
    }

    @Override
    public T updateItem(UpdateItemEnhancedRequest<T> request) {
        Key key = keyFrom(request.item());
        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName() + "#updateItem", key);
        Observation observation = DynamodbExecuteDocumentation.UPDATE_ITEM.observation(null, DynamodbExecuteObservationConvention.DEFAULT, () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.updateItem(request));
    }

    @Override
    public T updateItem(Consumer<UpdateItemEnhancedRequest.Builder<T>> requestConsumer) {
        UpdateItemEnhancedRequest.Builder<T> builder = UpdateItemEnhancedRequest.builder(tableSchema().itemType().rawClass());
        requestConsumer.accept(builder);
        return updateItem(builder.build());
    }

    @Override
    public T updateItem(T item) {
        return updateItem(b -> b.item(item));
    }

    @Override
    public UpdateItemEnhancedResponse<T> updateItemWithResponse(UpdateItemEnhancedRequest<T> request) {
        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName() + "#updateItemWithResponse", keyFrom(request.item()));
        Observation observation = DynamodbExecuteDocumentation.UPDATE_ITEM.observation(null, DynamodbExecuteObservationConvention.DEFAULT, () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.updateItemWithResponse(request));
    }

    @Override
    public UpdateItemEnhancedResponse<T> updateItemWithResponse(Consumer<UpdateItemEnhancedRequest.Builder<T>> requestConsumer) {
        UpdateItemEnhancedRequest.Builder<T> builder = UpdateItemEnhancedRequest.builder(tableSchema().itemType().rawClass());
        requestConsumer.accept(builder);
        return updateItemWithResponse(builder.build());
    }

    @Override
    public void deleteTable() {
        delegate.deleteTable();
    }

    @Override
    public DescribeTableEnhancedResponse describeTable() {
        return delegate.describeTable();
    }
}
