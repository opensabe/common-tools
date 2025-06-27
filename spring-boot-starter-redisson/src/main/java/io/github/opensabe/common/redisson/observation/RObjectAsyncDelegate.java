package io.github.opensabe.common.redisson.observation;

import org.redisson.api.ObjectEncoding;
import org.redisson.api.ObjectListener;
import org.redisson.api.RFuture;
import org.redisson.api.RObjectAsync;

import java.util.concurrent.TimeUnit;

/**
 * 该类实现 RObjectAsync 接口，将接口方法的调用委托给内部持有的 RObjectAsync 实例。
 *
 * @author heng.ma
 */
public class RObjectAsyncDelegate<T extends RObjectAsync> implements RObjectAsync {

    // 被委托的 RObjectAsync 实例
    protected final T delegate;

    /**
     * 构造函数，初始化委托对象。
     *
     * @param delegate 要委托的 RObjectAsync 实例，不能为 null
     */
    public RObjectAsyncDelegate(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public RFuture<Long> getIdleTimeAsync() {
        return delegate.getIdleTimeAsync();
    }

    @Override
    public RFuture<Integer> getReferenceCountAsync() {
        return delegate.getReferenceCountAsync();
    }

    @Override
    public RFuture<Integer> getAccessFrequencyAsync() {
        return delegate.getAccessFrequencyAsync();
    }

    @Override
    public RFuture<ObjectEncoding> getInternalEncodingAsync() {
        return delegate.getInternalEncodingAsync();
    }

    @Override
    public RFuture<Long> sizeInMemoryAsync() {
        return delegate.sizeInMemoryAsync();
    }

    @Override
    public RFuture<Void> restoreAsync(byte[] state) {
        return delegate.restoreAsync(state);
    }

    @Override
    public RFuture<Void> restoreAsync(byte[] state, long timeToLive, TimeUnit timeUnit) {
        return delegate.restoreAsync(state, timeToLive, timeUnit);
    }

    @Override
    public RFuture<Void> restoreAndReplaceAsync(byte[] state) {
        return delegate.restoreAndReplaceAsync(state);
    }

    @Override
    public RFuture<Void> restoreAndReplaceAsync(byte[] state, long timeToLive, TimeUnit timeUnit) {
        return delegate.restoreAndReplaceAsync(state, timeToLive, timeUnit);
    }

    @Override
    public RFuture<byte[]> dumpAsync() {
        return delegate.dumpAsync();
    }

    @Override
    public RFuture<Boolean> touchAsync() {
        return delegate.touchAsync();
    }

    @Override
    public RFuture<Void> migrateAsync(String host, int port, int database, long timeout) {
        return delegate.migrateAsync(host, port, database, timeout);
    }

    @Override
    public RFuture<Void> copyAsync(String host, int port, int database, long timeout) {
        return delegate.copyAsync(host, port, database, timeout);
    }

    @Override
    public RFuture<Boolean> copyAsync(String destination) {
        return delegate.copyAsync(destination);
    }

    @Override
    public RFuture<Boolean> copyAsync(String destination, int database) {
        return delegate.copyAsync(destination, database);
    }

    @Override
    public RFuture<Boolean> copyAndReplaceAsync(String destination) {
        return delegate.copyAndReplaceAsync(destination);
    }

    @Override
    public RFuture<Boolean> copyAndReplaceAsync(String destination, int database) {
        return delegate.copyAndReplaceAsync(destination, database);
    }

    @Override
    public RFuture<Boolean> moveAsync(int database) {
        return delegate.moveAsync(database);
    }

    @Override
    public RFuture<Boolean> deleteAsync() {
        return delegate.deleteAsync();
    }

    @Override
    public RFuture<Boolean> unlinkAsync() {
        return delegate.unlinkAsync();
    }

    @Override
    public RFuture<Void> renameAsync(String newName) {
        return delegate.renameAsync(newName);
    }

    @Override
    public RFuture<Boolean> renamenxAsync(String newName) {
        return delegate.renamenxAsync(newName);
    }

    @Override
    public RFuture<Boolean> isExistsAsync() {
        return delegate.isExistsAsync();
    }

    @Override
    public RFuture<Integer> addListenerAsync(ObjectListener listener) {
        return delegate.addListenerAsync(listener);
    }

    @Override
    public RFuture<Void> removeListenerAsync(int listenerId) {
        return delegate.removeListenerAsync(listenerId);
    }
}
