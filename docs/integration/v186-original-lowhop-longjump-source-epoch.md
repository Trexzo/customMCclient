# V186 — LowHop and LongJump native original player readback lifetime

## Stable immutable original reference
The V181 exact commit `b485a7bc8314f21e019af4122d1cdca280291bcf` has now passed **two independent complete pinned Mojang Minecraft 1.8.9 original client+dedicated server executions**: first [workflow #39126842096](https://github.com/Trexzo/customMCclient/actions/runs/39126842096) successful 78 steps, and a rerun of the original job at the identical commit likewise 78 successful steps. Foundation Windows/Ubuntu also PASSED. **This is evidence only for V181**, not for new child revisions.

Immediate parent V185 Draft #651 adds HighJump/AirJump original Space and Y getter lifetime checks, with 11 offline tests and exact-head Foundation Windows/Ubuntu PASS; original full suite is separately in progress.

## Actual source-level LowHop and LongJump risks
LowHop mirrored HighJump's old flow: call `customMcJump()`, then set delayed Y adjustment even if original native jump callback reentered onEnable/onDisable; later `customMcMotionY()` could change current Vertical Speed/module owner before an obsolete native velocity reduction; finally overwrote new owner's original Space sample. These are real *source-code reentrancy risks*, not observed vanilla crashes.

LongJump similarly queued an X/Z boost after an external real `customMcJump()`, and measured real `customMcMotionX()` / `customMcMotionZ()` without post-getter owner checks. Reentrant module restart, changed speed/preserve settings, or changed physical WASD could lead to stale writes. The source also reread the same X/Z getters once for optional momentum preservation and again for setters, allowing an incoherent single callback across multiple original World updates.

## Implementation
- Both modules add per-activation `nativeJumpEpoch`, incremented on enable and disable, capture it across real `customMcJump()` and prevent an old jump from queuing a motion update for a new owner.
- Both only update held-Space tracking in finally if the same native module activation remains current, sampling the current physical key rather than the stale pre-callback value.
- LowHop captures the live Vertical Speed and epoch **before original Y getter**. The read result may change Y only if both remain current. Original lower-than-current adjustment semantics retained for valid real source.
- LongJump snapshots WASD, module epoch, current horizontal Speed and Preserve Higher Momentum policy before reading original X. After X it validates ownership **before reading original Z**; after Z it validates again before any setter; after X setter it validates again to stop stale Z writes. Original rotation snapshot, diagonal normalization, source-driven preserve-higher-vector policy and genuine W/A/S/D eligibility are kept.
- LongJump now reads each actual native horizontal axis once per pending jump callback, ensuring the optional preserve and direct setter use a consistent original source pair.
- No forged Minecraft native World entities, packets, key events, or bypass of original controller/FOV/GUI focus/UUID policy.

## Offline regression and release
12 JUnit original-interface adapter tests cover both LowHop old native jump OFF→ON, Y getter reentry, live Vertical Speed edit and actual Space release; real default LowHop; LongJump old native jump OFF→ON, X getter OFF→ON and physical W release, Z getter Speed edit, X setter disable, higher existing original momentum, and legitimate diagonal motion. These are offline Java policy, **not** genuine graphical/gameplay certification.

Cumulative Draft V186 child of V185 #651. Require exact V186 Foundation Windows+Ubuntu and full original Minecraft 1.8.9 X11/vanilla dedicated server suite, then same-SHA original repeat before declaring stable. No merge; `main` unchanged.
