# V367 — Original Mojang sticky-A no-hit diagnostics (read-only)

Minecraft Java 1.8.9 · KillAura sticky target / native controller ·
draft stacked on V366 PR #878. No visuals or production policy changes.

## Exact source evidence

The V366 exact-head Foundation CI passed on Windows and Ubuntu. The
official original Mojang binary preflight passed the previously blocked
**V47** genuine initial A / survivor B / original B controller and true
dedicated-server B-damage step, plus V48, then proceeded to later tests.

However the complete original workflow failed at the *distinct* strict
sticky-UUID test, "Verify sticky UUID target lock retains A while B nearer
and releases to B on ineligibility".

Its native original game+server log positively witnessed:
- 20 actual original two-player source observations initially selecting
  closer A;
- 10 actual source-native UUID-locked A callback observations;
- 15 actual source-native observations retaining A while original B moved
  closer; **both original server damage scores remain zero** while A was
  in creative mode.

After the dedicated server restored original A to survival, the
acceptance did NOT observe the required real native locked A click
before deadline. The stricter follow-on real A server damage, eventual
A ineligibility, B acquisition and authoritative B damage checks were
not reached. This remains a failure, not a certified full client.

## Scope

This patch diagnoses the missing click **without changing gameplay**.
It does NOT modify Aura selection/lock policy, FOV, sprint/criticals,
GUI ownership, physical input, native controller or server oracle.

With the already-existing opt-in
`custommc.acceptance.reportKillAuraLockedTarget` flag, all three
bounded/diagnostic-only streams are emitted *only in actual phase 3*:
- `CUSTOMMC_189_V367_LOCK_A_PHASE3_SOURCE_DIAG`: actual original
  published World A/B UUID/index, current genuine 3D distances,
  selected/locked A, target rotation and original player rotation, A
  observed hurtTime.
- `CUSTOMMC_189_V367_LOCK_A_PHASE3_RAY_DIAG`: the **existing** original
  native crosshair `playerHit` and `playerIndex` just before combat
  scheduling; whether this physically hits actual source A or B.
- `CUSTOMMC_189_V367_LOCK_A_PHASE3_SCHEDULER_DIAG`: the existing
  computed scheduler candidate / final admission, current index/target.
  No second host World getter, target choice, click or packet.

Reports are bounded to first sample and every 80 up to 960 callbacks,
not unbounded spam or a fabricated positive result.

## Tests and acceptance

`scripts/test_mojang189_v367_locked_a_phase3_diag.py` asserts:
original A and B positive markers and server-score witnesses remain;
the phase-3-only diagnostic is snapshot-only without source/native
actions; ray logging occurs before unchanged synthetic scheduling; final
native controller call and refusal gates remain intact.

Run exact-head Windows and Ubuntu Foundation CI, original Mojang 1.8.9
binary preflight and the unchanged full A→B authoritative test. If the
locked-A test remains red, use *same original game evidence* to
distinguish the missing original ray, scheduler candidate, final
admission, controller veto or server damage; apply a separate focused
behavior repair only after grounded diagnosis.

**Draft, unmerged.** V47 passing is not entire-native-suite success.
