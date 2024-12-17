package io.github.opensabe.common.redisson.observation.robject;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import org.redisson.api.ObjectListener;
import org.redisson.api.RFuture;
import org.redisson.api.RObject;
import org.redisson.client.codec.Codec;

import java.util.concurrent.TimeUnit;

public class ObservedRObject implements RObject {
    private final RObject rObject;
    protected final UnifiedObservationFactory unifiedObservationFactory;

    public ObservedRObject(RObject rObject, UnifiedObservationFactory unifiedObservationFactory) {
        this.rObject = rObject;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Override
    public Long getIdleTime() {
        return rObject.getIdleTime();
    }

    @Override
    public long sizeInMemory() {
        return rObject.sizeInMemory();
    }

    @Override
    public void restore(byte[] state) {
        rObject.restore(state);
    }

    @Override
    public void restore(byte[] state, long timeToLive, TimeUnit timeUnit) {
        rObject.restore(state, timeToLive, timeUnit);
    }

    @Override
    public void restoreAndReplace(byte[] state) {
        rObject.restoreAndReplace(state);
    }

    @Override
    public void restoreAndReplace(byte[] state, long timeToLive, TimeUnit timeUnit) {
        rObject.restoreAndReplace(state, timeToLive, timeUnit);
    }

    @Override
    public byte[] dump() {
        return rObject.dump();
    }

    @Override
    public boolean touch() {
        return rObject.touch();
    }

    @Override
    public void migrate(String host, int port, int database, long timeout) {
        rObject.migrate(host, port, database, timeout);
    }

    @Override
    public void copy(String host, int port, int database, long timeout) {
        rObject.copy(host, port, database, timeout);
    }

    @Override
    public boolean copy(String destination) {
        return rObject.copy(destination);
    }

    @Override
    public boolean copy(String destination, int database) {
        return rObject.copy(destination,database);
    }

    @Override
    public boolean copyAndReplace(String destination) {
        return rObject.copyAndReplace(destination);
    }

    @Override
    public boolean copyAndReplace(String destination, int database) {
        return rObject.copyAndReplace(destination,database);
    }

    @Override
    public boolean move(int database) {
        return rObject.move(database);
    }

    @Override
    public String getName() {
        return rObject.getName();
    }

    @Override
    public boolean delete() {
        return rObject.delete();
    }

    @Override
    public boolean unlink() {
        return rObject.unlink();
    }

    @Override
    public void rename(String newName) {
        rObject.rename(newName);
    }

    @Override
    public boolean renamenx(String newName) {
        return rObject.renamenx(newName);
    }

    @Override
    public boolean isExists() {
        return rObject.isExists();
    }

    @Override
    public Codec getCodec() {
        return rObject.getCodec();
    }

    @Override
    public int addListener(ObjectListener listener) {
        return rObject.addListener(listener);
    }

    @Override
    public void removeListener(int listenerId) {
        rObject.removeListener(listenerId);
    }

    @Override
    public RFuture<Long> getIdleTimeAsync() {
        return rObject.getIdleTimeAsync();
    }

    @Override
    public RFuture<Long> sizeInMemoryAsync() {
        return rObject.sizeInMemoryAsync();
    }

    @Override
    public RFuture<Void> restoreAsync(byte[] state) {
        return rObject.restoreAsync(state);
    }

    @Override
    public RFuture<Void> restoreAsync(byte[] state, long timeToLive, TimeUnit timeUnit) {
        return rObject.restoreAsync(state, timeToLive, timeUnit);
    }

    @Override
    public RFuture<Void> restoreAndReplaceAsync(byte[] state) {
        return rObject.restoreAndReplaceAsync(state);
    }

    @Override
    public RFuture<Void> restoreAndReplaceAsync(byte[] state, long timeToLive, TimeUnit timeUnit) {
        return rObject.restoreAndReplaceAsync(state, timeToLive, timeUnit);
    }

    @Override
    public RFuture<byte[]> dumpAsync() {
        return rObject.dumpAsync();
    }

    @Override
    public RFuture<Boolean> touchAsync() {
        return rObject.touchAsync();
    }

    @Override
    public RFuture<Void> migrateAsync(String host, int port, int database, long timeout) {
        return rObject.migrateAsync(host, port, database, timeout);
    }

    @Override
    public RFuture<Void> copyAsync(String host, int port, int database, long timeout) {
        return rObject.copyAsync(host, port, database, timeout);
    }

    @Override
    public RFuture<Boolean> copyAsync(String destination) {
        return rObject.copyAsync(destination);
    }

    @Override
    public RFuture<Boolean> copyAsync(String destination, int database) {
        return rObject.copyAsync(destination,database);
    }

    @Override
    public RFuture<Boolean> copyAndReplaceAsync(String destination) {
        return rObject.copyAndReplaceAsync(destination);
    }

    @Override
    public RFuture<Boolean> copyAndReplaceAsync(String destination, int database) {
        return rObject.copyAndReplaceAsync(destination,database);
    }

    @Override
    public RFuture<Boolean> moveAsync(int database) {
        return rObject.moveAsync(database);
    }

    @Override
    public RFuture<Boolean> deleteAsync() {
        return rObject.deleteAsync();
    }

    @Override
    public RFuture<Boolean> unlinkAsync() {
        return rObject.unlinkAsync();
    }

    @Override
    public RFuture<Void> renameAsync(String newName) {
        return rObject.renameAsync(newName);
    }

    @Override
    public RFuture<Boolean> renamenxAsync(String newName) {
        return rObject.renamenxAsync(newName);
    }

    @Override
    public RFuture<Boolean> isExistsAsync() {
        return rObject.isExistsAsync();
    }

    @Override
    public RFuture<Integer> addListenerAsync(ObjectListener listener) {
        return rObject.addListenerAsync(listener);
    }

    @Override
    public RFuture<Void> removeListenerAsync(int listenerId) {
        return rObject.removeListenerAsync(listenerId);
    }
}
