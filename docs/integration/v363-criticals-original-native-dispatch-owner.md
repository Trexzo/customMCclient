# V363 — Criticals original-player final native melee admission

Minecraft Java 1.8.9 · Combat Criticals / HitSelect · draft stacked
on V362 AutoPot PR #872. No parallel HUD/ClickGUI/ArrayList edits.

## Native source gap

The existing Criticals module gates a *scheduled* synthetic attack
against cached host-tick movement (onGround), fallDistance and motionY.
But the three original-game controller lanes—TriggerBot native
attackEntity, KillAura native attackEntity, and vanilla AutoClicker
clickMouse—previously checked HitSelect, UUID, and vanilla GuiScreen
*without rechecking Criticals immediately before the actual action*.

A mapped native World/slot/ray getter may synchronously reenter with a
different local player, land on the ground, change vertical motion,
disable/re-enable Criticals, toggle a conflicting movement module, or
edit the minimum-fall/descending policy. Those transitions cannot
authorize a stale automatic critical attempt.

## V363 behavior

1. Criticals now increments a `lifecycleEpoch` on every enable/disable,
   and offers `permitsOriginalNativeFall(onGround, fall, motionY, busy)`.
   It is a pure original-value policy (no ground/motion writes, jumps,
   packets or invented server critical damage).
2. The host captures one scheduler admission serial, exact real
   source player reference, original source-generation counter, module
   activation epoch and Criticals GUI settings **only after** the
   original HitSelect, UUID, range, movement, and GUI scheduling vetoes.
   `playerPosition`, `playerHotbarSlot` and `playerHeldItem`
   callbacks invalidate the source generation, including same-player
   A→B→A native reentry.
3. At native dispatch, all three controller lanes call
   `host.mayDispatchOriginalCriticals()` before the final HitSelect
   UUID/hurt-time and vanilla GuiScreen gates. When Criticals is OFF,
   the default behavior remains unchanged—but turning it ON mid-
   callback cannot borrow an earlier disabled admission.
4. When Criticals is ON, the native gate demands exactly the original
   `tickHotbarPlayer == tickHeldItemAccess` player source and original
   mapped `customMcOnGround()`, `customMcFallDistance()` and
   `customMcMotionY()` getters, **read twice**. Every getter has an
   immediate owner-generation veto, the two triplets must agree, and
   the actual values must satisfy live natural falling and module
   movement-owner exclusion. Live Criticals policy and GUI are
   revalidated again before any controller attack.
5. Nothing fabricates target UUID, hurtTime, server damage, fallDistance,
   movement packets or a Minecraft click. The existing per-UUID
   HitSelect READY→WAIT_FOR_HURT→WAIT_FOR_READY state machine and strict
   original-game PlayerControllerMP/server-damage acceptance remain intact.

## Verification

- `Minecraft189CriticalsModuleTest`: pure original airborne/falling
  gate, finite values, descending policy, minimum-fall threshold,
  conflicting movement-owner denial and activation epoch.
- `Minecraft189CriticalsNativeDispatchOwnerTest`: deterministic
  original-host source fixture and ten cases covering legitimate
  descending, ground landing, falling reset, in-getter fall mutation,
  same- and different-player callback reentry, OFF→ON lifecycle,
  live setting change, disabled-module compatibility and NoFall.
  This unit fixture does not certify genuine original game behavior.
- `scripts/test_mojang189_v363_criticals_final_native_owner.py`
  inspects all three original native dispatch paths, the independent
  HitSelect UUID recovery check and dedicated-server oracles.

Exact-head Ubuntu/Windows Foundation and original Mojang 1.8.9 binary
preflight are required. V362 Foundation passed, but inherited V47
original KillAura A/B rejoin controller/server-damage still failed.
Never weaken that oracle, claim native acceptance prematurely or
merge this stack into main while it fails.
