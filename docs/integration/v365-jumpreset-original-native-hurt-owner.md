# V365 — JumpReset exact original hurt/movement source before native jump

Minecraft Java 1.8.9 · combat JumpReset · stacked on V364 HitSelect
PR #875. Parallel visuals are out of scope.

## Audit finding

JumpReset already consumed only a rising **local-player hurt-time** edge,
sampled its chance once, committed cooldown before a possibly partially
executed original `EntityPlayer.jump()`, and guarded recursive native
jumps. The host jump-source arbiter already made JumpReset subordinate
to active LongJump, HighJump, LowHop, BunnyHop, AirJump and movement
suspension, sharing one native jump lane with movement automation.

However, a scheduler-approved edge could still use **cached**
`Minecraft189PlayerHurtTimeState` and
`Minecraft189PlayerMovementState` while an original native callback
changed the actual local hurtTime, grounded state, sneaking, sprinting or
player source before the real `customMcJump()`. Cooldown was committed
before any such genuine per-source final readback.

## V365 contract

1. `Minecraft189JumpResetModule.apply` has an overload accepting a
   production-owned read-only `BooleanSupplier` admission. It
   *reserves the reentrant module lane* before executing native getters.
   A failed final admission consumes the already observed hurt edge
   without spending cooldown or reporting a synthetic jump.
2. Host tracks the true player source that published each independent
   native `playerMovementState` and `playerHurtTime` observation,
   invalidated by every source callback/position generation. The
   supplied jump source must implement both source-mapped interfaces
   and be the **exact Java original player** for both observations.
3. The host demands two matching readbacks of the original player's
   `customMcHurtTime`, `customMcOnGround`,
   `customMcSneaking` and `customMcSprinting`. Owner generation
   and unchanged GUI/jump lease are verified after *every* getter.
   HurtTime must match the one edge admitted by the module; current
   state must agree with the published movement snapshot and current
   physical forward policy. Other jump/movement module ownership is
   checked again.
4. **After** successful original getter validation and unchanged
   module activation/policy, cooldown is committed immediately before
   precisely one original `customMcJump()`. If vanilla physically
   jumps and then throws, earlier consume-before-jump and reentry
   exception behavior remains intact—there is no simulated rollback.
5. Other jump module order and their original callbacks are unchanged,
   and the module's unindexed test overload does not manufacture
   original EntityPlayerSP getter evidence. No new packets, movement
   writes, fake hurt events or claimed server outcomes.

## Evidence and release

`Minecraft189JumpResetNativeSourceTest` covers exact source owner and
one hurt edge, changed real hurt, ground/sneak/sprint native getter
interference, nested same/different source callbacks, actual W input,
and rejected admission not consuming cooldown. Existing JumpReset
native exception/reentrancy tests remain required.

`scripts/test_mojang189_v365_jumpreset_original_hurt_gate.py`
requires the production double-read source, the module admission ordering
and original jump source lease.

Require exact-head Windows/Ubuntu Foundation and real original Mojang
1.8.9 binary acceptance. Parent V364 passed Foundation but remains
blocked by inherited V47 rejoin B-controller/server-damage failure.
**Draft only** until genuine game authority checks pass.
