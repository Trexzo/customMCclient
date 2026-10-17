# Movement V154 — fail-closed original grounded AutoJump delay policy

## Exact cumulative baseline

V154 is a Draft child of [V153 PR #619](https://github.com/Trexzo/customMCclient/pull/619) at commit `e7c8fad8c0ea767be6c67c891fa729767e5622b0`. The immutable original-game-certified V151 parent had both Windows/Ubuntu Foundation green and **two distinct full original pinned Minecraft 1.8.9 binary runs** at 78/78 passes each. V152 and V153 are independent original-game fixture stabilization changes whose exact-head native runs may still be pending; V154 cannot claim their success or substitute a parent's result.

## Real production change

`Minecraft189AutoJumpModule` already uses a mapped `Minecraft189PlayerMovementState.Snapshot` (onGround, native sprint/sneak) and the exact original `Minecraft189PlayerJumpControl.customMcJump`, with GUI focus and airborne-source rearming. It previously read the persisted `landingDelayTicks` **only on the first source landing callback** and retained a stale grounded countdown when the user edited the delay through ClickGUI/profile while that same ground contact was pending. After a countdown reached zero but was still blocked by a Require Forward/Sprint gate, editing the delay could allow the old grounded contact to emit a jump immediately, despite a different user-configured contact policy.

V154 pins the configured landing delay to the *specific observed source air-to-ground transition*. If `landingDelayTicks` changes **while that native grounded action is still armed** (including after the countdown expires while user input is gated), the native jump lease is revoked: no synthetic jump on that contact, and no replay when the user changes W/Sprint or closes the GUI. A **fresh actually observed airborne-to-ground transition** re-arms and takes the newly configured persisted delay. This is a source-policy guard; it does not forge airborne samples, modify original player.onGround or server position, invent a jump packet, or reduce movement/GUI fail-closed gates.

Existing default zero-delay behavior, initial ground eligibility, one native jump per contact, the typed persisted setting key `movement.autoJump.landingDelayTicks`, and optional require-forward/require-movement/require-sprinting/sneak gates remain unchanged. The native jump lease is consumed *before* calling the original source jump method, so exceptions still cannot double-jump.

## Regression coverage

Added two JUnit positive+negative regressions in `Minecraft189AutoJumpModuleTest`: (1) change a pending two-grounded-callback delay to zero and prove the old source contact will not jump, then verify a newly observed real airborne/ground contact jumps once; (2) wait for a one-update delay to finish while Require Forward blocks, change GUI delay to two, then verify a forward input does not spend the stale contact and a new landing respects exactly two updates before the original mapped jump. Existing extensive module/host/native source tests remain mandatory.

## Mandatory quality bar

Require exact-head Foundation Ubuntu+Windows on V154, genuine Mojang 1.8.9 binary GUI/dedicated-server preflight unchanged, full original M405 UUID/PvP and M425 original ray/hurt/attack checks (all still strict), and a same-SHA repeat if intermittent checks fail. Unit-only PASS is not original-game proof. The branch and all ancestors remain Draft; no merge and no change to `main`.
