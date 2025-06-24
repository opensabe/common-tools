package io.github.opensabe.common.redisson.observation;

import org.redisson.api.ObjectEncoding;
import org.redisson.api.ObjectListener;
import org.redisson.api.RObject;
import org.redisson.client.codec.Codec;

import java.util.concurrent.TimeUnit;

/**
 * 该类实现 RObject 接口，将接口方法的调用委托给内部持有的 RObject 实例。
 *
 * @author heng.ma
 */
public class RObjectDelegate extends RObjectAsyncDelegate implements RObject {

    // 被委托的 RObject 实例
    protected final RObject delegate;

    /**
     * 构造函数，初始化委托对象。
     *
     * @param delegate 要委托的 RObject 实例，不能为 null
     */
    public RObjectDelegate(RObject delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public Long getIdleTime() {
        return delegate.getIdleTime();
    }

    @Override
    public int getReferenceCount() {
        return delegate.getReferenceCount();
    }

    @Override
    public int getAccessFrequency() {
        return delegate.getAccessFrequency();
    }

    @Override
    public ObjectEncoding getInternalEncoding() {
        return delegate.getInternalEncoding();
    }

    @Override
    public long sizeInMemory() {
        return delegate.sizeInMemory();
    }

    @Override
    public void restore(byte[] state) {
        delegate.restore(state);
    }

    @Override
    public void restore(byte[] state, long timeToLive, TimeUnit timeUnit) {
        delegate.restore(state, timeToLive, timeUnit);
    }

    @Override
    public void restoreAndReplace(byte[] state) {
        delegate.restoreAndReplace(state);
    }

    @Override
    public void restoreAndReplace(byte[] state, long timeToLive, TimeUnit timeUnit) {
        delegate.restoreAndReplace(state, timeToLive, timeUnit);
    }

    @Override
    public byte[] dump() {
        return delegate.dump();
    }

    @Override
    public boolean touch() {
        return delegate.touch();
    }

    @Override
    public void migrate(String host, int port, int database, long timeout) {
        delegate.migrate(host, port, database, timeout);
    }

    @Override
    public void copy(String host, int port, int database, long timeout) {
        delegate.copy(host, port, database, timeout);
    }

    @Override
    public boolean copy(String destination) {
        return delegate.copy(destination);
    }

    @Override
    public boolean copy(String destination, int database) {
        return delegate.copy(destination, database);
    }

    @Override
    public boolean copyAndReplace(String destination) {
        return delegate.copyAndReplace(destination);
    }

    @Override
    public boolean copyAndReplace(String destination, int database) {
        return delegate.copyAndReplace(destination, database);
    }

    @Override
    public boolean move(int database) {
        return delegate.move(database);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public boolean delete() {
        return delegate.delete();
    }

    // 以下从光标位置继续补充可能缺失的方法
    @Override
    public boolean renamenx(String newName) {
        return delegate.renamenx(newName);
    }

    @Override
    public boolean isExists() {
        return delegate.isExists();
    }

    @Override
    public Codec getCodec() {
        return delegate.getCodec();
    }

    @Override
    public int addListener(ObjectListener listener) {
        return delegate.addListener(listener);
    }

    @Override
    public void removeListener(int listenerId) {
        delegate.removeListener(listenerId);
    }

    @Override
    public boolean unlink() {
        return delegate.unlink();
    }

    @Override
    public void rename(String newName) {
        delegate.rename(newName);
    }


}
