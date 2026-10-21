# Internal HTTP call authentication (observe & alert)

Low-cost, observe-first detection of unexpected Intranet HTTP hops. Default is **off**. Failures **do not** change HTTP responses.

## Enable (production)

1. Deploy a build that includes this feature (common-tools `2.1.x` with the change).
2. Configure a shared secret (and optionally `previous` for rotation):

```yaml
opensabe:
  internal-call:
    enabled: true
    shared-secret: "<from secret manager>"
    # or:
    # secrets:
    #   current: "..."
    #   previous: "..."
    on-failure: WARN   # or ALARM
    edge:
      trusted-cidrs:   # must match real nginx / ALB private ranges
        - 10.0.0.0/8
      edge-token: ""   # optional; blank skips token check
```

3. Roll Gateway first (edge CIDR + outbound sign), then services (inbound verify + Feign/WebClient/RestTemplate outbound).
4. Expect `MISSING_HEADER` noise while old instances without signing are still up; that is expected during rollout.

## Optional nginx edge-token

Only when you set a non-empty `opensabe.internal-call.edge.edge-token`:

```nginx
proxy_set_header X-Edge-Token "<shared-edge-token>";
```

Recommended baseline: leave token empty; tighten Security Groups / NetworkPolicy so only the edge can reach Gateway; set accurate `trusted-cidrs`.

## Gray rollout

1. Upgrade library with `enabled=false` (no behavior change).
2. Configure secret + Gateway `trusted-cidrs`, then `enabled=true` with `WARN`.
3. Watch alarms / logs by reason code.
4. Rotate secrets with `secrets.current` / `secrets.previous`.
5. External callbacks hitting a business port: add path to `exclude-path-patterns` or disable inbound on that service.

## Misreport checklist

| Symptom | Likely cause |
|---|---|
| `MISSING_HEADER` | Rolling deploy; RestTemplate not decorated; host not matched for WebClient |
| `EXPIRED` | Clock skew between nodes |
| `UNTRUSTED_EDGE` | Wrong `trusted-cidrs`; peer not LB/nginx. On cloud with `forward-headers-strategy=NATIVE`, edge uses Netty `connectionRemoteAddress()` (TCP peer), not XFF client IP. If the LB preserves client IP at L4, peer is still the public client — use SG/NetworkPolicy and/or `edge-token`. |
| `BAD_SIGNATURE` | Secret mismatch / not rotated |
| `UNSUPPORTED_PEER_IP` | IPv6 or IPv4-mapped peer (`::ffff:10.x`) |
| `BAD_EDGE_TOKEN` | Token configured but nginx not injecting / wrong value |
| WebClient unsigned | Host is public FQDN; set `internal-host-patterns` or use short service name |
| RestTemplate unsigned | Use `InternalCallClients.decorate(restTemplate, interceptor)` |

## RestTemplate decorate

Spring-managed `RestTemplate` beans are customized automatically. For `new RestTemplate()`:

```java
@Autowired
private InternalCallRestTemplateInterceptor interceptor;

RestTemplate rt = InternalCallClients.decorate(new RestTemplate(), interceptor);
```

## Security boundary (read carefully)

- This is **observe & alert**, not default ENFORCE. It does not replace network isolation.
- Anyone with the shared secret can forge caller headers.
- This is **not** end-user login / JWT.
- Over-broad CIDRs (e.g. `0.0.0.0/0`) weaken edge trust.
- Public hosts are not signed; forged mechanism headers are still stripped when `enabled=true`.
- Gateway access logs redact edge-token and sign values as `***`.
- All Gateway instances must share the same secret and edge settings.
- **v1 canonical does not bind METHOD/path.** A captured valid signature can be replayed to other paths within the clock-skew window (`skew-ms`, default 5 minutes). There is **no** distributed or in-process nonce store in v1 (by design); rely on skew + network isolation.
- Request attribute `opensabe.internal-call.verified` is an audit/context marker only. **Do not** use it as the sole authorization decision.

## Reason codes

`MISSING_HEADER`, `BAD_TIMESTAMP`, `BAD_SIGNATURE`, `EXPIRED`, `UNSUPPORTED_VERSION`, `UNTRUSTED_EDGE`, `BAD_EDGE_TOKEN`, `PEER_IP_MISMATCH`, `UNSUPPORTED_PEER_IP`, `NO_SECRET_CONFIGURED`, `INTERNAL_ERROR`.
