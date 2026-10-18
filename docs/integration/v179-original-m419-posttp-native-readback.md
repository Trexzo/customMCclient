# V179 — original M419 native post-teleport World convergence

## Verified source failure
- Cumulative V178 at exact head `f18384166b5c482c32f2d7dda593a0608e0541a6`, [original Mojang run 39124635903](https://github.com/Trexzo/customMCclient/actions/runs/39124635903), **FAILED** in the early genuine three-client M419 rejoin/controller case.
- The original dedicated server reported `Teleported CIHost to -0.5, 5.0, -1.5` and the Python harness printed `CUSTOMMC_189_REJOIN_PHASE4_ORIGINAL_HOST_RECENTER_SERVER_PASS=YES`. Yet on the subsequent actual mapped original-client controller call, original World still read CIHost `(-1.20,4,-1.09)`, real A `(-0.06,4,-3.14)`, B `(-1.50,4,-3.50)`. Exact **real 3D** A=`2.340064443`, B=`2.428179744`, source margin `.088 < .120`. B selected and B UUID locked were true. The original controller correctly rejected.
- V176 original run 39122093009 previously failed an independent live M419 margin `.079 < .120`; merely acknowledging the dedicated server /tp does **not** establish source Minecraft client's post-teleport World convergence.
- This log does **not** demonstrate an issue with V178 FastFall/Glide production fixes. Both Foundation OS jobs on exact V178 passed.

## Scoped implementation
- An **acceptance-only** source observer (`Minecraft189RejoinTargetAcceptance.observe`) accumulates 20 consecutive fresh ORIGINAL Minecraft 1.8.9 client World snapshots with the two real living player UUIDs, selected+locked survivor B, both finite 3D ranges, and buffered A/B difference `>=0.30` blocks. A bad sample resets the streak. The extra `.18` above `.12` is ONLY an earlier test harness stability prerequisite; the real attackEntity verification retains its separate unchanged exact `.12` source requirement.
- On 20, 40, 60 ... native callbacks, emit a read-only original JVM stdout marker `CUSTOMMC_189_M419_NATIVE_PHASE4_SOURCE_STABLE=20` with actual A/B index, source distances and UUID lock. No fake entity state, packet, clicks, client teleport or controller permission is produced. Only the existing `requireRejoinedControllerB` opt-in test property enables this observer.
- Python original server harness records the **original Minecraft client JVM stdout byte offset before its real server-acknowledged phase4 CIHost teleport**, then requires a NEW original JVM source marker after this offset **before** making B Survival/damageable. Old log messages cannot satisfy the fence. Timeout fails closed. B score reset, B Survival, real ray/controller B UUID and new B-positive/A-zero genuine scoreboard are still mandatory.

## Regression evidence
- Three additional JUnit classifier tests for exact V176/V178 original crashes; missing/incorrect selection/UUID lock; finite in-range source and `.30` margin.
- Four additional Python tests for stale original-JVM marker rejection, invalid inputs/process exit, exact server staging before damageable B, and unchanged M419 `.12` controller rule.

Cumulative Draft child of V178 PR #644. Need exact-head Windows/Ubuntu Foundation and entire original Mojang 1.8.9 78/78 graphical/dedicated-server acceptance, plus same-SHA stability repeat, before any merge. No `main` changes.
