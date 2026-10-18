# Integration V170 — read the genuine original M419 crosshair in the post-rejoin attack lane

## Recorded exact-original failure and what remains unknown

This is a cumulative Draft child of [V169 PR #635](https://github.com/Trexzo/customMCclient/pull/635), exact parent source `ee5769f5197cc8b1ca85cf8fe3cbc53819e52795`. V169 Windows+Ubuntu Foundation was green at the time this implementation began, and pinned Mojang original game runs are checked separately; no native PASS transfers from other commits. Stable older V162 exact `66db7002abadb07207fbdfbf07171311bf494828` passed the full pinned original GUI and server suite **twice** 78/78, but later runs are intermittently blocked.

V167 [original 39114108797](https://github.com/Trexzo/customMCclient/actions/runs/39114108797) **FAILED after 48 passed workflow steps** with genuine original dedicated server `CITargetB taken` scoreboard staying zero in a later true rejoin controller scenario. The earlier actual real rejoin A disconnect, original UUID revalidation and some positive B server harm steps had passed, but **the later true native controller did not cause new positive server B damage before the source deadline**. Its phase-4 read-only M419 source diagnostics repeatedly reported `reason=null`, rejoined A entity index 6, B index 1, A true 3D distance ≈2.34, B ≈3.07, source-selected B=true and B UUID still locked. These facts confirm that the *existing M419 snapshot/UUID/3D selection prerequisites* were satisfied; they **do not prove** that original `Minecraft.objectMouseOver` hit the real B collision box, that the real controller was called, or that the server accepted a hit.

Existing read-only `Minecraft189RejoinTargetAcceptance.observeOriginalCrosshairAtClick` already measures true original `Minecraft.objectMouseOver` player ray against exact live source B UUID (including real yaw/pitch, source B eye/head geometry and current target rotation), but had a gating limitation: it reported only in **phase 3**, stopping precisely when the intermittent **phase-4 M419** controller B attack is expected. Thus the decisive original ray evidence for the actual failure was absent even though snapshot reason=null continued to print.

## Implementation: no click, rotation or server changes

**Only test-only original source acceptance instrumentation** `Minecraft189RejoinTargetAcceptance` is modified:

- `observeOriginalCrosshairAtClick` now runs for the original active **phase 3 or phase 4**, every **40 real mapped source ticks**, preserving its existing opt-in `custommc.acceptance.reportKillAuraRejoin` condition. Phase 0/1/2/5 and all other code paths remain silent.
- Phase number is added to the source non-PASS diagnostic `CUSTOMMC_189_REJOIN_ORIGINAL_CROSSHAIR_AT_CLICK` so later fail logs distinguish initial surviving B lock from actual post-rejoin controller M419.
- The existing original **boolean from mapped Mojang `objectMouseOver`** and exact source `originalHitIndex` → native UUID are still read, together with true original B feet Y, horizontal distance, eye-above-head delta, local yaw/pitch and selected target rotation. No new source observer fabricates a ray or changes any of these values.
- No change to `shouldAutoClick`, `PlayerControllerMP.attackEntity`, held item, player angle, original entity bounding boxes, Aura target UUID, native M419 wrong-A UUID veto or strict independent dedicated-server positive B/zero A damage. A logged `rayIsSurvivorB=true` is diagnostic ONLY and never a substitute for true controller + scoreboard proof.
- Pure helper `originalCrosshairObservationDue(phase,ticks)` isolates the read-only reporting cadence; positive original phase-4 reporting and rejection in all unrelated phases are regression tested.

## Four original-source regression tests

`Minecraft189M419CrosshairSourcePhaseTest` verifies phase-4 readback at ticks 40, 80, 400 (negative 39, 41, 0, -40); existing phase-3 reports remain; all other phases stay silent even when ticks are divisible by 40; diagnostic schedule is not an attack permission or original-game PASS. These Java tests contain no fabricated real raycast or outgoing packet.

## Required validation

Exact-head V170 **Foundation Windows+Ubuntu**, full pinned original **Mojang Minecraft 1.8.9 X11 GUI + dedicated server** 78/78. On a genuine later M419 failure, correlate phase-4 `realPlayerHit`, `realHitUuid`, `rayIsSurvivorB`, native source player angles and B collision geometry with the *separate original actual controller callback and dedicated-server B harm* to identify the concrete blocker. Do not increase timeouts, forge B damage, bypass UUID checks or disable strict tests. Require a second full identical-SHA native run for stability after first successful run, since earlier CI outcomes have been nondeterministic.

All parents and this PR remain cumulative Draft. Do not merge or change `main`. #503 original-binary reliability and #537 real modules remain open.
