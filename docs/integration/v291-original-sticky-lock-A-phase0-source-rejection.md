# V291 — actual original locked-target startup source failure provenance

Immutable parent: V290 Draft PR #761 exact head `d12cc26ac3640740c6b32c45861d6e49076f110b`.

## Genuine original Mojang failure requiring the next evidence batch

V288 [real pinned Minecraft 1.8.9 X11 GUI/vanilla server run #39632718006](https://github.com/Trexzo/customMCclient/actions/runs/39632718006), exact SHA `4768e7186f23d9a7e0e21f76a79718d5a383e5de`, job `119334963314`, **FAILED** at genuine original `Verify sticky UUID target lock retains A while B nearer and releases to B on ineligibility`, after 56 preceding successful stages.

The **actual** last error was `CUSTOMMC_OFFICIAL_189_LOCAL_MULTIPLAYER_FAILED: missing 20 disabled callbacks selecting genuine nearer A marker within deadline`. The original fixture sent genuine dedicated-server gamemode and three client teleport commands and demanded the existing real `CUSTOMMC_189_LOCK_TWO_ELIGIBLE_INITIAL_A_PASS=20` marker. It never got **20 consecutive source-native initial A** callbacks while Aura was still disabled.

**This is not evidence of a wrong locked UUID attack**; the initial negative/eligibility gate never armed Aura. The actual original test did not get as far as closer-B retention, true native PvP damage, or releasing A. Historical V276/V277 had genuine downstream UUID phase rejection failures; this V288 issue is a distinct earlier source-stability failure and remains FAILED regardless of later successes. Possible causes include source-array alignment, absent/changed remote identities, original 3D distance/height, A/B ordering, source nearest target, or source aim rotation ownership. No definitive cause is yet proven.

## Original-state diagnostic instrumentation, without gameplay changes

V291 modifies **only** `Minecraft189LockedTargetAcceptance` opt-in original Minecraft acceptance class:

- Preserve the *same* read sequence and failure semantics: source native UUID/kind/combat/positions/local snapshots must all be available, all native entity array counts must match, exactly two source native remote players and both matching alive A/B UUIDs must exist, finite original full 3D distances must be present. Each existing null branch now records a bounded **same-read reason**: missing native snapshot, count mismatch, invalid remote UUID inventory or nonfinite 3D distance. No extra native getter or second World/entity scan.
- Keep the **unchanged** original `Evidence.initialA()` predicate authoritative: A and B each at 3D distance 1..3.25, A closer by **strict** >0.12, original nearest native index exactly A, original target-rotation native index exactly A. The original `initialTicks=e!=null && e.initialA()?initialTicks+1:0` semantics are preserved, including resetting on any absence or different source.
- For **phase 0 only**, count true original callbacks, high water of existing consecutive valid-A counter, and failures grouped as source unavailable, native length mismatch, A/B remote UUID missing, nonfinite distance, geometry margin/distance invalid, or nearest/aim source index mismatch. The first and every 40th native callback prints a **read-only** `CUSTOMMC_189_LOCK_INITIAL_A_SOURCE_DIAGNOSTIC` with the last same-read decision, full original A/B 3D distances and native A/B, nearest/rotation indices, and strict original 20-streak count. No fake original PASS marker, no target change, click, source mutation, or new timer.
- A pure separate diagnostic function `m291InitialAReason` uses the copied original geometry/selected-source values to classify the same predicate requirements for read-only logs; it cannot authorize eligibility. Twelve new Java 8/JUnit tests check strict 3D boundaries, exact >0.12 margin, genuinely nearer B, missing/duplicate native UUID indices, stale nearest or target-rotation ownership, real source-index reorder and nonfinite values. These are **offline** input classifications, not original Mojang game observations.

## Strict historical and future certification

The real V288 disabled-A source timeout remains an authentic FAILED original run. The true V286 V65 nonplayer missing-ray, V283 second V29 Scaffold and V282 original FOV unexpected click, and earlier V276/V277 wrong-UUID failures also remain red historical evidence, not waived. V289's native run has crossed the formerly failed nonplayer stage 65 and exceeded 69 original stages, but cannot count as full 78/78 until GitHub marks the *entire original job completed SUCCESS*. V285 and V287 have each achieved independent exact-SHA double original 78/78; neither certifies V291.

V291 exact-head certification still requires **both Windows and Ubuntu Foundation 10/10, plus TWO independent complete original Mojang Minecraft 1.8.9 X11 GUI + vanilla dedicated-server 78/78 on that same unchanged commit SHA**. All PRs Draft, `main` unchanged; no merge or release.
