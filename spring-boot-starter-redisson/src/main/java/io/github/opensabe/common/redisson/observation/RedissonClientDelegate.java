/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.redisson.observation;

import org.redisson.api.*;
import org.redisson.api.ExecutorOptions;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.MapCacheOptions;
import org.redisson.api.MapOptions;
import org.redisson.api.options.*;
import org.redisson.api.redisnode.BaseRedisNodes;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonCodec;
import org.redisson.config.Config;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 该类实现 RedissonClient 接口，将所有方法调用委托给内部的 RedissonClient 实例。
 */
public class RedissonClientDelegate implements RedissonClient {

    protected final RedissonClient delegate;

    /**
     * 构造函数，初始化委托的 RedissonClient 实例。
     *
     * @param delegate 要委托的 RedissonClient 实例
     */
    public RedissonClientDelegate(RedissonClient delegate) {
        this.delegate = delegate;
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
    public <V, L> RTimeSeries<V, L> getTimeSeries(PlainOptions options) {
        return delegate.getTimeSeries(options);
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
    public <K, V> RStream<K, V> getStream(PlainOptions options) {
        return delegate.getStream(options);
    }

    @Override
    public RSearch getSearch() {
        return delegate.getSearch();
    }

    @Override
    public RSearch getSearch(Codec codec) {
        return delegate.getSearch(codec);
    }

    @Override
    public RSearch getSearch(OptionalOptions options) {
        return delegate.getSearch(options);
    }

    @Override
    public RRateLimiter getRateLimiter(String name) {
        return delegate.getRateLimiter(name);
    }

    @Override
    public RRateLimiter getRateLimiter(CommonOptions options) {
        return delegate.getRateLimiter(options);
    }

    @Override
    public RBinaryStream getBinaryStream(String name) {
        return delegate.getBinaryStream(name);
    }

    @Override
    public RBinaryStream getBinaryStream(CommonOptions options) {
        return delegate.getBinaryStream(options);
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
    public <V> RGeo<V> getGeo(PlainOptions options) {
        return delegate.getGeo(options);
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
    public <V> RSetCache<V> getSetCache(PlainOptions options) {
        return delegate.getSetCache(options);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec) {
        return delegate.getMapCache(name, codec);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec, MapCacheOptions<K, V> options) {
        return delegate.getMapCache(name, codec, options);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(org.redisson.api.options.MapCacheOptions<K, V> options) {
        return delegate.getMapCache(options);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name) {
        return delegate.getMapCache(name);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <K, V> RMapCache<K, V> getMapCache(String name, MapCacheOptions<K, V> options) {
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
    public <V> RBucket<V> getBucket(PlainOptions options) {
        return delegate.getBucket(options);
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
    public RBuckets getBuckets(OptionalOptions options) {
        return delegate.getBuckets(options);
    }

    @Override
    public <V> RJsonBucket<V> getJsonBucket(String name, JsonCodec codec) {
        return delegate.getJsonBucket(name, codec);
    }

    @Override
    public <V> RJsonBucket<V> getJsonBucket(JsonBucketOptions<V> options) {
        return delegate.getJsonBucket(options);
    }

    @Override
    public RJsonBuckets getJsonBuckets(JsonCodec codec) {
        return delegate.getJsonBuckets(codec);
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
    public <V> RHyperLogLog<V> getHyperLogLog(PlainOptions options) {
        return delegate.getHyperLogLog(options);
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
    public <V> RList<V> getList(PlainOptions options) {
        return delegate.getList(options);
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
    public <K, V> RListMultimap<K, V> getListMultimap(PlainOptions options) {
        return delegate.getListMultimap(options);
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
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(PlainOptions options) {
        return delegate.getListMultimapCache(options);
    }

    @Override
    public <K, V> RLocalCachedMapCache<K, V> getLocalCachedMapCache(String name, LocalCachedMapCacheOptions<K, V> options) {
        return delegate.getLocalCachedMapCache(name, options);
    }

    @Override
    public <K, V> RLocalCachedMapCache<K, V> getLocalCachedMapCache(String name, Codec codec, LocalCachedMapCacheOptions<K, V> options) {
        return delegate.getLocalCachedMapCache(name, codec, options);
    }

    @Override
    public <K, V> RListMultimapCacheNative<K, V> getListMultimapCacheNative(String name) {
        return delegate.getListMultimapCacheNative(name);
    }

    @Override
    public <K, V> RListMultimapCacheNative<K, V> getListMultimapCacheNative(String name, Codec codec) {
        return delegate.getListMultimapCacheNative(name, codec);
    }

    @Override
    public <K, V> RListMultimapCacheNative<K, V> getListMultimapCacheNative(PlainOptions options) {
        return delegate.getListMultimapCacheNative(options);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, LocalCachedMapOptions<K, V> options) {
        return delegate.getLocalCachedMap(name, options);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, Codec codec, LocalCachedMapOptions<K, V> options) {
        return delegate.getLocalCachedMap(name, codec, options);
    }

    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(org.redisson.api.options.LocalCachedMapOptions<K, V> options) {
        return delegate.getLocalCachedMap(options);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name) {
        return delegate.getMap(name);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <K, V> RMap<K, V> getMap(String name, MapOptions<K, V> options) {
        return delegate.getMap(name, options);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name, Codec codec) {
        return delegate.getMap(name, codec);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <K, V> RMap<K, V> getMap(String name, Codec codec, MapOptions<K, V> options) {
        return delegate.getMap(name, codec, options);
    }

    @Override
    public <K, V> RMap<K, V> getMap(org.redisson.api.options.MapOptions<K, V> options) {
        return delegate.getMap(options);
    }

    @Override
    public <K, V> RMapCacheNative<K, V> getMapCacheNative(String name) {
        return delegate.getMapCacheNative(name);
    }

    @Override
    public <K, V> RMapCacheNative<K, V> getMapCacheNative(String name, Codec codec) {
        return delegate.getMapCacheNative(name, codec);
    }

    @Override
    public <K, V> RMapCacheNative<K, V> getMapCacheNative(org.redisson.api.options.MapOptions<K, V> options) {
        return delegate.getMapCacheNative(options);
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
    public <K, V> RSetMultimap<K, V> getSetMultimap(PlainOptions options) {
        return delegate.getSetMultimap(options);
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
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(PlainOptions options) {
        return delegate.getSetMultimapCache(options);
    }

    @Override
    public <K, V> RSetMultimapCacheNative<K, V> getSetMultimapCacheNative(String name) {
        return delegate.getSetMultimapCacheNative(name);
    }

    @Override
    public <K, V> RSetMultimapCacheNative<K, V> getSetMultimapCacheNative(String name, Codec codec) {
        return delegate.getSetMultimapCacheNative(name, codec);
    }

    @Override
    public <K, V> RSetMultimapCacheNative<K, V> getSetMultimapCacheNative(PlainOptions options) {
        return delegate.getSetMultimapCacheNative(options);
    }

    @Override
    public RSemaphore getSemaphore(String name) {
        return delegate.getSemaphore(name);
    }

    @Override
    public RSemaphore getSemaphore(CommonOptions options) {
        return delegate.getSemaphore(options);
    }

    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(String name) {
        return delegate.getPermitExpirableSemaphore(name);
    }

    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(CommonOptions options) {
        return delegate.getPermitExpirableSemaphore(options);
    }

    @Override
    public RLock getLock(String name) {
        return delegate.getLock(name);
    }

    @Override
    public RLock getLock(CommonOptions options) {
        return delegate.getLock(options);
    }

    @Override
    public RLock getSpinLock(String name) {
        return delegate.getSpinLock(name);
    }

    @Override
    public RLock getSpinLock(String name, LockOptions.BackOff backOff) {
        return delegate.getSpinLock(name, backOff);
    }

    @Override
    public RFencedLock getFencedLock(String name) {
        return delegate.getFencedLock(name);
    }

    @Override
    public RFencedLock getFencedLock(CommonOptions options) {
        return delegate.getFencedLock(options);
    }

    @Override
    public RLock getMultiLock(RLock... locks) {
        return delegate.getMultiLock(locks);
    }

    @Override
    public RLock getMultiLock(String group, Collection<Object> values) {
        return delegate.getMultiLock(group, values);
    }

    @Override
    @SuppressWarnings("deprecation")
    public RLock getRedLock(RLock... locks) {
        return delegate.getRedLock(locks);
    }

    @Override
    public RLock getFairLock(String name) {
        return delegate.getFairLock(name);
    }

    @Override
    public RLock getFairLock(CommonOptions options) {
        return delegate.getFairLock(options);
    }

    @Override
    public RReadWriteLock getReadWriteLock(String name) {
        return delegate.getReadWriteLock(name);
    }

    @Override
    public RReadWriteLock getReadWriteLock(CommonOptions options) {
        return delegate.getReadWriteLock(options);
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
    public <V> RSet<V> getSet(PlainOptions options) {
        return delegate.getSet(options);
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
    public <V> RSortedSet<V> getSortedSet(PlainOptions options) {
        return delegate.getSortedSet(options);
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
    public <V> RScoredSortedSet<V> getScoredSortedSet(PlainOptions options) {
        return delegate.getScoredSortedSet(options);
    }

    @Override
    public RLexSortedSet getLexSortedSet(String name) {
        return delegate.getLexSortedSet(name);
    }

    @Override
    public RLexSortedSet getLexSortedSet(CommonOptions options) {
        return delegate.getLexSortedSet(options);
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
    public RShardedTopic getShardedTopic(PlainOptions options) {
        return delegate.getShardedTopic(options);
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
    public RTopic getTopic(PlainOptions options) {
        return delegate.getTopic(options);
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
    public RReliableTopic getReliableTopic(PlainOptions options) {
        return delegate.getReliableTopic(options);
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
    public RPatternTopic getPatternTopic(PatternTopicOptions options) {
        return delegate.getPatternTopic(options);
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
    public <V> RTransferQueue<V> getTransferQueue(PlainOptions options) {
        return delegate.getTransferQueue(options);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <V> RDelayedQueue<V> getDelayedQueue(RQueue<V> destinationQueue) {
        return delegate.getDelayedQueue(destinationQueue);
    }

    @Override
    public <V> RReliableQueue<V> getReliableQueue(String name) {
        return delegate.getReliableQueue(name);
    }

    @Override
    public <V> RReliableQueue<V> getReliableQueue(String name, Codec codec) {
        return delegate.getReliableQueue(name, codec);
    }

    @Override
    public <V> RReliableQueue<V> getReliableQueue(PlainOptions options) {
        return delegate.getReliableQueue(options);
    }

    @Override
    public <V> RQueue<V> getQueue(String name, Codec codec) {
        return delegate.getQueue(name, codec);
    }

    @Override
    public <V> RQueue<V> getQueue(PlainOptions options) {
        return delegate.getQueue(options);
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
    public <V> RRingBuffer<V> getRingBuffer(PlainOptions options) {
        return delegate.getRingBuffer(options);
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
    public <V> RPriorityQueue<V> getPriorityQueue(PlainOptions options) {
        return delegate.getPriorityQueue(options);
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
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(PlainOptions options) {
        return delegate.getPriorityBlockingQueue(options);
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
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(PlainOptions options) {
        return delegate.getPriorityBlockingDeque(options);
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
    public <V> RPriorityDeque<V> getPriorityDeque(PlainOptions options) {
        return delegate.getPriorityDeque(options);
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
    public <V> RBlockingQueue<V> getBlockingQueue(PlainOptions options) {
        return delegate.getBlockingQueue(options);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name) {
        return delegate.getBoundedBlockingQueue(name);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name, Codec codec) {
        return delegate.getBoundedBlockingQueue(name, codec);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(PlainOptions options) {
        return delegate.getBoundedBlockingQueue(options);
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
    public <V> RDeque<V> getDeque(PlainOptions options) {
        return delegate.getDeque(options);
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
    public <V> RBlockingDeque<V> getBlockingDeque(PlainOptions options) {
        return delegate.getBlockingDeque(options);
    }

    @Override
    public RAtomicLong getAtomicLong(String name) {
        return delegate.getAtomicLong(name);
    }

    @Override
    public RAtomicLong getAtomicLong(CommonOptions options) {
        return delegate.getAtomicLong(options);
    }

    @Override
    public RAtomicDouble getAtomicDouble(String name) {
        return delegate.getAtomicDouble(name);
    }

    @Override
    public RAtomicDouble getAtomicDouble(CommonOptions options) {
        return delegate.getAtomicDouble(options);
    }

    @Override
    public RLongAdder getLongAdder(String name) {
        return delegate.getLongAdder(name);
    }

    @Override
    public RLongAdder getLongAdder(CommonOptions options) {
        return delegate.getLongAdder(options);
    }

    @Override
    public RDoubleAdder getDoubleAdder(String name) {
        return delegate.getDoubleAdder(name);
    }

    @Override
    public RDoubleAdder getDoubleAdder(CommonOptions options) {
        return delegate.getDoubleAdder(options);
    }

    @Override
    public RCountDownLatch getCountDownLatch(String name) {
        return delegate.getCountDownLatch(name);
    }

    @Override
    public RCountDownLatch getCountDownLatch(CommonOptions options) {
        return delegate.getCountDownLatch(options);
    }

    @Override
    public RBitSet getBitSet(String name) {
        return delegate.getBitSet(name);
    }

    @Override
    public RBitSet getBitSet(CommonOptions options) {
        return delegate.getBitSet(options);
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
    public <V> RBloomFilter<V> getBloomFilter(PlainOptions options) {
        return delegate.getBloomFilter(options);
    }

    @Override
    public RIdGenerator getIdGenerator(String name) {
        return delegate.getIdGenerator(name);
    }

    @Override
    public RIdGenerator getIdGenerator(CommonOptions options) {
        return delegate.getIdGenerator(options);
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
    public RFunction getFunction(OptionalOptions options) {
        return delegate.getFunction(options);
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
    public RScript getScript(OptionalOptions options) {
        return delegate.getScript(options);
    }

    @Override
    public RVectorSet getVectorSet(String name) {
        return delegate.getVectorSet(name);
    }

    @Override
    public RVectorSet getVectorSet(CommonOptions options) {
        return delegate.getVectorSet(options);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name) {
        return delegate.getExecutorService(name);
    }

    @Override
    @SuppressWarnings("deprecation")
    public RScheduledExecutorService getExecutorService(String name, ExecutorOptions options) {
        return delegate.getExecutorService(name, options);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name, Codec codec) {
        return delegate.getExecutorService(name, codec);
    }

    @Override
    @SuppressWarnings("deprecation")
    public RScheduledExecutorService getExecutorService(String name, Codec codec, ExecutorOptions options) {
        return delegate.getExecutorService(name, codec, options);
    }

    @Override
    public RScheduledExecutorService getExecutorService(org.redisson.api.options.ExecutorOptions options) {
        return delegate.getExecutorService(options);
    }

    @Override
    @SuppressWarnings("deprecation")
    public RRemoteService getRemoteService() {
        return delegate.getRemoteService();
    }

    @Override
    @SuppressWarnings("deprecation")
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
    public RRemoteService getRemoteService(PlainOptions options) {
        return delegate.getRemoteService(options);
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
    public RKeys getKeys(KeysOptions options) {
        return delegate.getKeys(options);
    }

    @Override
    public RLiveObjectService getLiveObjectService() {
        return delegate.getLiveObjectService();
    }

    @Override
    public RLiveObjectService getLiveObjectService(LiveObjectOptions options) {
        return delegate.getLiveObjectService(options);
    }

    @Override
    public RClientSideCaching getClientSideCaching(ClientSideCachingOptions options) {
        return delegate.getClientSideCaching(options);
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
    @SuppressWarnings("deprecation")
    public NodesGroup<Node> getNodesGroup() {
        return delegate.getNodesGroup();
    }

    @Override
    @SuppressWarnings("deprecation")
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
