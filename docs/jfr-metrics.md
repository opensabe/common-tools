# JFR → Prometheus metrics（只消费）

`spring-framework-parent-common` 提供 **被动** JFR 事件消费：挂到进程已有的 **磁盘** Flight Recorder 仓库（`EventStream.openRepository`），聚合成有界 Top-N Gauge，供 Prometheus / Grafana 使用。

## 硬约束

- **不**启动 JFR，**不** `enable` / `disable` / 改 period、threshold、throttle、stackTrace
- 进程必须自己录制：`-XX:StartFlightRecording=...`（默认 `disk=true`）或 `jcmd JFR.start`
- `disk=false` 纯内存录制 → 本模块无数据（JEP 349 不支持消费内存录制）

## 最小开启

```yaml
opensabe:
  jfr:
    metrics:
      enabled: true
```

并保证 JVM 已有磁盘录制，例如：

```text
-XX:StartFlightRecording=name=continuous,settings=default,maxage=15m
```

默认 `enabled=false`：升级无额外消费开销。

## 开关一览

| 配置 | 作用 |
|---|---|
| `opensabe.jfr.metrics.enabled` | 全局：关则零消费线程 / 不打开 EventStream |
| `consume.<event>.enabled` | 不订阅该事件（不解析 chunk 中的该类事件） |
| `circuit-breaker.*` | 消费侧速率熔断：退订 handler 或关流，**不改** JVM 录制 |

完整示例见 [jfr-metrics-example.yml](jfr-metrics-example.yml)。

## default.jfc 与面板为空

| 事件 | default.jfc | 指标 |
|---|---|---|
| `jdk.ObjectAllocationSample` | 开（约 150/s） | `jfr_alloc_sample_weight_bytes_by_class` / `_by_method` |
| `jdk.ExecutionSample` | 开 | `jfr_execution_samples_by_method` |
| `jdk.JavaMonitorEnter` | 开（常 ≥20ms） | `jfr_monitor_enter_wait_milliseconds` / `_count` |
| `jdk.OldObjectSample` | 开（默认无栈） | `jfr_old_object_samples_by_class`，`jfr_old_object_heap_bytes` |
| `jdk.ObjectAllocationOutsideTLAB` | **关** | `jfr_outside_tlab_bytes_by_class` |
| `jdk.AllocationRequiringGC` | **关** | `jfr_alloc_requiring_gc_bytes_by_method` / `_max_` |
| `jdk.ZAllocationStall` | 开 | `jfr_z_allocation_stall_seconds`（启用后指标会注册；非 ZGC / 窗口无事件时为 **0**，0 不代表一定没有停顿） |

面板空时先看：

- `jfr_metrics_repository_attached` — 是否挂上磁盘仓库
- `jfr_metrics_event_seen{event=...}` — 窗口内是否见到该事件
- `jfr_metrics_circuit_open{event=...}` — 消费是否被熔断

## 进程侧可选（本模块不代开）

对齐大对象 / AllocationRequiringGC 排障：

```text
-XX:StartFlightRecording:settings=default,jdk.ObjectAllocationOutsideTLAB#enabled=true,jdk.AllocationRequiringGC#enabled=true
```

泄漏栈（可选）：`memory-leaks=stack-traces`。避免常开 `memory-leaks=gc-roots`。

## 语义注意

- Sample 聚合的是 **weight**（分配压力），不是每次 `new` 的精确 size
- `AllocationRequiringGC.size` 可能是 **TLAB 申请量**，事件 **无** objectClass
- `OldObjectSample.lastKnownHeapUsage` 是采样时 **全局堆** 用量，禁止按 class SUM；按类只用 sample count
- OutsideTLAB 的 `min-size` 只过滤聚合，**降低不了** JVM 记事件成本

## 防雪崩

压力大时危险的是消费回调分配。熔断只停消费：

1. 单事件超过 `max-per-second` → 丢弃并退订该 handler  
2. 合计超过 `global-max-per-second` 或 ≥2 个事件熔断 → 关闭 EventStream，冷却后再挂  
3. 聚合 map 硬顶 `top-n * 4`

若进程 jfc 打开了 OutsideTLAB / InNewTLAB，JVM 记事件成本本模块停不掉。

## Grafana

导入 [grafana/jfr-runtime.json](grafana/jfr-runtime.json)。PromQL 示例：

```promql
topk(20, jfr_alloc_sample_weight_bytes_by_class)
topk(20, jfr_execution_samples_by_method)
topk(20, jfr_monitor_enter_wait_milliseconds / 1000)
```
