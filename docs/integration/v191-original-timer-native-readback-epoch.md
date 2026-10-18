# V191 — authentic Minecraft Timer field readback lease and 1x restoration

## Parent evidence
V181 has two independently completed full 78-step pinned Mojang 1.8.9 X11 graphical + vanilla dedicated server successes at the same SHA; V182 through V188 have each completed their own exact-SHA original 78-step runs. V189 and V190 exact source original suites must be checked separately. These results do not certify V191.

## Real Java source concern
`Minecraft189TimerSpeedModule` has a persistent native Game Timer field, read via `customMcTimerSpeed()` and written by `customMcSetTimerSpeed()`. It implements default and airborne speed, real ground/sprint/sneak/GUI guards, optional smooth ramping and mandatory instant return to the vanilla **1.0x** game clock when permission is absent. Previously those permissions were checked **before** reading the original mapped Timer field. That getter can synchronously cause a module OFF→ON or persisted GUI setting edit; after returning, the old callback could set an obsolete accelerated world clock or continue old smoothing under a new owner. This is an identified static Java callback reentrancy gap, not evidence of an observed native Minecraft crash.

## Implementation
- Per-activation `nativeReadEpoch` increments on every onEnable/onDisable.
- Snapshot all relevant source policy **before mapped getter**: enabled epoch; normal/airborne Speed; Airborne Override; Smooth Transition and Transition Step; Sprint Only, Pause Sneaking, Ground Only and Pause While ClickGUI flags.
- After genuine mapped Timer field getter, validate same epoch and **all** live GUI settings. Old callback with OFF→ON or edited settings cannot issue stale Timer write.
- When real callback **disabled the module** (no new activation), release persistent non-vanilla or nonfinite Timer field **to 1.0x immediately** even if the previous activation had smoothing. An OFF→ON cycle does **not** overwrite the new owner's Timer state.
- In a still-valid activation, preserve prior exact speed/ground/airborne/movement/GUI and true native field ramping, finite fallback and redundant-setter avoidance. No packet injection, synthetic game Time/World, fabricated player state or replaced original acceptance.

## Regression
10 new adversarial JUnit Java timer-adapter tests: mapped getter OFF→ON; disable and immediate vanilla 1x restoration; disabled NaN restoration; mid-read normal Speed edit, GUI pause edit, Smooth Transition Step edit, Airborne Override edit and Ground Only edit; unchanged valid timer 1.5x single write and eventual release; always-vanilla disabled callback. These are offline source adapter policy tests, not original binary gameplay evidence.

Cumulative Draft child of V190 #656. Require exact V191 Windows+Ubuntu Foundation, complete original Mojang Minecraft 1.8.9 78-step GUI/dedicated-server CI and independent same-SHA repeat before promotion. All Draft; `main` unchanged.
