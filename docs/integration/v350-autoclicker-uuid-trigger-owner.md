# V350 — Auto Clicker trigger-mode UUID owner

Minecraft Java 1.8.9 | Dedicated combat feature PR | Draft

## Why this combat module comes next

The combined V342–V349 combat runtime already serializes original-source
primary actions as **AutoPot > AutoRod > melee**, with optional post-melee
AutoBlock. KillAura and dedicated TriggerBot have their own identity
contracts. The legacy Auto Clicker `triggerMode` additionally confirms an
original crosshair-player hit, but previously counted consecutive
confirmation frames and CPS phase without tracking the actual player's
UUID. When World loadedEntityList reuses an index for a different player,
one target's scheduler could carry credit to the next.

## V350 changes

- Preserve the original physical-hold Auto Clicker path unchanged.
- In the indexed native HostRuntime callback, when Auto Clicker's trigger
  mode owns the melee lane, match the actual original World UUID list to
  the current indexed source snapshot and independently check source
  player-kind/local-owner and combat list lengths. Missing, mismatching,
  throwing, non-player or local-owner evidence fails shut.
- Retain the verified UUID inside the Auto Clicker scheduler. A different
  UUID, lost crosshair, missing native proof, GUI focus or failed input
  eligibility resets its target confirmation and **all** CPS/ramp/burst
  phase. There is no inherited next-target click.
- Check the module's OFF/ON epoch across original World getters. A
  reentrant callback or configuration change cannot revive a stale
  trigger owner; the existing host admission serial still protects nested
  callbacks. Refresh pointer/keyboard inputs after native getters.
- At final shared click admission, revalidate the same original World
  snapshot and current native loaded UUIDs; preserve all existing
  HitSelect, Antibot, Team, Friend, Wall, range, Criticals and GUI gates.
- Unindexed overloads are deliberately retained for existing Java test
  fixtures and are not used in the original indexed bridge callback.
  No changes to original vanilla `clickMouse`, player-controller arguments,
  physical mouse handling, packets, `leftClickCounter`, target UUID
  manufacture or server damage authority.
- The previous combat arbiter, AutoPot/AutoRod priority and AutoBlock
  reblock-only-after-melee remain untouched. The UX lane is untouched.

## Tests and honest proof boundary

`Minecraft189AutoClickerNativeTargetUuidTest` covers A→B→A target reuse,
consecutive confirmation, missing UUID and ray evidence, fractional
phase reset, GUI suspension, OFF→ON epoch and ordinary hold-mode parity.
`scripts/test_mojang189_v350_autoclicker_uuid_owner.py` asserts native
source wiring and unchanged shared arbiter priority.

Require exact-head Foundation Windows/Ubuntu and official SHA-verified
original Mojang Minecraft 1.8.9 + vanilla dedicated-server acceptance.
These source and unit tests do **not** constitute successful real-player
controller UUID, hurt-time or server-damage evidence. V349 native
acceptance must still pass on the precise integrated head. Keep draft
and never merge to main automatically.
