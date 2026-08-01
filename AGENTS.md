# AGENTS — common-tools

> AI：**Matchplay 业务 bug 优先在业务仓排查**；仅当缺陷定位到 starter 本身时再改本仓。**无 Matchplay Eureka**。

## 0. 形态

| 字段 | 取值 |
|---|---|
| packaging | `pom` aggregator |
| 对外 BOM | `spring-cloud-parent` 等 |
| 当前线 | **3.0.0-SNAPSHOT** = Spring Boot **4.1.0** + Spring Cloud **2025.1.2** + Java **25** |

## 1. 关键约束

- 版本发布影响 **所有** 引用 `be-matchplay-parent` → `spring-cloud-parent` 的服务。
- Starter 内勿耦合 Matchplay 业务常量。
- `3.0.0-SNAPSHOT` 为破坏性升级：消费者需同步 JDK 25、Boot 4.1、Cloud 2025.1.2；Jackson 一等公民为 **Jackson 3**（`tools.jackson`）；Undertow 已移除；ES 使用 Java API Client（`ElasticsearchClient`，非 HLRC）；Gateway artifact 为 `spring-cloud-starter-gateway-server-webflux`；AOP starter 为 `spring-boot-starter-aspectj`。
- ES：密钥过滤在 transport `Instrumentation`（`SecretFilteringInstrumentation`），于 HTTP 发出前拦截，故不会留下 HC5 observation CACHE 条目。已发出请求若无 response，observation 依赖 Caffeine 5min eviction `stop()`；`throwable` 标签目前几乎不写入。`ScriptedSearcher` 仅转发 index/preference，size/sort/aggs 等会丢（调用方勿依赖透传）。模块内 `CACHE` 与 `spring-boot-starter-cache` 无关。
- Redisson/MultiRedis：MultiRedis 经反射 `PropertiesDataRedisConnectionDetails(props, sslBundles.getIfAvailable())`，平台与 Virtual 双路径一致；`ssl.bundle` 依赖 Boot `SslBundles`（无专用 TLS IT）。`RedissonClient` 只吃 multi 的 **default** 条目（非 Lettuce 路由）。已删 `getNodesGroup`/`getClusterNodesGroup`（改 `getRedisNodes`）。同步与 **async** `RateLimiter` set/update/acquire/release 均经 `ObservedRRateLimiter`（async 在 `whenComplete` 收尾 observation）。
- 优雅关闭：SPI 为 `io.github.opensabe.common.executor.GracefulShutdownHandler`（位于 `spring-framework-parent-common`；原 `UndertowGracefulShutdownHandler` 仍保留为 deprecated 别名）。`UndertowGracefulShutdownInitializer` 现为 `SmartLifecycle`（phase 低于 Boot `WebServerGracefulShutdownLifecycle`），在 web 排空之后执行，勿改回 `ContextClosedEvent`。
- Cache：本库 Redis `CacheManager` 显式 `RedisCacheWriter.immediateWrites()`，恢复 Data Redis 3.x 同步可见语义（4.x + Lettuce 默认异步 put/evict）。无 `@Expire` 的 `@CacheEvict` 会清理该 name 下所有 TTL 变体（2.x 可能只清一个；见 `ExpireTest#testCacheEvictFansOutAcrossExpireTtls`）。`CompositedCache.get(key, Class)` 将 cached-null 当 miss（默认 `cacheNullValues=false` 时无影响）。`DynamicRedis/CaffeineCacheManager` 按 TTL 永久建 cache 为既有无界增长点。Redis cache 值序列化仍走裸 `GenericJackson2JsonRedisSerializer`（Jackson 2；与 HTTP Jackson 3 分离）；**默认不注册 JavaTimeModule**，`LocalDateTime` 缓存值需自定义 ObjectMapper（见 `GenericJackson2CacheWireContractTest`）。
- JsonUtil：standalone mapper 启用 `DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS` + `TimestampModule`，保持 LocalDateTime/Date 时间戳线格式。
- 边界 Jackson（非 HTTP，勿假定全域一致）：`JSONTypeHandler` 用独立最小 `JsonMapper`（仅关 `FAIL_ON_UNKNOWN_PROPERTIES`）→ DB JSON 日期偏 **ISO**，非 JsonUtil epoch-ms，亦无 `NON_NULL`。Dynamo/`S3JsonConverter`/MQ 信封与内层走 **JsonUtil**（Spring 可劫持 Boot `ObjectMapper`）。`BaseMQMessage.data`：**V1** 为 JSON 字符串、**V2** 为对象（`StringDeserializer` 将对象压成 compact 字符串供遗留 `AbstractMQConsumer`）。HTTP `JacksonUtil`：仅 `NON_NULL`，无 Timestamp 模块。不强制统一 mapper。
- 持久化线格式矩阵（兼容测）：JsonUtil=epoch-ms；业务 Redis 热路径多为 **Fastjson**（BOM `fastjson.version`）；starter Cache=`GenericJackson2JsonRedisSerializer`；`JSONTypeHandler`=ISO。Fastjson **跨小版本**勿双依赖进 classpath，用 test `IsolatedJarClassLoader` + `target/compat-libs`（`fastjson`+`fastjson2`+`fastjson2-extension`；路径经 `basedir`/test-classes sibling 解析，见 `FastjsonCompatBridge`）。JsonUtil↔Fastjson / MQ V1↔V2 契约见 `*WireContractTest`。
- MapStruct（opensabe `mapstruct-processor` 1.4.0）：在 Java 25 下 APT 可能生成残缺 `MapperRegisterImpl`；`common-utils` 测试侧跳过该 processor，手写静态 map 的 `MapperRepositoryImpl` + 最小 `MapperRegisterImpl`，并由 `MapstructTestBootstrap` 补齐 `@Binding` mapper。生产侧消费者若遇同类问题需升级 processor 或自行 register。
- Socket.IO 测试客户端：与 OTel/Boot 引入的 okhttp 5（`okhttp-jvm`）对齐，勿混用 socket.io-client 自带的 okhttp 4 + okio 3.6（会 `NoSuchMethodError: Okio.socket`）。
- 测试侧：`@MockBean` → `@MockitoBean`；`TestRestTemplate` 包为 `org.springframework.boot.resttestclient`，并需 `@AutoConfigureTestRestTemplate`；观测用 `@AutoConfigureTracing`（替代已移除的 `@AutoConfigureObservability`）。
- 协同：升级后需与 `be-matchplay-parent` 对齐 BOM / JDK 25（本仓版本为 **3.0.0-SNAPSHOT**）。
- AWS SDK：`aws.sdk.version`（当前 **2.50.2**）在 `spring-cloud-parent` 与 `aws-bom` 共用；S3 客户端显式 `RequestChecksumCalculation/ResponseChecksumValidation.WHEN_REQUIRED`（SDK 2.30+ 默认 CRC32，LocalStack/部分兼容端不支持）。DynamoDB Enhanced 自定义 schema 须传 `MethodHandles.Lookup` 给 `BeanAttributeGetter/Setter.create`。

## 1.1 延期独立里程碑（Batch C，勿与小版本 bump 混 PR）

| 项 | 当前 | 方向 | 阻塞/注意 |
|----|------|------|-----------|
| Protobuf | `netty-push-protobuf` **3.19.6** | 3.25.x 或 4.x | alive-protobuf 生成码与 wire 兼容 |
| LangChain4j Milvus | **0.32.0** | 1.x 线 | API 大跳，单独回归 |
| PayPal OpenAPI 客户端 | Jackson 2 + `jakarta.annotation` 1.3.5 | 重生或迁 Jackson 3 | 与 HTTP Jackson 3 双栈并存 |
| Cache Redis serializer | Jackson 2 `GenericJackson2` | 等 Spring Data Redis Jackson 3 正式路径 | **勿盲目统一** |
| opensabe `mapstruct-processor` | **1.4.0** | 等上游修 Java 25 APT | 已知 blocker |

## 2. 改动归属

→ 全 Matchplay Java 栈的基础设施面；需与 `be-matchplay-parent` 协同升级。
