# AGENTS — common-tools

> AI：**Matchplay 业务 bug 优先在业务仓排查**；仅当缺陷定位到 starter 本身时再改本仓。**无 Matchplay Eureka**。

## 0. 形态

| 字段 | 取值 |
|---|---|
| packaging | `pom` aggregator |
| 对外 BOM | `spring-cloud-parent` 等 |

## 1. 关键约束

- 版本发布影响 **所有** 引用 `be-matchplay-parent` → `spring-cloud-parent` 的服务。
- Starter 内勿耦合 Matchplay 业务常量。

## 2. 改动归属

→ 全 Matchplay Java 栈的基础设施面；需与 `be-matchplay-parent` 协同升级。
