---
name: common-tools-multiversion-release
description: >-
  Backport fixes and cut patch releases across common-tools maintenance lines
  (main, v1.0.4.*, v1.0.8.*): bump Maven versions, create same-named branch and
  tag, publish via GitHub Release to Maven Central. Use when backporting, cutting
  LTS patches, tagging, creating GitHub Releases, or deploying common-tools.
---

# common-tools multi-version release

## Maintenance lines

| Line | Latest released (branch = tag) | Next patch |
|---|---|---|
| main | `v2.1.2` (`2.1.2`) | cut `v2.1.X`, then leave main at `2.1.(X+1)-SNAPSHOT` |
| 1.0.4 LTS | `v1.0.4.28` | `v1.0.4.29` |
| 1.0.8 LTS | `v1.0.8.6` | `v1.0.8.7` |

Update this table after every successful release.

Always `git fetch origin --tags` first. Confirm the latest tip by checking that
branch tip and tag tip are the same commit. Do not merge `main` into LTS lines.
Do not move or force-push existing tags.

## Version bump rules

- Project version is hardcoded in module `pom.xml` parent `<version>`, some
  module own `<version>`, and `spring-cloud-parent` `<opensabe.version>`.
  Also update `spring-boot-starter-mybatis/readme.md` when it contains the
  same string.
- Replace only the project version string. Do not change Spring Boot /
  Spring Cloud BOM versions.
- main: release branch turns current `A.B.C-SNAPSHOT` into the next release
  number (e.g. `2.1.1-SNAPSHOT` → `2.1.2`). After publish, advance main to
  `2.1.3-SNAPSHOT` (one past the release), not `2.1.2-SNAPSHOT`.
- LTS tips are already release versions. New branch bumps the last segment
  (`1.0.4.27` → `1.0.4.28`, `1.0.8.5` → `1.0.8.6`).

## Branch / tag / deploy

1. From the latest tip of that line, create branch `v{version}`.
2. Apply the fix and bump versions. Commit message style: `release X.Y.Z` on
   main line, `final X.Y.Z.W` on LTS.
3. Push the branch. Create GitHub Release with the **same** name as the branch
   and tag (`v{version}`), target = that branch:
   ```bash
   gh release create "v{version}" --target "v{version}" --title "release {version}" --notes "..."
   ```
4. Release `created` triggers `.github/workflows/maven-publish.yml`
   (`mvn -DskipTests deploy -P maven-central`). Watch that workflow.
5. Never reuse a non-SNAPSHOT Maven Central version. If a tag/Release already
   exists for that version, cut the next patch instead.


## Publish notes

- Prefer **sequential** GitHub Releases (one line at a time). Parallel Central uploads can flake with `Bundle has content that does NOT have a .pom file` and missing `Pre Bundling` logs.
- Keep `central-publishing-maven-plugin` at **0.11.0+** on every maintenance line (`maven-central` profile in root `pom.xml`).
- If deploy fails with that pom-bundle error but the commit is correct, `gh run rerun <id>` is often enough; do not bump the artifact version.
- Workflow success still leaves Central in "validated / manual publish" when auto-publish is off; finish at https://central.sonatype.com/publishing/deployments if needed.
- Branch and tag names collide (`v1.0.4.28` etc.). Use explicit refspecs: `git push origin refs/tags/vX.Y.Z` and `git show refs/tags/vX.Y.Z:...`.

## Checklist

```
- [ ] git fetch; confirm latest tip per line
- [ ] fix applied on each line (no main→LTS merge)
- [ ] only project version strings replaced
- [ ] branch + tag + Release all named v{version}
- [ ] Maven Package workflow green
- [ ] update "Latest released" table in this skill (on main only)
```
