# V281 — enforce real Mojang 1.8.9 CI for shared shipped runtime sources

Parent: V280 Draft PR #751, exact head `b4a291c26eaa4c537902af83256e96d385173636`.

## Confirmed path-filter omission

Read-only audit of the **actual V280 Git tree** and the original `minecraft-189-real-client.yml` PR path allowlist identified:

| Production module | Actual Java sources | Previously matched | Previously omitted |
| --- | ---: | ---: | ---: |
| bootstrap | 8 | 1 | **7** |
| core | 100 | 0 | **100** |
| launcher | 49 | 0 | **49** |
| platform-api | 2 | 0 | **2** |
| **Total** | **159** | **1** | **158** |

These are not necessarily gameplay bugs. They are **real CI scheduling omissions**: changes exclusively to shared production Java can change the shipped runtime without triggering the pinned genuine Mojang 1.8.9 X11 GUI / dedicated vanilla server workflow. Examples are module lifecycles, event bus, ClickGUI input, bootstrap class loader, launcher/native staging, and platform API. Earlier V279 only guaranteed the 1.8.9 platform source tree, not these linked libraries.

Additionally, root and shared-module Gradle build configuration scripts and `settings.gradle.kts` / `gradle.properties` did not trigger original CI. The `platform-1.8.9/build.gradle.kts` path was already covered and remains preserved.

## Exact scoped scheduling repair

Add only these PR `paths` triggers to the **existing unchanged real workflow**:

```yaml
- 'bootstrap/src/main/java/**'
- 'core/src/main/java/**'
- 'launcher/src/main/java/**'
- 'platform-api/src/main/java/**'
- 'bootstrap/build.gradle.kts'
- 'core/build.gradle.kts'
- 'launcher/build.gradle.kts'
- 'platform-api/build.gradle.kts'
- 'build.gradle.kts'
- 'settings.gradle.kts'
- 'gradle.properties'
```

No global wildcard or documentation trigger; existing exact platform and acceptance path entries remain. Changes to shared test files alone are still Foundation-only unless another established original trigger matches; this scope covers **production sources and build wiring**, not arbitrary test-only edits.

Six offline Python path-filter regression tests inspect **every actual Java source recursively** in all four modules, sample bootstrap/GUI/API/launcher source paths, dynamically inspect six existing root/module Gradle build files and remaining two root configuration files, and prove unrelated docs/test paths remain excluded. These source-level checks are **not** evidence of native gameplay success.

## Original acceptance and certification

No production game, source getter, click policy, packet, controller, X11, dedicated server, retry, deadline, acceptance marker or 78-step test was modified. GitHub Actions is now scheduled for relevant changes to shipped runtime code. Certification still requires exact-head Foundation Windows+Ubuntu 10/10 and **two independent fully completed authentic 78/78 Minecraft 1.8.9 original GUI/server jobs on identical commit SHA**.

Historical V276/V277 original multiplayer UUID/phase failures are genuine and remain unwaived under issue #503; V280 diagnostics do not prove a root-cause fix. All stacked PRs remain Draft; `main` untouched, no merge or publication.
