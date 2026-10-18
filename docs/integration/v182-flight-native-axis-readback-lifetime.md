# V182 — production Flight original native XYZ readback lifetime

## Baseline certainty
Parent **V181 exact head** `b485a7bc8314f21e019af4122d1cdca280291bcf` has both [Foundation Windows/Ubuntu PASS](https://github.com/Trexzo/customMCclient/actions/runs/39126842100) and a **real complete pinned Mojang original 1.8.9 78-step successful first run** [#39126842096](https://github.com/Trexzo/customMCclient/actions/runs/39126842096). An independent attempt of the exact V181 official original-binary job was requested; this is still separate from V182's own source verification.

## Actual production source correctness gap
`Minecraft189FlightModule` has always derived movement from genuine mapped original player-motion getters and Java keyboard state. Before V182 it checked module enable and GUI focus **before** calling real mapped `customMcMotionY()`, `customMcMotionX()` and `customMcMotionZ()`, but if those real getters reentered lifecycle/keyboard/setting changes, the existing callback could still write old Y/X/Z velocity or spend new mode state. An X setter could also reenter disabled module/changed GUI policy and be followed by a stale Z setter from the same previous callback.

This is a code-level race/reentrancy risk, **not** an independently observed V181 gameplay crash.

## Implemented native owner lifetime
- Per-module `nativeReadEpoch` increments on every `onEnable` / `onDisable`. A reentrant OFF→ON cannot inherit a previous native getter's permission.
- **Vertical:** cache actual Space/Shift state and persisted vertical speed/smooth/step *before* reading original Y; after Y getter, require same lifetime, current physical held directions, and unchanged live GUI policy. A stale read issues no Y setter. After genuine vertical setter, an OFF/ON cancels horizontal stage instead of carrying a stale callback forward.
- **Horizontal:** cache actual W/S/A/D and persisted horizontal speed, sprint boost/multiplier, smoothing/step *before* reading original X. Recheck all policy/lifetime/keys **after X**, before touching original Z; then **after Z**, before either setter. An X setter that reenters lifecycle or GUI policy stops old Z write. Existing yaw, diagonal normalization, authoritative sprinting snapshot, smoothing and finite-motion logic remain unchanged for valid current callbacks.
- **GUI focus** still vetoes all three original getter invocations; no synthetic reset, packet, input faking, or new Minecraft permissions.

## Offline regression
Ten JUnit Java interface-adapter adversarial tests covering lifetime restart mid Y/X; vertical GUI speed change and Space release during original Y getter; horizontal W key release mid X; horizontal speed change mid Z; X setter disabling Flight / editing smoothing; vertical setter disabling Flight; and true GUI-focus omission of all three getters. These tests prove **offline Java callback policy**, not genuine Mojang gameplay.

Cumulative Draft child of V181 PR #647. Exact V182 head Foundation Windows/Ubuntu plus entire original Mojang Minecraft 1.8.9 X11/dedicated original suite required, not inherited from V181. Parent success cannot be transferred to child. Remain Draft, `main` unchanged.
