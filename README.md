# common-tools — opensabe 公共 Starter 聚合仓

> **`io.github.opensabe-tech:common-tools`**：Spring Boot / Cloud Starter、`spring-cloud-parent`、`common-utils`、调度与推送相关模块的 **聚合发布仓库**。Matchplay 通过 **`be-matchplay-parent`** 间接对齐此 BOM。

## 1. 模块（节选）

见根 `pom.xml` `<modules>`：`spring-cloud-parent`、`common-utils`、`spring-boot-starter-mybatis`、`spring-boot-starter-redis` 等。

## 2. 本地构建（维护者）

```bash
cd common-tools
mvn clean install -DskipTests
```

完整 reactor 体量大；按需 `-pl` 编译单模块。

## 3. 上游

官方仓库：`https://github.com/opensabe/common-tools`（见 `pom.xml` `url`）。

## 4. 文档

- AI：[`AGENTS.md`](./AGENTS.md)
