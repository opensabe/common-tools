---
applyTo: "**/*.{java,xml,yml,yaml,md}"
---

# Copilot code review — common-tools

When reviewing PRs in this repository:

1. **Scope**: Flag Matchplay business constants or Eureka coupling in starters. Prefer comments that push domain fixes to business repos.
2. **BOM blast radius**: Call out dependency/version changes that may break all consumers of `spring-cloud-parent` / `be-matchplay-parent`.
3. **Stack correctness**: Boot 4.1 / Cloud 2025.1.2 / JDK 25; Jackson 3 on HTTP path vs Jackson 2 on Redis Cache serializer; no HLRC for ES.
4. **Compat**: For serialization/MQ/cache changes, require or suggest wire-contract tests (`*WireContractTest`, Fastjson isolated CL) rather than assuming format unity.
5. **CI hygiene**: Missing Apache license headers will fail `license:check -P ci-check`. Reject accidental log/`LOG_ROOT` commits.
6. **Tests**: Prefer Spring Boot 4 test APIs (`@MockitoBean`, `@AutoConfigureTestRestTemplate`, `@AutoConfigureTracing`).
7. **Tone**: Prioritize correctness, compatibility, and upgrade risk over style nits. Skip suggesting Batch C deferred upgrades unless the PR already touches them.
