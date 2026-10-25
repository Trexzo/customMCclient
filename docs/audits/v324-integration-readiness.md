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
- Every one of the **110 off-stack PRs** was individually compared by exact saved head SHA against V324 using GitHub's commit comparison: **73 heads are Git ancestors of V324 (status=behind, ahead=0)**; **37 heads are diverged (unique commits in both directions)**. The 37 comprise all 31 historically divergent PRs from the 2026-10-23 issue #777 audit plus **six newer alternatives: #782, #783 and #796–#799**. [Full per-head ancestry results](v324-off-stack-ancestry.tsv). Git divergence does **not** prove that implementation is absent or that any PR needs cherry-picking; 37 source/behavior parity decisions remain separate.
- This is an explicit current snapshot, not a continuing PR count. Creating this audit PR adds another open PR after the snapshot.

## CI evidence, precise limits

- V321 #801, V322 #802, V323 #803 and V324 #804 each independently passed **two complete 80-step original Mojang Minecraft 1.8.9 + vanilla server CI attempts on unchanged respective head SHAs** and Windows/Ubuntu Foundation CI.
- **Subsequent child V325 #806:** exact head `55aef8aeaae76b3345c1e03c7bf8d21aaf1d8540` has now independently completed **two 80/80 original Mojang CI successes on the same SHA**, [original run #40213360492 attempts 1+2](https://github.com/Trexzo/customMCclient/actions/runs/40213360492) and [Windows/Ubuntu Foundation #40213360499 (10/10 both)](https://github.com/Trexzo/customMCclient/actions/runs/40213360499). V325 requires both genuine native nonplayer net +4 and >=4 distinct newly visible UUIDs for M417, closing the independent #782/#783 source acceptance gap. **V324 remains unchanged and separately certified; the audit base remains V324.**
- V324 exact-head [original CI #40205094104](https://github.com/Trexzo/customMCclient/actions/runs/40205094104) attempts 1 and 2 both finished **80/80 SUCCESS**; [Foundation #40205094099](https://github.com/Trexzo/customMCclient/actions/runs/40205094099) Windows and Ubuntu both **10/10 SUCCESS**.
- V324's first original preflight reports **532 Python regression tests passing**. This does **not** imply a tested, downloadable Windows release ZIP or manual interactive Windows GUI verification.
- Historical intermittent early B=10 damage is **not root-cause proven fixed**, even though subsequent strict B=0 native CI checks passed. Preserve issue #503 and all strict real controller/UUID/ray/FOV/Wall/Scaffold/scoreboard checks.

## Targeted historical divergent-PR source comparison

- **#390 (old Attack Range gate):** `Minecraft189AttackRangeModule.java` and `Minecraft189AttackRangeFeature.java` already exist at V324 with the original `combat.attackRange` setting/registration structure and a newer optional native-hitbox distance setting. **Source superseded/extended; no blind cherry-pick.** Exact line/test equivalence beyond the reviewed source is not claimed.
- **#470 (original raytrace):** `Minecraft189ScaffoldWorldRayTraceAccess.java` remains at V324, and transformer mappings still include `MOVING_OBJECT_TYPE_BLOCK` and `MOVING_OBJECT_SIDE_HIT`. Source-level continuity is visible; a complete per-instruction parity proof has not been performed.
- **#483 (native right-click bridge):** historical `Minecraft189ScaffoldNativePlacementAccess` has been replaced by `Minecraft189ScaffoldVanillaPlaceAccess`, with original Mojang controller references and fail-closed default. Replacement is **not** byte-for-byte proof of the old bridge/test.
- **#467 (World ownership epochs):** [focused original-world-ownership parity audit](v324-world-ownership-467-parity.md). Historical per-host `Minecraft189ScaffoldWorldOwnership` class/accessor/test and exact acceptance marker do **not** exist 1:1. Current code enforces exact native World identity via **callback-only `Minecraft189ScaffoldWorldLease`**, **production `Minecraft189ScaffoldActivationSource` weak identity/revocation**, acceptance-only `Minecraft189ScaffoldSourceEpoch` plus same-source readback, and an independent **`Minecraft189WorldEntityHostSourceLease`** for loadedEntityList callbacks; tests cover stale original World evidence, replacement, disconnect, invalid source and nested callback revocation. **Live safety goals are architecturally superseded, but old long-lived per-host World-cell accessor/marker API is not equivalence-certified.** Do not cherry-pick a legacy owner without a demonstrated consumer/requirement.
- **#412:** the current binary preflight checks original obfuscated class mappings and Mojang runtime; original branch's individual historic tests were not compared one-for-one.

## Post-inventory parity of six newer divergent sibling PRs

The exact ancestry snapshot contains six diverged sibling heads beyond the 31 historical: **#782, #783, #796, #797, #798 and #799**. The ancestry status remains correct, but source checks changed the integration judgment:

- **#796–#799:** [dedicated forensic sibling audit](v324-forensic-sibling-parity.md) compares their exact head tree Git blob SHAs, changes and added source lines against V324/V325. Their individual read-only original post-A World tick, B hurtTime, genuine vanilla-server damage-score and Mojang `attackEntity` Entity-UUID witnesses are **present in combined current sources**. Several dedicated regression files are byte-identical to donor heads. The shared V317 failure-only audit was intentionally strengthened from two competing sibling diagnostic-handler counts to **four independent bounded diagnostic paths**; old exact-two lines should not be replayed. These four PRs are **diagnostically superseded**, not absent.
- **#782/#783:** comparative source audit identified a **real M417 acceptance evidence gap** at V324: four new distinct nonplayer UUIDs were observed diagnostically, but the actual positive marker required only net nonplayers +4. Separate [Draft V325 #806](https://github.com/Trexzo/customMCclient/pull/806) strengthens the actual same-native-World-source gate to require both facts. V325 is now **separately exact-head double-original CI certified**, not by inheritance: [original #40213360492 attempts 1+2](https://github.com/Trexzo/customMCclient/actions/runs/40213360492) both 80/80 SUCCESS, [Foundation #40213360499](https://github.com/Trexzo/customMCclient/actions/runs/40213360499) both OS 10/10 SUCCESS.
- Historical **31 diverged PR heads** still require scoped evaluation of their functional changes where not already addressed (#390, #470, #483 have source supersession evidence; #467 source World ownership lifecycle remains unresolved). No automatic merge decisions follow from this audit.

## Legacy GUI focus and finite-native-value siblings (#542–#546)

A second targeted patch-to-current-source audit confirms that the **five Git-diverged module sibling PRs #542–#546** have their proposed control guards and JUnit regressions in the V324/V325 code. [Detailed module parity notes](v324-module-sibling-parity.md) cover AutoSprint ClickGUI synthetic sprint veto; WTap native setter failure revocation; AutoJump focus/ground-contact reset; FastPlace/FastBreak/SpeedMine menu-focus write veto; and NoFall/NoSlow finite source protection. The old NoFall single `if (Float.isFinite(distance)` guard is intentionally part of a stronger current combined native-epoch/player/GUI/threshold gate. **Do not cherry-pick older alternatives over these modernized guards.** This is source/test inclusion, not proof of separate run-time certification for each branch.

## Packaging, dependency, privacy and licensing preliminary triage

- `README.md` and root `build.gradle.kts` define `assembleLauncherDistribution`, `verifyLauncherDistribution`, `build/custommc-distribution`, Windows `bin/launcher.bat`, launcher `lib/` and an overlay of **exactly five stable JAR filenames**. `verifyFoundation` depends on distribution-shape verification. This is a **verified CI distribution shape**, not an audited user-installable Windows package.
- Root Gradle version is `0.1.0-SNAPSHOT`. Build uses Java 21 toolchain with Java 8 bytecode in some modules. Directly referenced libraries include ASM 9.7.1, LWJGL 2.9.3, Gson 2.14.0 and JUnit 5.11.0; transitive/version/license review and a complete dependency SBOM remain outstanding.
- Git tree extension inventory: 849 `.java`, 202 `.md`, 53 `.py`, 7 `.kts`, 2 `.yml`, one `.properties` and one `.gitignore`. **No binary/JAR/EXE/DLL/credential-bearing extensions are tracked by path in this exact tree.** This is *not* a secret scan of file contents, generated build output or historical commits.
- There is **no root `LICENSE` or `COPYING` file** in this exact tree. Resolve proprietary/third-party license obligations and intended redistribution terms explicitly. No Mojang/Minecraft binary is tracked in this source snapshot; runtime downloads, asset provenance, licensing and user entitlement must be separately reviewed.
- README documents reading the access token from an environment variable rather than a command-line argument. This does not certify logging, storage, crash reports, launcher environment or token handling end-to-end.
- No Gradle wrapper files or dependency lockfile appear in the pinned tree. CI pins Gradle 8.10.2, but a fully independent clean-machine and offline dependency-reproducibility acceptance has **not** been shown.

## Decisions required before merge or release

1. Complete remaining targeted source-and-test parity decisions for **37 Git-diverged off-stack PR heads**, noting that #796–#799, #542–#546 and #467 have now been specifically source-triaged and #782/#783 triggered the separate V325 correction. #467's historic per-host World evidence accessor/marker is not 1:1 present, but modern callback, activation and receipt guards already cover source revocation goals. Do not infer 37 missing features or cherry-pick historic APIs blindly. The 73 other off-stack heads are already Git ancestors. Preserve historical negative CI evidence.
2. Review build dependency graph, license notices, third-party redistributed binaries, artifact provenance, credentials/data handling and generated package contents.
3. Reproduce `assembleLauncherDistribution` on a clean Windows machine, inspect all artifacts/hashes, run interactive GUI/login startup and negative/failure cases, and explicitly verify rollback and packaging.
4. Re-review the final exact integration head, branch protection and release policy. **No automatic cherry-picks, ready-for-review promotions, merges, closes, rebases, retargets, binary distribution or release.** `main` remains at the exact original SHA above.

**Audit limitation:** Git object metadata and selected source files have been reviewed. This report does not claim a full automated source-content secrets scan, a complete binary provenance/SBOM review, or complete semantic equivalence of all 110 off-stack PRs.
