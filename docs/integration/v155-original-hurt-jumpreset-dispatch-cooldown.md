# Combat V155: exact original JumpReset hurt-edge dispatch ownership

## Immutable parent and production bug

The V155 Draft branch is stacked directly above [Movement V154 PR #620](https://github.com/Trexzo/customMCclient/pull/620) at commit `b8e41690b7ae957accafaadd631d8dcf9230b474`. Earlier immutable [V151 PR #617](https://github.com/Trexzo/customMCclient/pull/617) had two complete original pinned Mojang 1.8.9 GUI/dedicated-server jobs green 78/78 at the same unchanged SHA. V152–V154 native complete certification remains separately gated by exact-head CI; parent passes from other SHAs must never be used as proof for V155.

`Minecraft189JumpResetModule` uses the mapped actual local `hurtTime` state and source player movement snapshot to issue one native `Minecraft189PlayerJumpControl.customMcJump()` on a fresh local hurt-time edge. Previously it set the configured cooldown **only after** the native player jump returned. If the genuine original method performed a partial jump then threw, the cooldown never became owned. A synchronous native callback that reentered `apply` with another higher observed hurtTime could also try to issue another jump before the first completed, particularly when configured Cooldown Ticks = 0. Neither case is a second independent source owner action.

## Real production changes

- `Minecraft189JumpResetModule.apply` commits its current original hurt-event cooldown **before** calling the original mapped jump method. A native exception no longer retroactively permits a second hit to evade the cooldown.
- While its own native jump is in flight, `apply` forbids reentrant dispatch and consumes the currently observed hurt sample rather than accumulating that nested edge for the next callback. The guard is safely released in `finally` even if original `customMcJump` throws.
- Legitimate subsequent independent hurt edges remain eligible under the *original configured cooldown* and original onGround, sneak, forward, sprint, chance and suspension gates. No gameplay source fields, target UUID, fake damage, movement packet or game state are changed.

## Regression coverage and constraints

Three new JUnit tests in `Minecraft189JumpResetModuleTest` cover (1) original jump partially mutating then throwing with cooldown preserved across fresh hurt edges, (2) zero-cooldown nested source jump with a *new* hurtTime sample that must not recursively spend the original action nor replay on the next call, and (3) native reentrant throw releasing the guard for an actual later hurt event. Existing edge/cooldown/chance/sprint/cleanup settings tests remain unchanged.

Exact-head release gate requires Windows and Ubuntu Foundation plus full original Mojang 1.8.9 binary GUI/dedicated server acceptance. These source-unit regressions demonstrate Java lifecycle policy, **not** physical in-game JumpReset behavior on a multiplayer server. Additional dedicated genuine hit-to-JumpReset proof would be necessary to claim the specific JumpReset effect certified. Strict M405 original rejoin UUID, M425 physical vanilla mouse, original PvP damage, Render, Scaffold, original AutoRod/AutoPot use and prior negative gates must remain mandatory. The branch stays Draft and `main` remains untouched.
