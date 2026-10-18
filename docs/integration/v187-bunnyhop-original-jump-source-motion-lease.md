# V187 — original BunnyHop jump contact owner and native X/Z motion lease

## Basis
V181's unchanged source SHA `b485a7bc8314f21e019af4122d1cdca280291bcf` has genuinely passed the **full original Mojang Minecraft 1.8.9 78-step X11/vanilla dedicated-server workflow twice** in independent attempts, plus Windows/Ubuntu Foundation. This is a verified baseline, not a claim of subsequent cumulative V182–V187 runtime certification.

V185 #651 and V186 #652 fix HighJump/AirJump and LowHop/LongJump source jump/velocity lifecycle. Their new Java regressions are offline adapter evidence; full original binary CI is independent for each exact SHA.

## Actual production source risks in BunnyHop
`Minecraft189BunnyHopModule` already used true local ground/WASD snapshots, live landing delay, GUI focus disarming, and original `customMcJump()` and `customMcMotionX/Z()` accesses. Two remaining hazards:
1. It consumed its grounded, armed lease **after** calling the original player's external mapped `jump()`. If that method synchronously disabled and reenabled BunnyHop, the old callback could incorrectly set **the new activation's `armed=false`**. A thrown native jump also left an old ground-contact lease armed, permitting repeat jump after a failed callback.
2. Horizontal motion reads original X, then Z; source getters and mapped X setter may reenter module OFF→ON, current speed/smooth acceleration settings or physical WASD. Prior code could issue old X/Z writes from invalidated original source/policy, including a stale Z after X setter already disabled its module.

## Implementation
- Add `nativeHopEpoch`, incrementing every onEnable/onDisable.
- **Before** calling original native player `customMcJump()`, consume the old ground-contact `armed` and `eligibleLandingCallbacks` lease. If the callback restarts a module, the old action cannot overwrite the **new** activation's armed state. Native failure cannot re-play the previous grounded jump. Existing landing delay and GUI controls preserved.
- In horizontal motion, snapshot original W/S/A/D, speed, smoothing enabled, acceleration percentage, and activation epoch before mapped original X getter; validate the same exact source input/current policy after X getter **before reading original Z**, after Z **before any setter**, and after X setter **before issuing Z**. If stale, return without old remaining velocity writes.
- Retain original yaw/diagonal normalization, real mapped X/Z values used by smooth interpolation, nonfinite protections, unchanged actual GUI/freeze/flight arbitration, and no synthetic packets/World/key/controller behavior.

## Regression and exact-head gate
Eleven offline JUnit Java-adapter tests cover original jump OFF→ON before grounded lease rearm; throwing mapped jump cannot repeat on same ground contact; X getter restart or W release; Z getter changed Speed or acceleration GUI setting; X setter disable or Smooth Acceleration edit; valid normalized W+A diagonal, genuine source progression 0.095 → 0.16625 in smooth mode; and suspended/unknown rotation skipping native reads. These are **not authentic Mojang graphical/gameplay pass evidence**.

V187 is a cumulative Draft child of V186 PR #652, carrying V185/186 and earlier original movement fixes. Require exact V187 Windows+Ubuntu Foundation, full original Mojang Minecraft 1.8.9 X11/vanilla dedicated-server workflow (all 78 steps), and second unchanged-SHA full original attempt before promotion. `main` remains unchanged.
