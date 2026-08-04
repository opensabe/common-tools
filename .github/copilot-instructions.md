# common-tools — Copilot instructions

Maven `pom` aggregator publishing shared BOM and Spring Boot starters. Downstream services consume versions via the `spring-cloud-parent` BOM.

## Stack (3.0.0-SNAPSHOT)

- Java **25**, Spring Boot **4.1**, Spring Cloud **2025.1.2**
- HTTP JSON: **Jackson 3** (`tools.jackson`); Redis Cache serializer remains Jackson 2 `GenericJackson2JsonRedisSerializer`
- Starters must stay domain-agnostic: no product-specific constants, service names, or Eureka wiring

## Change ownership

- Change this repo only for starter/BOM/infrastructure defects; keep product logic in consumer applications
- Version bumps affect all BOM consumers — keep changes intentional and documented
- Coordinate breaking upgrades with the consumer parent POM that imports this BOM

## Build / CI

- JDK 25 (Temurin), Maven
- Local: `mvn -T1C clean test` (or module-scoped `-pl … -am`)
- CI (`maven-integration.yml` on PRs to `main`): `mvn -B license:check -P ci-check` then `mvn -B clean verify -P ci-check`
- New/edited Java files need Apache license headers (`license:format -P ci-check` if missing)
- Do not commit log dirs or `${LOG_ROOT}` artifacts

## Review / coding focus

- Prefer small, infrastructure-safe diffs; avoid drive-by refactors
- Watch dual Jackson stacks, Fastjson Redis wire compat, MQ V1/V2 envelopes, ES Java API Client (not HLRC)
- Tests: `@MockitoBean` (not `@MockBean`); `TestRestTemplate` needs `@AutoConfigureTestRestTemplate`; tracing via `@AutoConfigureTracing`
- Defer Batch C items unless explicitly requested: Protobuf major bump, LangChain4j 1.x, PayPal Jackson 3, Cache→Jackson 3, mapstruct-processor APT on JDK 25

More detail: root `AGENTS.md`.
