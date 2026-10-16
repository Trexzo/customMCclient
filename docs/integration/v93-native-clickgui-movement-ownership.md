# V93: native ClickGUI movement ownership — cumulative integration

## Scope and provenance

This integration branch starts from V83, PR #549, commit `86221ffdcdf0a32b29ecf50c4cbee7bf4937bb2b`, and cumulatively includes V84–V92. It modifies nine pre-existing production Java modules, the native host runtime dispatch, and their focused JUnit suites. No new gameplay module is merely a placeholder. The base remains an **unmerged draft integration lineage**, not `main`.

The player access interfaces in unit tests are implemented by test doubles. Invoking the real `Minecraft189HostRuntime` and its `clickGuiToggleController()` tests the **CustomMC ClickGUI model and module dispatch**, not proof that the exact unmodified Mojang 1.8.9 client showed the UI or executed a world-physics transition. The separate official Mojang 1.8.9 binary workflow must be completed, and a focused runtime proof should still be added for actual in-game GUI ownership before calling this UI behavior original-game certified.

## Changes, per source module

| Version | Draft PR | Source-native behavior when CustomMC ClickGUI owns focus |
|---|---|---|
| V84 | #550 | Flight: suppress new native motion X/Y/Z writes, preserve vanilla motion, resume on close. |
| V85 | #551 | AutoSneak: suppress synthetic sneak setter, preserve already-existing player sneak state. |
| V86 | #552 | Glide: suppress Y writes and revoke old activation-delay credit. |
| V87 | #553 | AirJump: consume UI-owned Space press edge, prevent post-close replay into native jump. |
| V88 | #554 | ReverseStep: invalidate pending ground-to-air transition and delay credit; require new landing. |
| V89 | #555 | FastFall: suppress Y writes and revoke stale descent activation credit. |
| V90 | #556 | NoGravity: suppress Y writes, keep pre-existing vertical arbitration over FastFall/Glide. |
| V91 | #557 | Step: release persistent boosted native stepHeight to vanilla 0.6 while focused, recalculate after close. |
| V92 | #558 | NoClip: release only module-owned persistent noClip state to this native player's baseline, re-acquire after close. |

All changes retain the existing source-compatible apply overloads for previous Java callers. Toggle behavior is driven by `clickGuiRuntime.coreRuntime().model().snapshot().open()` passed at the production host boundary. Do not confuse this with every vanilla inventory, pause, or OS-focus state.

## Validation requirements

1. Every new module's focused JUnit test must run under Foundation CI on Windows and Ubuntu. The tests include actual CustomMC host GUI toggle and source-access-interface doubles, plus state transitions and stale-input-edge protection.
2. The **entire** existing official Mojang 1.8.9 workflow must pass on the *exact cumulative V93 commit*; all prior original-native PvP, UUID-target-lock, FOV, Scaffold, Timer and dedicated-server proof steps remain mandatory.
3. Original-game native ClickGUI ownership still needs focused runtime evidence (screen state, actual event delivery, player field before/after in a real client), not an extrapolation from JUnit mocks or a generic official-workflow PASS.
4. Preserve the pre-existing game authority: do not weaken assertions, truncate historical workflow requirements, remove player-identity checks, or claim that a passed Foundation build proves original game behavior.
5. If any step fails, report the failed test/fixture and distinguish a reproducible module regression from an unrelated historical test-runtime fault. Compare the exact failing step and rerun before attributing cause.

## Known original-suite investigations at the start of integration

- V84 #550's first full original-client run failed in an **older retained A UUID lock before disconnect** test, before a proof of Flight focus; its failed job was queued for rerun.
- V86 #552's first original-client run failed in an **older rear-player FOV** test with client exit 255; a failed-job rerun was requested.
- These failures are **not** evidence that the new modules passed original-game interaction tests, nor evidence that Flight or Glide caused the earlier harness failures.
- CI conclusions elsewhere in this integration lineage are point-in-time only; inspect commit SHA and GitHub Actions jobs afresh before merging.

## Integration/rollback discipline

Keep module draft PRs #550–#558 independently reviewable until V93's cumulative tests succeed. Do not merge their stacked bases independently onto `main`; V93 is the integration review surface. To roll back the unmerged change set, leave this draft branch unmerged and use the unchanged V83 base. When merging is eventually authorized, select a single integration strategy and avoid duplicating the nine stacked changes.
