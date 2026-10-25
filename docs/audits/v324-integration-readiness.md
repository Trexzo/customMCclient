# V324 integration and release readiness — exact-source audit

**Snapshot:** 2026-10-25. **Scope:** source inventory, open-PR snapshot, targeted historical branch parity, and release packaging entry points. **This is not a merge, security certification, binary redistribution permission, or release approval.**

## Immutable source anchors

- Certified cumulative integration head: `060445b541f7b83b975d3311683e7778a431bf0e` (Draft PR #804). Git tree: `1fad285aa17d4ba74bb34b60b0732a5f89bb48a3`.
- Unchanged `main`: `ea65d456ab4cb0150c0b7fa2ebb7d732925ab051`.
- Source tree was read from GitHub's full recursive `git/trees/<SHA>?recursive=1` endpoint with **`truncated=false`**, not from its limited 300-file comparison view.
- **V324: 1,219 Git tree entries = 1,115 blobs/files + 104 tree directories**. Main: 569 blobs + 102 directories. Full tree comparison: **546 added, 91 modified, 0 deleted files** = **637 changed paths**. The REST commit-compare endpoint returned only 300 file entries and is not a valid full-file inventory.
- [Complete V324 Git tree](v324-exact-git-tree.tsv) includes every file and directory path, Git mode, exact object SHA and blob byte size. [All source differences vs main](v324-main-tree-delta.tsv) includes each changed path and both content SHAs/sizes. These TSVs are pinned to V324, not to this documentation-only audit branch.

## Open PR snapshot *before* creating this audit PR

- Uncapped GitHub REST `/pulls?state=open&per_page=50&page=1..9`, verified an empty page 10: **426 open PRs**, **321 Draft**, **105 non-Draft**.
- Walking current head/base branch names from #804 to `main` yields **316 directly stacked PRs** and **110 off-stack PRs**. The 316 figure is PR-base-chain membership, **not** a count of commits.
- [All 426 open PR heads](v324-open-pull-requests.tsv) includes PR number, Draft/ready status, exact head/base ref/SHA, update time and direct-stack/off-stack grouping.
- The **31 particular historically divergent PRs** identified in issue #777's 2026-10-23 audit were each re-fetched at their current exact head and re-compared to V324; **all 31 still diverge in Git ancestry**. Do **not** infer 31 missing features. The remaining 79 off-stack PRs **have not been reclassified for actual Git ancestry/behavior equivalence in this snapshot**; neither `off-stack` nor `ready` is a merge decision.
- This is an explicit current snapshot, not a continuing PR count. Creating this audit PR adds another open PR after the snapshot.

## CI evidence, precise limits

- V321 #801, V322 #802, V323 #803 and V324 #804 each independently passed **two complete 80-step original Mojang Minecraft 1.8.9 + vanilla server CI attempts on unchanged respective head SHAs** and Windows/Ubuntu Foundation CI.
- V324 exact-head [original CI #40205094104](https://github.com/Trexzo/customMCclient/actions/runs/40205094104) attempts 1 and 2 both finished **80/80 SUCCESS**; [Foundation #40205094099](https://github.com/Trexzo/customMCclient/actions/runs/40205094099) Windows and Ubuntu both **10/10 SUCCESS**.
- V324's first original preflight reports **532 Python regression tests passing**. This does **not** imply a tested, downloadable Windows release ZIP or manual interactive Windows GUI verification.
- Historical intermittent early B=10 damage is **not root-cause proven fixed**, even though subsequent strict B=0 native CI checks passed. Preserve issue #503 and all strict real controller/UUID/ray/FOV/Wall/Scaffold/scoreboard checks.

## Targeted historical divergent-PR source comparison

- **#390 (old Attack Range gate):** `Minecraft189AttackRangeModule.java` and `Minecraft189AttackRangeFeature.java` already exist at V324 with the original `combat.attackRange` setting/registration structure and a newer optional native-hitbox distance setting. **Source superseded/extended; no blind cherry-pick.** Exact line/test equivalence beyond the reviewed source is not claimed.
- **#470 (original raytrace):** `Minecraft189ScaffoldWorldRayTraceAccess.java` remains at V324, and transformer mappings still include `MOVING_OBJECT_TYPE_BLOCK` and `MOVING_OBJECT_SIDE_HIT`. Source-level continuity is visible; a complete per-instruction parity proof has not been performed.
- **#483 (native right-click bridge):** historical `Minecraft189ScaffoldNativePlacementAccess` has been replaced by `Minecraft189ScaffoldVanillaPlaceAccess`, with original Mojang controller references and fail-closed default. Replacement is **not** byte-for-byte proof of the old bridge/test.
- **#467 (World ownership epochs):** historical per-host `Minecraft189ScaffoldWorldOwnership` class/test are not in V324. Newer `Minecraft189ScaffoldSourceEpoch` in `Minecraft189RuntimeBridge` uses weak Minecraft/World/player identity, invalidates old receipt generations, and checks same-source readback before accepting original placement evidence. **Similar safety goal but different ownership lifetime and scope** (acceptance-only receipt epoch versus host-owned World cell evidence). Exhaustive lifecycle equivalence is **still unresolved**; do not mark #467 fully absorbed.
- **#412:** the current binary preflight checks original obfuscated class mappings and Mojang runtime; original branch's individual historic tests were not compared one-for-one.

## Packaging, dependency, privacy and licensing preliminary triage

- `README.md` and root `build.gradle.kts` define `assembleLauncherDistribution`, `verifyLauncherDistribution`, `build/custommc-distribution`, Windows `bin/launcher.bat`, launcher `lib/` and an overlay of **exactly five stable JAR filenames**. `verifyFoundation` depends on distribution-shape verification. This is a **verified CI distribution shape**, not an audited user-installable Windows package.
- Root Gradle version is `0.1.0-SNAPSHOT`. Build uses Java 21 toolchain with Java 8 bytecode in some modules. Directly referenced libraries include ASM 9.7.1, LWJGL 2.9.3, Gson 2.14.0 and JUnit 5.11.0; transitive/version/license review and a complete dependency SBOM remain outstanding.
- Git tree extension inventory: 849 `.java`, 202 `.md`, 53 `.py`, 7 `.kts`, 2 `.yml`, one `.properties` and one `.gitignore`. **No binary/JAR/EXE/DLL/credential-bearing extensions are tracked by path in this exact tree.** This is *not* a secret scan of file contents, generated build output or historical commits.
- There is **no root `LICENSE` or `COPYING` file** in this exact tree. Resolve proprietary/third-party license obligations and intended redistribution terms explicitly. No Mojang/Minecraft binary is tracked in this source snapshot; runtime downloads, asset provenance, licensing and user entitlement must be separately reviewed.
- README documents reading the access token from an environment variable rather than a command-line argument. This does not certify logging, storage, crash reports, launcher environment or token handling end-to-end.
- No Gradle wrapper files or dependency lockfile appear in the pinned tree. CI pins Gradle 8.10.2, but a fully independent clean-machine and offline dependency-reproducibility acceptance has **not** been shown.

## Decisions required before merge or release

1. Complete targeted source-and-test parity for historical unresolved alternatives, especially #467's World-cell ownership lifecycle and the remaining off-stack branches. Preserve historical negative CI evidence.
2. Review build dependency graph, license notices, third-party redistributed binaries, artifact provenance, credentials/data handling and generated package contents.
3. Reproduce `assembleLauncherDistribution` on a clean Windows machine, inspect all artifacts/hashes, run interactive GUI/login startup and negative/failure cases, and explicitly verify rollback and packaging.
4. Re-review the final exact integration head, branch protection and release policy. **No automatic cherry-picks, ready-for-review promotions, merges, closes, rebases, retargets, binary distribution or release.** `main` remains at the exact original SHA above.

**Audit limitation:** Git object metadata and selected source files have been reviewed. This report does not claim a full automated source-content secrets scan, a complete binary provenance/SBOM review, or complete semantic equivalence of all 110 off-stack PRs.
