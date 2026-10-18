# V198 — original PlayerControllerMP SpeedMine hit/progress getter owner epoch

## Verified and failed original precedent
The genuine pinned Minecraft 1.8.9 official X11/vanilla dedicated server suite completed successfully on V194 and V195 exact SHAs, each 78/78 job steps, on top of multiple earlier exact SHA originals. V196's native suite [#39137125584](https://github.com/Trexzo/customMCclient/actions/runs/39137125584) **FAILED** at a real rejoin UUID target phase4; native B was out of 3.5 source reach after a redundant B reteleport. V197 Draft #663 removes that extra B reteleport, keeps verified all-three original stone floor and the intact 3D/UUID/B-only damage admission. Its pinned original runtime was separately in progress when this file was written. V196 failure is not converted to a pass.

## Production source issue
`Minecraft189SpeedMineModule` directly reads the original mapped Minecraft 1.8.9 PlayerControllerMP mining state via `customMcIsHittingBlock()` and `customMcBlockDamageProgress()`; if eligible, it writes back a bounded progress minimum via `customMcSetBlockDamageProgress(float)`. The original native getters can execute synchronous application callbacks, such as module disable→enable, a GUI profile setting change, or a real controller stop-mining transition.

Before V198 the module checked current module/GUI/sneak/ground eligibility only *before* both getters. It could use obsolete native progress from a previous enable cycle or after mining stopped, and write stale progress into a new mining session. This is an identified Java source reentrancy gap, not a claimed observed Mojang crash.

## Actual fixes
- Per-activation `nativeMiningEpoch`, incremented during every enable and disable.
- Capture true configured active-minimum progress, progressive toggle and step, Require Attack Held, Pause Sneaking, Ground Only, Airborne Override and Airborne Progress **before** calling the genuine original hit getter. After that source call, require exact same module activation and persisted policy or stop before reading progress.
- After actual original progress getter, repeat the same owner/policy validation before calculating target.
- Preserve genuine original state/airborne source selection, finite progress constraints, optional bounded incremental mining and existing GUI focus veto. No guessed blocks, packets, fake mining source, synthetic break events or progress beyond configured cap.
- Before the actual native setter, read **real original `isHittingBlock` a second time**, then revalidate exact same epoch and settings, preventing a block that has stopped being mined while the native progress getter executed from receiving the obsolete progress. Source block identity isn't exposed by this mapped interface, so V198 does **not** claim to prove unchanged block coordinates across a simultaneous switch from block A to B; the new explicit native still-hitting check covers the confirmed original stop/no-hit case.

## Eleven offline original-controller Java regression tests
Adversarial real-interface adapters cover source hit getter OFF→ON; progress getter OFF→ON and SpeedMine minimum profile edit; original hit flag changing false during progress; second source hit getter disabling module or toggling progressive; true bounded progress ramp, original mapped airborne progress, ground/sneak and real ClickGUI veto before native reads, and original source not hitting. These are offline policy regressions, **not original binary runtime proof**.

Cumulative Draft child of V197 #663, inheriting strict original rejoin native geometry, Box ESP true glReadPixels and original FOV/GameSettings transaction. Require exact V198 Foundation Windows+Ubuntu, all 78 genuine pinned Minecraft 1.8.9 X11 graphical/vanilla dedicated-server steps, then independent same-SHA native repeat before certification. Keep `main` unchanged.
