package io.github.opensabe.common.redisson.observation;

import io.github.opensabe.common.redisson.observation.ratelimiter.ObservedRRateLimiter;
import io.github.opensabe.common.redisson.observation.rlock.ObservedRLock;
import io.github.opensabe.common.redisson.observation.rlock.ObservedRReadWriteLock;
import io.github.opensabe.common.redisson.observation.rsemaphore.ObservedRPermitExpirableSemaphore;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import org.redisson.api.*;
import org.redisson.api.redisnode.BaseRedisNodes;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonCodec;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class ObservedRedissonClient implements RedissonClient {
    private final RedissonClient delegate;
    private final UnifiedObservationFactory unifiedObservationFactory;

    public ObservedRedissonClient(RedissonClient delegate, UnifiedObservationFactory unifiedObservationFactory) {
        this.delegate = delegate;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Override
    public <V, L> RTimeSeries<V, L> getTimeSeries(String name) {
        return delegate.getTimeSeries(name);
    }

    @Override
    public <V, L> RTimeSeries<V, L> getTimeSeries(String name, Codec codec) {
        return delegate.getTimeSeries(name, codec);
    }

    @Override
    public <K, V> RStream<K, V> getStream(String name) {
        return delegate.getStream(name);
    }

    @Override
    public <K, V> RStream<K, V> getStream(String name, Codec codec) {
        return delegate.getStream(name, codec);
    }

    @Override
    public RRateLimiter getRateLimiter(String name) {
        return new ObservedRRateLimiter(
                delegate.getRateLimiter(name), unifiedObservationFactory
        );
    }

    @Override
    public RBinaryStream getBinaryStream(String name) {
        return delegate.getBinaryStream(name);
    }

    @Override
    public <V> RGeo<V> getGeo(String name) {
        return delegate.getGeo(name);
    }

    @Override
    public <V> RGeo<V> getGeo(String name, Codec codec) {
        return delegate.getGeo(name, codec);
    }

    @Override
    public <V> RSetCache<V> getSetCache(String name) {
        return delegate.getSetCache(name);
    }

    @Override
    public <V> RSetCache<V> getSetCache(String name, Codec codec) {
        return delegate.getSetCache(name, codec);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec) {
        return delegate.getMapCache(name, codec);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec, MapOptions<K, V> options) {
        return delegate.getMapCache(name, codec, options);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name) {
        return delegate.getMapCache(name);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, MapOptions<K, V> options) {
        return delegate.getMapCache(name, options);
    }

    @Override
    public <V> RBucket<V> getBucket(String name) {
        return delegate.getBucket(name);
    }

    @Override
    public <V> RBucket<V> getBucket(String name, Codec codec) {
        return delegate.getBucket(name, codec);
    }

    @Override
    public RBuckets getBuckets() {
        return delegate.getBuckets();
    }

    @Override
    public RBuckets getBuckets(Codec codec) {
        return delegate.getBuckets(codec);
    }

    @Override
    public <V> RJsonBucket<V> getJsonBucket(String name, JsonCodec<V> codec) {
        return delegate.getJsonBucket(name, codec);
    }

    @Override
    public <V> RHyperLogLog<V> getHyperLogLog(String name) {
        return delegate.getHyperLogLog(name);
    }

    @Override
    public <V> RHyperLogLog<V> getHyperLogLog(String name, Codec codec) {
        return delegate.getHyperLogLog(name, codec);
    }

    @Override
    public <V> RList<V> getList(String name) {
        return delegate.getList(name);
    }

    @Override
    public <V> RList<V> getList(String name, Codec codec) {
        return delegate.getList(name, codec);
    }

    @Override
    public <K, V> RListMultimap<K, V> getListMultimap(String name) {
        return delegate.getListMultimap(name);
    }

    @Override
    public <K, V> RListMultimap<K, V> getListMultimap(String name, Codec codec) {
        return delegate.getListMultimap(name, codec);
    }

    @Override
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name) {
        return delegate.getListMultimapCache(name);
    }

    @Override
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name, Codec codec) {
        return delegate.getListMultimapCache(name, codec);
    }

    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, LocalCachedMapOptions<K, V> options) {
        return delegate.getLocalCachedMap(name, options);
    }

    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, Codec codec, LocalCachedMapOptions<K, V> options) {
        return delegate.getLocalCachedMap(name, codec, options);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name) {
        return delegate.getMap(name);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name, MapOptions<K, V> options) {
        return delegate.getMap(name, options);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name, Codec codec) {
        return delegate.getMap(name, codec);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name, Codec codec, MapOptions<K, V> options) {
        return delegate.getMap(name, codec, options);
    }

    @Override
    public <K, V> RSetMultimap<K, V> getSetMultimap(String name) {
        return delegate.getSetMultimap(name);
    }

    @Override
    public <K, V> RSetMultimap<K, V> getSetMultimap(String name, Codec codec) {
        return delegate.getSetMultimap(name, codec);
    }

    @Override
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name) {
        return delegate.getSetMultimapCache(name);
    }

    @Override
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name, Codec codec) {
        return delegate.getSetMultimapCache(name, codec);
    }

    @Override
    public RSemaphore getSemaphore(String name) {
        return delegate.getSemaphore(name);
    }

    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(String name) {
        return new ObservedRPermitExpirableSemaphore(
                delegate.getPermitExpirableSemaphore(name), unifiedObservationFactory
        );
    }

    private RLock getObservedLock(RLock delegate) {
        return new ObservedRLock(delegate, unifiedObservationFactory);
    }

    @Override
    public RLock getLock(String name) {
        return getObservedLock(delegate.getLock(name));
    }

    @Override
    public RLock getSpinLock(String name) {
        return getObservedLock(delegate.getSpinLock(name));
    }

    @Override
    public RLock getSpinLock(String name, LockOptions.BackOff backOff) {
        return getObservedLock(delegate.getSpinLock(name, backOff));
    }

    @Override
    public RLock getMultiLock(RLock... locks) {
        return getObservedLock(delegate.getMultiLock(locks));
    }

    @Override
    public RLock getRedLock(RLock... locks) {
        return getObservedLock(delegate.getRedLock(locks));
    }

    @Override
    public RLock getFairLock(String name) {
        return getObservedLock(delegate.getFairLock(name));
    }

    @Override
    public RReadWriteLock getReadWriteLock(String name) {
        return new ObservedRReadWriteLock(delegate.getReadWriteLock(name), unifiedObservationFactory);
    }

    @Override
    public <V> RSet<V> getSet(String name) {
        return delegate.getSet(name);
    }

    @Override
    public <V> RSet<V> getSet(String name, Codec codec) {
        return delegate.getSet(name, codec);
    }

    @Override
    public <V> RSortedSet<V> getSortedSet(String name) {
        return delegate.getSortedSet(name);
    }

    @Override
    public <V> RSortedSet<V> getSortedSet(String name, Codec codec) {
        return delegate.getSortedSet(name, codec);
    }

    @Override
    public <V> RScoredSortedSet<V> getScoredSortedSet(String name) {
        return delegate.getScoredSortedSet(name);
    }

    @Override
    public <V> RScoredSortedSet<V> getScoredSortedSet(String name, Codec codec) {
        return delegate.getScoredSortedSet(name, codec);
    }

    @Override
    public RLexSortedSet getLexSortedSet(String name) {
        return delegate.getLexSortedSet(name);
    }

    @Override
    public RShardedTopic getShardedTopic(String name) {
        return delegate.getShardedTopic(name);
    }

    @Override
    public RShardedTopic getShardedTopic(String name, Codec codec) {
        return delegate.getShardedTopic(name, codec);
    }

    @Override
    public RTopic getTopic(String name) {
        return delegate.getTopic(name);
    }

    @Override
    public RTopic getTopic(String name, Codec codec) {
        return delegate.getTopic(name, codec);
    }

    @Override
    public RReliableTopic getReliableTopic(String name) {
        return delegate.getReliableTopic(name);
    }

    @Override
    public RReliableTopic getReliableTopic(String name, Codec codec) {
        return delegate.getReliableTopic(name, codec);
    }

    @Override
    public RPatternTopic getPatternTopic(String pattern) {
        return delegate.getPatternTopic(pattern);
    }

    @Override
    public RPatternTopic getPatternTopic(String pattern, Codec codec) {
        return delegate.getPatternTopic(pattern, codec);
    }

    @Override
    public <V> RQueue<V> getQueue(String name) {
        return delegate.getQueue(name);
    }

    @Override
    public <V> RTransferQueue<V> getTransferQueue(String name) {
        return delegate.getTransferQueue(name);
    }

    @Override
    public <V> RTransferQueue<V> getTransferQueue(String name, Codec codec) {
        return delegate.getTransferQueue(name, codec);
    }

    @Override
    public <V> RDelayedQueue<V> getDelayedQueue(RQueue<V> destinationQueue) {
        return delegate.getDelayedQueue(destinationQueue);
    }

    @Override
    public <V> RQueue<V> getQueue(String name, Codec codec) {
        return delegate.getQueue(name, codec);
    }

    @Override
    public <V> RRingBuffer<V> getRingBuffer(String name) {
        return delegate.getRingBuffer(name);
    }

    @Override
    public <V> RRingBuffer<V> getRingBuffer(String name, Codec codec) {
        return delegate.getRingBuffer(name, codec);
    }

    @Override
    public <V> RPriorityQueue<V> getPriorityQueue(String name) {
        return delegate.getPriorityQueue(name);
    }

    @Override
    public <V> RPriorityQueue<V> getPriorityQueue(String name, Codec codec) {
        return delegate.getPriorityQueue(name, codec);
    }

    @Override
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name) {
        return delegate.getPriorityBlockingQueue(name);
    }

    @Override
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name, Codec codec) {
        return delegate.getPriorityBlockingQueue(name, codec);
    }

    @Override
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name) {
        return delegate.getPriorityBlockingDeque(name);
    }

    @Override
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name, Codec codec) {
        return delegate.getPriorityBlockingDeque(name, codec);
    }

    @Override
    public <V> RPriorityDeque<V> getPriorityDeque(String name) {
        return delegate.getPriorityDeque(name);
    }

    @Override
    public <V> RPriorityDeque<V> getPriorityDeque(String name, Codec codec) {
        return delegate.getPriorityDeque(name, codec);
    }

    @Override
    public <V> RBlockingQueue<V> getBlockingQueue(String name) {
        return delegate.getBlockingQueue(name);
    }

    @Override
    public <V> RBlockingQueue<V> getBlockingQueue(String name, Codec codec) {
        return delegate.getBlockingQueue(name, codec);
    }

    @Override
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name) {
        return delegate.getBoundedBlockingQueue(name);
    }

    @Override
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name, Codec codec) {
        return delegate.getBoundedBlockingQueue(name, codec);
    }

    @Override
    public <V> RDeque<V> getDeque(String name) {
        return delegate.getDeque(name);
    }

    @Override
    public <V> RDeque<V> getDeque(String name, Codec codec) {
        return delegate.getDeque(name, codec);
    }

    @Override
    public <V> RBlockingDeque<V> getBlockingDeque(String name) {
        return delegate.getBlockingDeque(name);
    }

    @Override
    public <V> RBlockingDeque<V> getBlockingDeque(String name, Codec codec) {
        return delegate.getBlockingDeque(name, codec);
    }

    @Override
    public RAtomicLong getAtomicLong(String name) {
        return delegate.getAtomicLong(name);
    }

    @Override
    public RAtomicDouble getAtomicDouble(String name) {
        return delegate.getAtomicDouble(name);
    }

    @Override
    public RLongAdder getLongAdder(String name) {
        return delegate.getLongAdder(name);
    }

    @Override
    public RDoubleAdder getDoubleAdder(String name) {
        return delegate.getDoubleAdder(name);
    }

    @Override
    public RCountDownLatch getCountDownLatch(String name) {
        return delegate.getCountDownLatch(name);
    }

    @Override
    public RBitSet getBitSet(String name) {
        return delegate.getBitSet(name);
    }

    @Override
    public <V> RBloomFilter<V> getBloomFilter(String name) {
        return delegate.getBloomFilter(name);
    }

    @Override
    public <V> RBloomFilter<V> getBloomFilter(String name, Codec codec) {
        return delegate.getBloomFilter(name, codec);
    }

    @Override
    public RIdGenerator getIdGenerator(String name) {
        return delegate.getIdGenerator(name);
    }

    @Override
    public RFunction getFunction() {
        return delegate.getFunction();
    }

    @Override
    public RFunction getFunction(Codec codec) {
        return delegate.getFunction(codec);
    }

    @Override
    public RScript getScript() {
        return delegate.getScript();
    }

    @Override
    public RScript getScript(Codec codec) {
        return delegate.getScript(codec);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name) {
        return delegate.getExecutorService(name);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name, ExecutorOptions options) {
        return delegate.getExecutorService(name, options);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name, Codec codec) {
        return delegate.getExecutorService(name, codec);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name, Codec codec, ExecutorOptions options) {
        return delegate.getExecutorService(name, codec, options);
    }

    @Override
    public RRemoteService getRemoteService() {
        return delegate.getRemoteService();
    }

    @Override
    public RRemoteService getRemoteService(Codec codec) {
        return delegate.getRemoteService(codec);
    }

    @Override
    public RRemoteService getRemoteService(String name) {
        return delegate.getRemoteService(name);
    }

    @Override
    public RRemoteService getRemoteService(String name, Codec codec) {
        return delegate.getRemoteService(name, codec);
    }

    @Override
    public RTransaction createTransaction(TransactionOptions options) {
        return delegate.createTransaction(options);
    }

    @Override
    public RBatch createBatch(BatchOptions options) {
        return delegate.createBatch(options);
    }

    @Override
    public RBatch createBatch() {
        return delegate.createBatch();
    }

    @Override
    public RKeys getKeys() {
        return delegate.getKeys();
    }

    @Override
    public RLiveObjectService getLiveObjectService() {
        return delegate.getLiveObjectService();
    }

    @Override
    public RedissonRxClient rxJava() {
        return delegate.rxJava();
    }

    @Override
    public RedissonReactiveClient reactive() {
        return delegate.reactive();
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {
        delegate.shutdown(quietPeriod, timeout, unit);
    }

    @Override
    public Config getConfig() {
        return delegate.getConfig();
    }

    @Override
    public <T extends BaseRedisNodes> T getRedisNodes(RedisNodes<T> nodes) {
        return delegate.getRedisNodes(nodes);
    }

    @Override
    public NodesGroup<Node> getNodesGroup() {
        return delegate.getNodesGroup();
    }

    @Override
    public ClusterNodesGroup getClusterNodesGroup() {
        return delegate.getClusterNodesGroup();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isShuttingDown() {
        return delegate.isShuttingDown();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }
}
