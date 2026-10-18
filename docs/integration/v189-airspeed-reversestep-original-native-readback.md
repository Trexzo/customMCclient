# V189 – AirSpeed and ReverseStep original native motion owner leases

## Verified parent sources
V181 pinned original Minecraft 1.8.9 X11+vanilla dedicated-server workflow completed successfully in two independent 78-step same-SHA attempts. V182 Flight, V183 Step, V184 NoClip, V185 HighJump/AirJump and V186 LowHop/LongJump each subsequently passed one own exact-SHA full original 78-step CI run. V187 and V188 latest original jobs remain separately subject to completion. Nothing is inherited as runtime certification for V189.

## Production source-level defects resolved
AirSpeed applies real original local-player X/Z velocity only while airborne and an allowed movement key is held; its mapped X/Z getter can synchronously restart the module or change W/S/A/D, persisted Speed or Smooth Acceleration policy. Previously an obsolete read could issue stale X/Z writes. V189 adds a module lifecycle epoch and snapshots original physical keys and speed/smoothing/percentage before X getter; rechecks after X before reading Z, after Z before either setter, and after X setter before any Z write. Original source airborne status, yaw-relative diagonal normalization, smoothing interpolation, finite checks, and higher-priority suspension remain.

ReverseStep receives a real observed ground→air edge and its pre-existing delayed descent. The original player `customMcMotionY()` getter may reenter module lifecycle or change Delay, Sneak/Movement gate or Speed policy. Previously the consumed old transition could still overwrite the new module's original Y with stale descent. V189 snapshots activation epoch, configured Speed, Delay, Require Sneaking and Require Movement prior to original getter, then rechecks before any native Y setter; a revoked read returns false without replaying the old transition. The existing once-per-ground-contact lease, real Y rising/descending safety, GUI focus cancellation and no synthetic player movement remain intact.

## Offline test evidence (not native game certification)
12 adversarial Java JUnit interface adapter tests: AirSpeed X getter OFF→ON, W release, Z getter Speed/Smoothing edit, X setter disabling, genuine airborne/diagonal behavior; ReverseStep Y getter restart, Speed/Sneak/Delay edits, real departure consuming exactly one Y descent, rising/GUI focus veto. Only real original full binary CI is sufficient to certify gameplay and native controller/collision ownership.

Cumulative Draft child of V188 #654. Exact V189 SHA Windows+Ubuntu Foundation and complete Mojang Minecraft 1.8.9 X11 graphical+dedicated-server 78-step suite and unchanged-SHA repeat required before merge. `main` remains unchanged.
