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

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.redisson.api.BatchOptions;
import org.redisson.api.ExecutorOptions;
import org.redisson.api.LocalCachedMapCacheOptions;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LockOptions;
import org.redisson.api.MapCacheOptions;
import org.redisson.api.MapOptions;
import org.redisson.api.RArray;
import org.redisson.api.RAtomicDouble;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBatch;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RBitSet;
import org.redisson.api.RBitVectorStore;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBloomFilterNative;
import org.redisson.api.RBoundedBlockingQueue;
import org.redisson.api.RBucket;
import org.redisson.api.RBuckets;
import org.redisson.api.RClientSideCaching;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RCuckooFilter;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RDeque;
import org.redisson.api.RDoubleAdder;
import org.redisson.api.RFencedLock;
import org.redisson.api.RFunction;
import org.redisson.api.RGcra;
import org.redisson.api.RGeo;
import org.redisson.api.RHyperLogLog;
import org.redisson.api.RIdGenerator;
import org.redisson.api.RJsonBucket;
import org.redisson.api.RJsonBuckets;
import org.redisson.api.RKeys;
import org.redisson.api.RLexSortedSet;
import org.redisson.api.RList;
import org.redisson.api.RListMultimap;
import org.redisson.api.RListMultimapCache;
import org.redisson.api.RListMultimapCacheNative;
import org.redisson.api.RLiveObjectService;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RLocalCachedMapCache;
import org.redisson.api.RLock;
import org.redisson.api.RLongAdder;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RMapCacheNative;
import org.redisson.api.RPatternTopic;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RPriorityBlockingDeque;
import org.redisson.api.RPriorityBlockingQueue;
import org.redisson.api.RPriorityDeque;
import org.redisson.api.RPriorityQueue;
import org.redisson.api.RQueue;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RReliablePubSubTopic;
import org.redisson.api.RReliableQueue;
import org.redisson.api.RReliableTopic;
import org.redisson.api.RRemoteService;
import org.redisson.api.RRingBuffer;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScript;
import org.redisson.api.RSearch;
import org.redisson.api.RSemaphore;
import org.redisson.api.RSet;
import org.redisson.api.RSetCache;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RSetMultimapCache;
import org.redisson.api.RSetMultimapCacheNative;
import org.redisson.api.RShardedTopic;
import org.redisson.api.RSortedSet;
import org.redisson.api.RStream;
import org.redisson.api.RTDigest;
import org.redisson.api.RTimeSeries;
import org.redisson.api.RTopK;
import org.redisson.api.RTopic;
import org.redisson.api.RTransaction;
import org.redisson.api.RTransferQueue;
import org.redisson.api.RVectorSet;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.RedissonRxClient;
import org.redisson.api.TransactionOptions;
import org.redisson.api.options.ClientSideCachingOptions;
import org.redisson.api.options.CommonOptions;
import org.redisson.api.options.JsonBucketOptions;
import org.redisson.api.options.KeysOptions;
import org.redisson.api.options.LiveObjectOptions;
import org.redisson.api.options.OptionalOptions;
import org.redisson.api.options.PatternTopicOptions;
import org.redisson.api.options.PlainOptions;
import org.redisson.api.redisnode.BaseRedisNodes;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonCodec;
import org.redisson.config.Config;


/**
 * {@link RedissonClient} 的委托实现，将所有 API 调用转发至底层客户端实例。
 * <p>
 * 供 {@link ObservedRedissonClient} 等装饰器在保留完整 Redisson 能力的同时注入观测逻辑。
 */
public class RedissonClientDelegate implements RedissonClient {

    /** 被委托的底层 {@link RedissonClient} 实例。 */
    protected final RedissonClient delegate;

    /**
     * 使用指定底层客户端构造委托实例。
     *
     * @param delegate 底层 Redisson 客户端，不可为 {@code null}
     */
    public RedissonClientDelegate(RedissonClient delegate) {
        this.delegate = delegate;
    }

    /**
     * 委托底层客户端执行：获取 TimeSeries。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V, L> RTimeSeries<V, L> getTimeSeries(String name) {
        return delegate.getTimeSeries(name);
    }

    /**
     * 委托底层客户端执行：获取 TimeSeries。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V, L> RTimeSeries<V, L> getTimeSeries(String name, Codec codec) {
        return delegate.getTimeSeries(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 TimeSeries。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V, L> RTimeSeries<V, L> getTimeSeries(PlainOptions options) {
        return delegate.getTimeSeries(options);
    }

    /**
     * 委托底层客户端执行：获取 Stream。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RStream<K, V> getStream(String name) {
        return delegate.getStream(name);
    }

    /**
     * 委托底层客户端执行：获取 Stream。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RStream<K, V> getStream(String name, Codec codec) {
        return delegate.getStream(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 Stream。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RStream<K, V> getStream(PlainOptions options) {
        return delegate.getStream(options);
    }

    /**
     * 委托底层客户端执行：获取 Search。
     *
     * @return 底层方法返回值
     */
    @Override
    public RSearch getSearch() {
        return delegate.getSearch();
    }

    /**
     * 委托底层客户端执行：获取 Search。
     *
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public RSearch getSearch(Codec codec) {
        return delegate.getSearch(codec);
    }

    /**
     * 委托底层客户端执行：获取 Search。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RSearch getSearch(OptionalOptions options) {
        return delegate.getSearch(options);
    }

    /**
     * 委托底层客户端执行：获取 RateLimiter。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RRateLimiter getRateLimiter(String name) {
        return delegate.getRateLimiter(name);
    }

    /**
     * 委托底层客户端执行：获取 RateLimiter。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RRateLimiter getRateLimiter(CommonOptions options) {
        return delegate.getRateLimiter(options);
    }

    /**
     * 委托底层客户端执行：获取 BinaryStream。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RBinaryStream getBinaryStream(String name) {
        return delegate.getBinaryStream(name);
    }

    /**
     * 委托底层客户端执行：获取 BinaryStream。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RBinaryStream getBinaryStream(CommonOptions options) {
        return delegate.getBinaryStream(options);
    }

    /**
     * 委托底层客户端执行：获取 Geo。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RGeo<V> getGeo(String name) {
        return delegate.getGeo(name);
    }

    /**
     * 委托底层客户端执行：获取 Geo。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RGeo<V> getGeo(String name, Codec codec) {
        return delegate.getGeo(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 Geo。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RGeo<V> getGeo(PlainOptions options) {
        return delegate.getGeo(options);
    }

    /**
     * 委托底层客户端执行：获取 SetCache。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RSetCache<V> getSetCache(String name) {
        return delegate.getSetCache(name);
    }

    /**
     * 委托底层客户端执行：获取 SetCache。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RSetCache<V> getSetCache(String name, Codec codec) {
        return delegate.getSetCache(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 SetCache。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RSetCache<V> getSetCache(PlainOptions options) {
        return delegate.getSetCache(options);
    }

    /**
     * 委托底层客户端执行：获取 MapCache。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec) {
        return delegate.getMapCache(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 MapCache。
     *
     * @param <K 参数
     * @param RMapCache<K 参数
     * @param name 参数
     * @param codec 参数
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec, MapCacheOptions<K, V> options) {
        return delegate.getMapCache(name, codec, options);
    }

    /**
     * 委托底层客户端执行：获取 MapCache。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RMapCache<K, V> getMapCache(org.redisson.api.options.MapCacheOptions<K, V> options) {
        return delegate.getMapCache(options);
    }

    /**
     * 委托底层客户端执行：获取 MapCache。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name) {
        return delegate.getMapCache(name);
    }

    /**
     * 委托底层客户端执行：获取 MapCache。
     *
     * @param <K 参数
     * @param RMapCache<K 参数
     * @param name 参数
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public <K, V> RMapCache<K, V> getMapCache(String name, MapCacheOptions<K, V> options) {
        return delegate.getMapCache(name, options);
    }

    /**
     * 委托底层客户端执行：获取 Bucket。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBucket<V> getBucket(String name) {
        return delegate.getBucket(name);
    }

    /**
     * 委托底层客户端执行：获取 Bucket。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBucket<V> getBucket(String name, Codec codec) {
        return delegate.getBucket(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 Bucket。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBucket<V> getBucket(PlainOptions options) {
        return delegate.getBucket(options);
    }

    /**
     * 委托底层客户端执行：获取 Buckets。
     *
     * @return 底层方法返回值
     */
    @Override
    public RBuckets getBuckets() {
        return delegate.getBuckets();
    }

    /**
     * 委托底层客户端执行：获取 Buckets。
     *
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public RBuckets getBuckets(Codec codec) {
        return delegate.getBuckets(codec);
    }

    /**
     * 委托底层客户端执行：获取 Buckets。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RBuckets getBuckets(OptionalOptions options) {
        return delegate.getBuckets(options);
    }

    /**
     * 委托底层客户端执行：获取 JsonBucket。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RJsonBucket<V> getJsonBucket(String name, JsonCodec codec) {
        return delegate.getJsonBucket(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 JsonBucket。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RJsonBucket<V> getJsonBucket(JsonBucketOptions<V> options) {
        return delegate.getJsonBucket(options);
    }

    /**
     * 委托底层客户端执行：获取 JsonBuckets。
     *
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public RJsonBuckets getJsonBuckets(JsonCodec codec) {
        return delegate.getJsonBuckets(codec);
    }

    /**
     * 委托底层客户端执行：获取 HyperLogLog。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RHyperLogLog<V> getHyperLogLog(String name) {
        return delegate.getHyperLogLog(name);
    }

    /**
     * 委托底层客户端执行：获取 HyperLogLog。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RHyperLogLog<V> getHyperLogLog(String name, Codec codec) {
        return delegate.getHyperLogLog(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 HyperLogLog。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RHyperLogLog<V> getHyperLogLog(PlainOptions options) {
        return delegate.getHyperLogLog(options);
    }

    /**
     * 委托底层客户端执行：获取 List。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RList<V> getList(String name) {
        return delegate.getList(name);
    }

    /**
     * 委托底层客户端执行：获取 List。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RList<V> getList(String name, Codec codec) {
        return delegate.getList(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 List。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RList<V> getList(PlainOptions options) {
        return delegate.getList(options);
    }

    /**
     * 委托底层客户端执行：获取 ListMultimap。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RListMultimap<K, V> getListMultimap(String name) {
        return delegate.getListMultimap(name);
    }

    /**
     * 委托底层客户端执行：获取 ListMultimap。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RListMultimap<K, V> getListMultimap(String name, Codec codec) {
        return delegate.getListMultimap(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 ListMultimap。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RListMultimap<K, V> getListMultimap(PlainOptions options) {
        return delegate.getListMultimap(options);
    }

    /**
     * 委托底层客户端执行：获取 ListMultimapCache。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name) {
        return delegate.getListMultimapCache(name);
    }

    /**
     * 委托底层客户端执行：获取 ListMultimapCache。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name, Codec codec) {
        return delegate.getListMultimapCache(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 ListMultimapCache。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(PlainOptions options) {
        return delegate.getListMultimapCache(options);
    }

    /**
     * 委托底层客户端执行：获取 LocalCachedMapCache。
     *
     * @param name 参数
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RLocalCachedMapCache<K, V> getLocalCachedMapCache(String name, LocalCachedMapCacheOptions<K, V> options) {
        return delegate.getLocalCachedMapCache(name, options);
    }

    /**
     * 委托底层客户端执行：获取 LocalCachedMapCache。
     *
     * @param name 参数
     * @param codec 参数
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RLocalCachedMapCache<K, V> getLocalCachedMapCache(String name, Codec codec, LocalCachedMapCacheOptions<K, V> options) {
        return delegate.getLocalCachedMapCache(name, codec, options);
    }

    /**
     * 委托底层客户端执行：获取 ListMultimapCacheNative。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RListMultimapCacheNative<K, V> getListMultimapCacheNative(String name) {
        return delegate.getListMultimapCacheNative(name);
    }

    /**
     * 委托底层客户端执行：获取 ListMultimapCacheNative。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RListMultimapCacheNative<K, V> getListMultimapCacheNative(String name, Codec codec) {
        return delegate.getListMultimapCacheNative(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 ListMultimapCacheNative。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RListMultimapCacheNative<K, V> getListMultimapCacheNative(PlainOptions options) {
        return delegate.getListMultimapCacheNative(options);
    }

    /**
     * 委托底层客户端执行：获取 LocalCachedMap。
     *
     * @param <K 参数
     * @param RLocalCachedMap<K 参数
     * @param name 参数
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, LocalCachedMapOptions<K, V> options) {
        return delegate.getLocalCachedMap(name, options);
    }

    /**
     * 委托底层客户端执行：获取 LocalCachedMap。
     *
     * @param <K 参数
     * @param RLocalCachedMap<K 参数
     * @param name 参数
     * @param codec 参数
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, Codec codec, LocalCachedMapOptions<K, V> options) {
        return delegate.getLocalCachedMap(name, codec, options);
    }

    /**
     * 委托底层客户端执行：获取 LocalCachedMap。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(org.redisson.api.options.LocalCachedMapOptions<K, V> options) {
        return delegate.getLocalCachedMap(options);
    }

    /**
     * 委托底层客户端执行：获取 Map。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RMap<K, V> getMap(String name) {
        return delegate.getMap(name);
    }

    /**
     * 委托底层客户端执行：获取 Map。
     *
     * @param <K 参数
     * @param RMap<K 参数
     * @param name 参数
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public <K, V> RMap<K, V> getMap(String name, MapOptions<K, V> options) {
        return delegate.getMap(name, options);
    }

    /**
     * 委托底层客户端执行：获取 Map。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RMap<K, V> getMap(String name, Codec codec) {
        return delegate.getMap(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 Map。
     *
     * @param <K 参数
     * @param RMap<K 参数
     * @param name 参数
     * @param codec 参数
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public <K, V> RMap<K, V> getMap(String name, Codec codec, MapOptions<K, V> options) {
        return delegate.getMap(name, codec, options);
    }

    /**
     * 委托底层客户端执行：获取 Map。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RMap<K, V> getMap(org.redisson.api.options.MapOptions<K, V> options) {
        return delegate.getMap(options);
    }

    /**
     * 委托底层客户端执行：获取 MapCacheNative。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RMapCacheNative<K, V> getMapCacheNative(String name) {
        return delegate.getMapCacheNative(name);
    }

    /**
     * 委托底层客户端执行：获取 MapCacheNative。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RMapCacheNative<K, V> getMapCacheNative(String name, Codec codec) {
        return delegate.getMapCacheNative(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 MapCacheNative。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RMapCacheNative<K, V> getMapCacheNative(org.redisson.api.options.MapOptions<K, V> options) {
        return delegate.getMapCacheNative(options);
    }

    /**
     * 委托底层客户端执行：获取 SetMultimap。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RSetMultimap<K, V> getSetMultimap(String name) {
        return delegate.getSetMultimap(name);
    }

    /**
     * 委托底层客户端执行：获取 SetMultimap。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RSetMultimap<K, V> getSetMultimap(String name, Codec codec) {
        return delegate.getSetMultimap(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 SetMultimap。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RSetMultimap<K, V> getSetMultimap(PlainOptions options) {
        return delegate.getSetMultimap(options);
    }

    /**
     * 委托底层客户端执行：获取 SetMultimapCache。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name) {
        return delegate.getSetMultimapCache(name);
    }

    /**
     * 委托底层客户端执行：获取 SetMultimapCache。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name, Codec codec) {
        return delegate.getSetMultimapCache(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 SetMultimapCache。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(PlainOptions options) {
        return delegate.getSetMultimapCache(options);
    }

    /**
     * 委托底层客户端执行：获取 SetMultimapCacheNative。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RSetMultimapCacheNative<K, V> getSetMultimapCacheNative(String name) {
        return delegate.getSetMultimapCacheNative(name);
    }

    /**
     * 委托底层客户端执行：获取 SetMultimapCacheNative。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RSetMultimapCacheNative<K, V> getSetMultimapCacheNative(String name, Codec codec) {
        return delegate.getSetMultimapCacheNative(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 SetMultimapCacheNative。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K, V> RSetMultimapCacheNative<K, V> getSetMultimapCacheNative(PlainOptions options) {
        return delegate.getSetMultimapCacheNative(options);
    }

    /**
     * 委托底层客户端执行：获取 Semaphore。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RSemaphore getSemaphore(String name) {
        return delegate.getSemaphore(name);
    }

    /**
     * 委托底层客户端执行：获取 Semaphore。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RSemaphore getSemaphore(CommonOptions options) {
        return delegate.getSemaphore(options);
    }

    /**
     * 委托底层客户端执行：获取 PermitExpirableSemaphore。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(String name) {
        return delegate.getPermitExpirableSemaphore(name);
    }

    /**
     * 委托底层客户端执行：获取 PermitExpirableSemaphore。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(CommonOptions options) {
        return delegate.getPermitExpirableSemaphore(options);
    }

    /**
     * 委托底层客户端执行：获取 Lock。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RLock getLock(String name) {
        return delegate.getLock(name);
    }

    /**
     * 委托底层客户端执行：获取 Lock。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RLock getLock(CommonOptions options) {
        return delegate.getLock(options);
    }

    /**
     * 委托底层客户端执行：获取 SpinLock。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RLock getSpinLock(String name) {
        return delegate.getSpinLock(name);
    }

    /**
     * 委托底层客户端执行：获取 SpinLock。
     *
     * @param name 参数
     * @param backOff 参数
     * @return 底层方法返回值
     */
    @Override
    public RLock getSpinLock(String name, LockOptions.BackOff backOff) {
        return delegate.getSpinLock(name, backOff);
    }

    /**
     * 委托底层客户端执行：获取 FencedLock。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RFencedLock getFencedLock(String name) {
        return delegate.getFencedLock(name);
    }

    /**
     * 委托底层客户端执行：获取 FencedLock。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RFencedLock getFencedLock(CommonOptions options) {
        return delegate.getFencedLock(options);
    }

    /**
     * 委托底层客户端执行：获取 MultiLock。
     *
     * @param locks 参数
     * @return 底层方法返回值
     */
    @Override
    public RLock getMultiLock(RLock... locks) {
        return delegate.getMultiLock(locks);
    }

    /**
     * 委托底层客户端执行：获取 MultiLock。
     *
     * @param group 参数
     * @param values 参数
     * @return 底层方法返回值
     */
    @Override
    public RLock getMultiLock(String group, Collection<Object> values) {
        return delegate.getMultiLock(group, values);
    }

    /**
     * 委托底层客户端执行：获取 RedLock。
     *
     * @param locks 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public RLock getRedLock(RLock... locks) {
        return delegate.getRedLock(locks);
    }

    /**
     * 委托底层客户端执行：获取 FairLock。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RLock getFairLock(String name) {
        return delegate.getFairLock(name);
    }

    /**
     * 委托底层客户端执行：获取 FairLock。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RLock getFairLock(CommonOptions options) {
        return delegate.getFairLock(options);
    }

    /**
     * 委托底层客户端执行：获取 ReadWriteLock。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RReadWriteLock getReadWriteLock(String name) {
        return delegate.getReadWriteLock(name);
    }

    /**
     * 委托底层客户端执行：获取 ReadWriteLock。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RReadWriteLock getReadWriteLock(CommonOptions options) {
        return delegate.getReadWriteLock(options);
    }

    /**
     * 委托底层客户端执行：获取 Set。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RSet<V> getSet(String name) {
        return delegate.getSet(name);
    }

    /**
     * 委托底层客户端执行：获取 Set。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RSet<V> getSet(String name, Codec codec) {
        return delegate.getSet(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 Set。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RSet<V> getSet(PlainOptions options) {
        return delegate.getSet(options);
    }

    /**
     * 委托底层客户端执行：获取 SortedSet。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RSortedSet<V> getSortedSet(String name) {
        return delegate.getSortedSet(name);
    }

    /**
     * 委托底层客户端执行：获取 SortedSet。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RSortedSet<V> getSortedSet(String name, Codec codec) {
        return delegate.getSortedSet(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 SortedSet。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RSortedSet<V> getSortedSet(PlainOptions options) {
        return delegate.getSortedSet(options);
    }

    /**
     * 委托底层客户端执行：获取 ScoredSortedSet。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RScoredSortedSet<V> getScoredSortedSet(String name) {
        return delegate.getScoredSortedSet(name);
    }

    /**
     * 委托底层客户端执行：获取 ScoredSortedSet。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RScoredSortedSet<V> getScoredSortedSet(String name, Codec codec) {
        return delegate.getScoredSortedSet(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 ScoredSortedSet。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RScoredSortedSet<V> getScoredSortedSet(PlainOptions options) {
        return delegate.getScoredSortedSet(options);
    }

    /**
     * 委托底层客户端执行：获取 LexSortedSet。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RLexSortedSet getLexSortedSet(String name) {
        return delegate.getLexSortedSet(name);
    }

    /**
     * 委托底层客户端执行：获取 LexSortedSet。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RLexSortedSet getLexSortedSet(CommonOptions options) {
        return delegate.getLexSortedSet(options);
    }

    /**
     * 委托底层客户端执行：获取 ShardedTopic。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RShardedTopic getShardedTopic(String name) {
        return delegate.getShardedTopic(name);
    }

    /**
     * 委托底层客户端执行：获取 ShardedTopic。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public RShardedTopic getShardedTopic(String name, Codec codec) {
        return delegate.getShardedTopic(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 ShardedTopic。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RShardedTopic getShardedTopic(PlainOptions options) {
        return delegate.getShardedTopic(options);
    }

    /**
     * 委托底层客户端执行：获取 Topic。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RTopic getTopic(String name) {
        return delegate.getTopic(name);
    }

    /**
     * 委托底层客户端执行：获取 Topic。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public RTopic getTopic(String name, Codec codec) {
        return delegate.getTopic(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 Topic。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RTopic getTopic(PlainOptions options) {
        return delegate.getTopic(options);
    }

    /**
     * 委托底层客户端执行：获取 ReliableTopic。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RReliableTopic getReliableTopic(String name) {
        return delegate.getReliableTopic(name);
    }

    /**
     * 委托底层客户端执行：获取 ReliableTopic。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public RReliableTopic getReliableTopic(String name, Codec codec) {
        return delegate.getReliableTopic(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 ReliableTopic。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RReliableTopic getReliableTopic(PlainOptions options) {
        return delegate.getReliableTopic(options);
    }

    /**
     * 委托底层客户端执行：获取 PatternTopic。
     *
     * @param pattern 参数
     * @return 底层方法返回值
     */
    @Override
    public RPatternTopic getPatternTopic(String pattern) {
        return delegate.getPatternTopic(pattern);
    }

    /**
     * 委托底层客户端执行：获取 PatternTopic。
     *
     * @param pattern 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public RPatternTopic getPatternTopic(String pattern, Codec codec) {
        return delegate.getPatternTopic(pattern, codec);
    }

    /**
     * 委托底层客户端执行：获取 PatternTopic。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RPatternTopic getPatternTopic(PatternTopicOptions options) {
        return delegate.getPatternTopic(options);
    }

    /**
     * 委托底层客户端执行：获取 Queue。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RQueue<V> getQueue(String name) {
        return delegate.getQueue(name);
    }

    /**
     * 委托底层客户端执行：获取 TransferQueue。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RTransferQueue<V> getTransferQueue(String name) {
        return delegate.getTransferQueue(name);
    }

    /**
     * 委托底层客户端执行：获取 TransferQueue。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RTransferQueue<V> getTransferQueue(String name, Codec codec) {
        return delegate.getTransferQueue(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 TransferQueue。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RTransferQueue<V> getTransferQueue(PlainOptions options) {
        return delegate.getTransferQueue(options);
    }

    /**
     * 委托底层客户端执行：获取 DelayedQueue。
     *
     * @param destinationQueue 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public <V> RDelayedQueue<V> getDelayedQueue(RQueue<V> destinationQueue) {
        return delegate.getDelayedQueue(destinationQueue);
    }

    /**
     * 委托底层客户端执行：获取 ReliableQueue。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RReliableQueue<V> getReliableQueue(String name) {
        return delegate.getReliableQueue(name);
    }

    /**
     * 委托底层客户端执行：获取 ReliableQueue。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RReliableQueue<V> getReliableQueue(String name, Codec codec) {
        return delegate.getReliableQueue(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 ReliableQueue。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RReliableQueue<V> getReliableQueue(PlainOptions options) {
        return delegate.getReliableQueue(options);
    }

    /**
     * 委托底层客户端执行：获取 Queue。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RQueue<V> getQueue(String name, Codec codec) {
        return delegate.getQueue(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 Queue。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RQueue<V> getQueue(PlainOptions options) {
        return delegate.getQueue(options);
    }

    /**
     * 委托底层客户端执行：获取 RingBuffer。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RRingBuffer<V> getRingBuffer(String name) {
        return delegate.getRingBuffer(name);
    }

    /**
     * 委托底层客户端执行：获取 RingBuffer。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RRingBuffer<V> getRingBuffer(String name, Codec codec) {
        return delegate.getRingBuffer(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 RingBuffer。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RRingBuffer<V> getRingBuffer(PlainOptions options) {
        return delegate.getRingBuffer(options);
    }

    /**
     * 委托底层客户端执行：获取 PriorityQueue。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RPriorityQueue<V> getPriorityQueue(String name) {
        return delegate.getPriorityQueue(name);
    }

    /**
     * 委托底层客户端执行：获取 PriorityQueue。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RPriorityQueue<V> getPriorityQueue(String name, Codec codec) {
        return delegate.getPriorityQueue(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 PriorityQueue。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RPriorityQueue<V> getPriorityQueue(PlainOptions options) {
        return delegate.getPriorityQueue(options);
    }

    /**
     * 委托底层客户端执行：获取 PriorityBlockingQueue。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name) {
        return delegate.getPriorityBlockingQueue(name);
    }

    /**
     * 委托底层客户端执行：获取 PriorityBlockingQueue。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name, Codec codec) {
        return delegate.getPriorityBlockingQueue(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 PriorityBlockingQueue。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(PlainOptions options) {
        return delegate.getPriorityBlockingQueue(options);
    }

    /**
     * 委托底层客户端执行：获取 PriorityBlockingDeque。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name) {
        return delegate.getPriorityBlockingDeque(name);
    }

    /**
     * 委托底层客户端执行：获取 PriorityBlockingDeque。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name, Codec codec) {
        return delegate.getPriorityBlockingDeque(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 PriorityBlockingDeque。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(PlainOptions options) {
        return delegate.getPriorityBlockingDeque(options);
    }

    /**
     * 委托底层客户端执行：获取 PriorityDeque。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RPriorityDeque<V> getPriorityDeque(String name) {
        return delegate.getPriorityDeque(name);
    }

    /**
     * 委托底层客户端执行：获取 PriorityDeque。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RPriorityDeque<V> getPriorityDeque(String name, Codec codec) {
        return delegate.getPriorityDeque(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 PriorityDeque。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RPriorityDeque<V> getPriorityDeque(PlainOptions options) {
        return delegate.getPriorityDeque(options);
    }

    /**
     * 委托底层客户端执行：获取 BlockingQueue。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBlockingQueue<V> getBlockingQueue(String name) {
        return delegate.getBlockingQueue(name);
    }

    /**
     * 委托底层客户端执行：获取 BlockingQueue。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBlockingQueue<V> getBlockingQueue(String name, Codec codec) {
        return delegate.getBlockingQueue(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 BlockingQueue。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBlockingQueue<V> getBlockingQueue(PlainOptions options) {
        return delegate.getBlockingQueue(options);
    }

    /**
     * 委托底层客户端执行：获取 BoundedBlockingQueue。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name) {
        return delegate.getBoundedBlockingQueue(name);
    }

    /**
     * 委托底层客户端执行：获取 BoundedBlockingQueue。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name, Codec codec) {
        return delegate.getBoundedBlockingQueue(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 BoundedBlockingQueue。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(PlainOptions options) {
        return delegate.getBoundedBlockingQueue(options);
    }

    /**
     * 委托底层客户端执行：获取 Deque。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RDeque<V> getDeque(String name) {
        return delegate.getDeque(name);
    }

    /**
     * 委托底层客户端执行：获取 Deque。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RDeque<V> getDeque(String name, Codec codec) {
        return delegate.getDeque(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 Deque。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RDeque<V> getDeque(PlainOptions options) {
        return delegate.getDeque(options);
    }

    /**
     * 委托底层客户端执行：获取 BlockingDeque。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBlockingDeque<V> getBlockingDeque(String name) {
        return delegate.getBlockingDeque(name);
    }

    /**
     * 委托底层客户端执行：获取 BlockingDeque。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBlockingDeque<V> getBlockingDeque(String name, Codec codec) {
        return delegate.getBlockingDeque(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 BlockingDeque。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBlockingDeque<V> getBlockingDeque(PlainOptions options) {
        return delegate.getBlockingDeque(options);
    }

    /**
     * 委托底层客户端执行：获取 AtomicLong。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RAtomicLong getAtomicLong(String name) {
        return delegate.getAtomicLong(name);
    }

    /**
     * 委托底层客户端执行：获取 AtomicLong。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RAtomicLong getAtomicLong(CommonOptions options) {
        return delegate.getAtomicLong(options);
    }

    /**
     * 委托底层客户端执行：获取 AtomicDouble。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RAtomicDouble getAtomicDouble(String name) {
        return delegate.getAtomicDouble(name);
    }

    /**
     * 委托底层客户端执行：获取 AtomicDouble。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RAtomicDouble getAtomicDouble(CommonOptions options) {
        return delegate.getAtomicDouble(options);
    }

    /**
     * 委托底层客户端执行：获取 LongAdder。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RLongAdder getLongAdder(String name) {
        return delegate.getLongAdder(name);
    }

    /**
     * 委托底层客户端执行：获取 LongAdder。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RLongAdder getLongAdder(CommonOptions options) {
        return delegate.getLongAdder(options);
    }

    /**
     * 委托底层客户端执行：获取 DoubleAdder。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RDoubleAdder getDoubleAdder(String name) {
        return delegate.getDoubleAdder(name);
    }

    /**
     * 委托底层客户端执行：获取 DoubleAdder。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RDoubleAdder getDoubleAdder(CommonOptions options) {
        return delegate.getDoubleAdder(options);
    }

    /**
     * 委托底层客户端执行：获取 CountDownLatch。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RCountDownLatch getCountDownLatch(String name) {
        return delegate.getCountDownLatch(name);
    }

    /**
     * 委托底层客户端执行：获取 CountDownLatch。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RCountDownLatch getCountDownLatch(CommonOptions options) {
        return delegate.getCountDownLatch(options);
    }

    /**
     * 委托底层客户端执行：获取 BitSet。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RBitSet getBitSet(String name) {
        return delegate.getBitSet(name);
    }

    /**
     * 委托底层客户端执行：获取 BitSet。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RBitSet getBitSet(CommonOptions options) {
        return delegate.getBitSet(options);
    }

    /**
     * 委托底层客户端执行：获取 BloomFilter。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBloomFilter<V> getBloomFilter(String name) {
        return delegate.getBloomFilter(name);
    }

    /**
     * 委托底层客户端执行：获取 BloomFilter。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBloomFilter<V> getBloomFilter(String name, Codec codec) {
        return delegate.getBloomFilter(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 BloomFilter。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBloomFilter<V> getBloomFilter(PlainOptions options) {
        return delegate.getBloomFilter(options);
    }

    /**
     * 委托底层客户端执行：获取 TDigest。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RTDigest getTDigest(String name) {
        return delegate.getTDigest(name);
    }

    /**
     * 委托底层客户端执行：获取 TDigest。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RTDigest getTDigest(PlainOptions options) {
        return delegate.getTDigest(options);
    }

    /**
     * 委托底层客户端执行：获取 IdGenerator。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RIdGenerator getIdGenerator(String name) {
        return delegate.getIdGenerator(name);
    }

    /**
     * 委托底层客户端执行：获取 IdGenerator。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RIdGenerator getIdGenerator(CommonOptions options) {
        return delegate.getIdGenerator(options);
    }

    /**
     * 委托底层客户端执行：获取 Function。
     *
     * @return 底层方法返回值
     */
    @Override
    public RFunction getFunction() {
        return delegate.getFunction();
    }

    /**
     * 委托底层客户端执行：获取 Function。
     *
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public RFunction getFunction(Codec codec) {
        return delegate.getFunction(codec);
    }

    /**
     * 委托底层客户端执行：获取 Function。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RFunction getFunction(OptionalOptions options) {
        return delegate.getFunction(options);
    }

    /**
     * 委托底层客户端执行：获取 Script。
     *
     * @return 底层方法返回值
     */
    @Override
    public RScript getScript() {
        return delegate.getScript();
    }

    /**
     * 委托底层客户端执行：获取 Script。
     *
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public RScript getScript(Codec codec) {
        return delegate.getScript(codec);
    }

    /**
     * 委托底层客户端执行：获取 Script。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RScript getScript(OptionalOptions options) {
        return delegate.getScript(options);
    }

    /**
     * 委托底层客户端执行：获取 VectorSet。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RVectorSet getVectorSet(String name) {
        return delegate.getVectorSet(name);
    }

    /**
     * 委托底层客户端执行：获取 VectorSet。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RVectorSet getVectorSet(CommonOptions options) {
        return delegate.getVectorSet(options);
    }

    /**
     * 委托底层客户端执行：获取 ExecutorService。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RScheduledExecutorService getExecutorService(String name) {
        return delegate.getExecutorService(name);
    }

    /**
     * 委托底层客户端执行：获取 ExecutorService。
     *
     * @param name 参数
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public RScheduledExecutorService getExecutorService(String name, ExecutorOptions options) {
        return delegate.getExecutorService(name, options);
    }

    /**
     * 委托底层客户端执行：获取 ExecutorService。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public RScheduledExecutorService getExecutorService(String name, Codec codec) {
        return delegate.getExecutorService(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 ExecutorService。
     *
     * @param name 参数
     * @param codec 参数
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public RScheduledExecutorService getExecutorService(String name, Codec codec, ExecutorOptions options) {
        return delegate.getExecutorService(name, codec, options);
    }

    /**
     * 委托底层客户端执行：获取 ExecutorService。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RScheduledExecutorService getExecutorService(org.redisson.api.options.ExecutorOptions options) {
        return delegate.getExecutorService(options);
    }

    /**
     * 委托底层客户端执行：获取 RemoteService。
     *
     * @param getRemoteService( 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public RRemoteService getRemoteService() {
        return delegate.getRemoteService();
    }

    /**
     * 委托底层客户端执行：获取 RemoteService。
     *
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    @SuppressWarnings("deprecation")
    public RRemoteService getRemoteService(Codec codec) {
        return delegate.getRemoteService(codec);
    }

    /**
     * 委托底层客户端执行：获取 RemoteService。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RRemoteService getRemoteService(String name) {
        return delegate.getRemoteService(name);
    }

    /**
     * 委托底层客户端执行：获取 RemoteService。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public RRemoteService getRemoteService(String name, Codec codec) {
        return delegate.getRemoteService(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 RemoteService。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RRemoteService getRemoteService(PlainOptions options) {
        return delegate.getRemoteService(options);
    }

    /**
     * 委托底层客户端执行：创建 Transaction。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RTransaction createTransaction(TransactionOptions options) {
        return delegate.createTransaction(options);
    }

    /**
     * 委托底层客户端执行：创建 Batch。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RBatch createBatch(BatchOptions options) {
        return delegate.createBatch(options);
    }

    /**
     * 委托底层客户端执行：创建 Batch。
     *
     * @return 底层方法返回值
     */
    @Override
    public RBatch createBatch() {
        return delegate.createBatch();
    }

    /**
     * 委托底层客户端执行：获取 Keys。
     *
     * @return 底层方法返回值
     */
    @Override
    public RKeys getKeys() {
        return delegate.getKeys();
    }

    /**
     * 委托底层客户端执行：获取 Keys。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RKeys getKeys(KeysOptions options) {
        return delegate.getKeys(options);
    }

    /**
     * 委托底层客户端执行：获取 LiveObjectService。
     *
     * @return 底层方法返回值
     */
    @Override
    public RLiveObjectService getLiveObjectService() {
        return delegate.getLiveObjectService();
    }

    /**
     * 委托底层客户端执行：获取 LiveObjectService。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RLiveObjectService getLiveObjectService(LiveObjectOptions options) {
        return delegate.getLiveObjectService(options);
    }

    /**
     * 委托底层客户端执行：获取 ClientSideCaching。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RClientSideCaching getClientSideCaching(ClientSideCachingOptions options) {
        return delegate.getClientSideCaching(options);
    }

    /**
     * 委托底层客户端执行：调用 rxJava。
     *
     * @return 底层方法返回值
     */
    @Override
    public RedissonRxClient rxJava() {
        return delegate.rxJava();
    }

    /**
     * 委托底层客户端执行：调用 reactive。
     *
     * @return 底层方法返回值
     */
    @Override
    public RedissonReactiveClient reactive() {
        return delegate.reactive();
    }

    /**
     * 委托底层客户端执行：获取 Array。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RArray<V> getArray(String name) {
        return delegate.getArray(name);
    }

    /**
     * 委托底层客户端执行：获取 Array。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RArray<V> getArray(String name, Codec codec) {
        return delegate.getArray(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 Array。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RArray<V> getArray(PlainOptions options) {
        return delegate.getArray(options);
    }

    /**
     * 委托底层客户端执行：获取 Gcra。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RGcra getGcra(String name) {
        return delegate.getGcra(name);
    }

    /**
     * 委托底层客户端执行：获取 Gcra。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RGcra getGcra(CommonOptions options) {
        return delegate.getGcra(options);
    }

    /**
     * 委托底层客户端执行：获取 NonReentrantLock。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RLock getNonReentrantLock(String name) {
        return delegate.getNonReentrantLock(name);
    }

    /**
     * 委托底层客户端执行：获取 NonReentrantLock。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RLock getNonReentrantLock(CommonOptions options) {
        return delegate.getNonReentrantLock(options);
    }

    /**
     * 委托底层客户端执行：获取 NonReentrantFairLock。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public RLock getNonReentrantFairLock(String name) {
        return delegate.getNonReentrantFairLock(name);
    }

    /**
     * 委托底层客户端执行：获取 NonReentrantFairLock。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public RLock getNonReentrantFairLock(CommonOptions options) {
        return delegate.getNonReentrantFairLock(options);
    }

    /**
     * 委托底层客户端执行：获取 ReliablePubSubTopic。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RReliablePubSubTopic<V> getReliablePubSubTopic(String name) {
        return delegate.getReliablePubSubTopic(name);
    }

    /**
     * 委托底层客户端执行：获取 ReliablePubSubTopic。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RReliablePubSubTopic<V> getReliablePubSubTopic(String name, Codec codec) {
        return delegate.getReliablePubSubTopic(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 ReliablePubSubTopic。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RReliablePubSubTopic<V> getReliablePubSubTopic(PlainOptions options) {
        return delegate.getReliablePubSubTopic(options);
    }

    /**
     * 委托底层客户端执行：获取 BitVectorStore。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <K> RBitVectorStore<K> getBitVectorStore(String name) {
        return delegate.getBitVectorStore(name);
    }

    /**
     * 委托底层客户端执行：获取 BitVectorStore。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <K> RBitVectorStore<K> getBitVectorStore(String name, Codec codec) {
        return delegate.getBitVectorStore(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 BitVectorStore。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <K> RBitVectorStore<K> getBitVectorStore(PlainOptions options) {
        return delegate.getBitVectorStore(options);
    }

    /**
     * 委托底层客户端执行：获取 BloomFilterNative。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBloomFilterNative<V> getBloomFilterNative(String name) {
        return delegate.getBloomFilterNative(name);
    }

    /**
     * 委托底层客户端执行：获取 BloomFilterNative。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBloomFilterNative<V> getBloomFilterNative(String name, Codec codec) {
        return delegate.getBloomFilterNative(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 BloomFilterNative。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RBloomFilterNative<V> getBloomFilterNative(PlainOptions options) {
        return delegate.getBloomFilterNative(options);
    }

    /**
     * 委托底层客户端执行：获取 CuckooFilter。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RCuckooFilter<V> getCuckooFilter(String name) {
        return delegate.getCuckooFilter(name);
    }

    /**
     * 委托底层客户端执行：获取 CuckooFilter。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RCuckooFilter<V> getCuckooFilter(String name, Codec codec) {
        return delegate.getCuckooFilter(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 CuckooFilter。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RCuckooFilter<V> getCuckooFilter(PlainOptions options) {
        return delegate.getCuckooFilter(options);
    }

    /**
     * 委托底层客户端执行：获取 TopK。
     *
     * @param name 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RTopK<V> getTopK(String name) {
        return delegate.getTopK(name);
    }

    /**
     * 委托底层客户端执行：获取 TopK。
     *
     * @param name 参数
     * @param codec 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RTopK<V> getTopK(String name, Codec codec) {
        return delegate.getTopK(name, codec);
    }

    /**
     * 委托底层客户端执行：获取 TopK。
     *
     * @param options 参数
     * @return 底层方法返回值
     */
    @Override
    public <V> RTopK<V> getTopK(PlainOptions options) {
        return delegate.getTopK(options);
    }

    /**
     * 委托底层客户端执行：调用 shutdown。
     *
     */
    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    /**
     * 委托底层客户端执行：关闭 Async。
     *
     * @return 底层方法返回值
     */
    @Override
    public java.util.concurrent.CompletionStage<Void> shutdownAsync() {
        return delegate.shutdownAsync();
    }

    /**
     * 委托底层客户端执行：调用 shutdown。
     *
     * @param quietPeriod 参数
     * @param timeout 参数
     * @param unit 参数
     */
    @Override
    public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {
        delegate.shutdown(quietPeriod, timeout, unit);
    }

    /**
     * 委托底层客户端执行：关闭 Async。
     *
     * @param quietPeriod 参数
     * @param timeout 参数
     * @return 底层方法返回值
     */
    @Override
    public java.util.concurrent.CompletionStage<Void> shutdownAsync(java.time.Duration quietPeriod, java.time.Duration timeout) {
        return delegate.shutdownAsync(quietPeriod, timeout);
    }

    /**
     * 委托底层客户端执行：获取 Config。
     *
     * @return 底层方法返回值
     */
    @Override
    public Config getConfig() {
        return delegate.getConfig();
    }

    /**
     * 委托底层客户端执行：获取 RedisNodes。
     *
     * @param nodes 参数
     * @return 底层方法返回值
     */
    @Override
    public <T extends BaseRedisNodes> T getRedisNodes(RedisNodes<T> nodes) {
        return delegate.getRedisNodes(nodes);
    }

    /**
     * 委托底层客户端执行：判断 Shutdown。
     *
     * @return 底层方法返回值
     */
    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    /**
     * 委托底层客户端执行：判断 ShuttingDown。
     *
     * @return 底层方法返回值
     */
    @Override
    public boolean isShuttingDown() {
        return delegate.isShuttingDown();
    }

    /**
     * 委托底层客户端执行：获取 Id。
     *
     * @return 底层方法返回值
     */
    @Override
    public String getId() {
        return delegate.getId();
    }
}
