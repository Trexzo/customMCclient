# V364 — HitSelect recovery begins only after real native controller entry

Minecraft 1.8.9 · combat HitSelect / Criticals / melee controller · draft
stacked on V363 PR #874. No UI/visuals files touched.

## Verified scheduling gap

The existing optional HitSelect per-original-UUID recovery state machine
uses `READY → WAIT_FOR_HURT → WAIT_FOR_READY → READY`. V357 wrote
`WAIT_FOR_HURT` inside the synthetic **scheduler** after its initial
Criticals, source UUID, range, GUI and module-epoch checks. But the
V363 final **native** Criticals check, later GuiScreen check, native ray
identity recheck, original AutoClicker controller token, and vanilla
clickMouse leftClickCounter are downstream of that transition. A
scheduler-approved attempt vetoed there never reaches the original
PlayerControllerMP.attackEntity; it cannot produce a real target hurt
edge and leaves that UUID in `WAIT_FOR_HURT` indefinitely.

## V364 change

- The scheduler still validates HitSelect's exact original remote UUID,
  hurt window and source/activation epoch. It **never advances
  `WAIT_FOR_HURT`** merely because a click was scheduled. The
  backward-compatible `noteScheduledOriginalClick` is intentionally
  a no-op.
- Each of the three native attack lanes (TriggerBot, KillAura and
  vanilla AutoClicker clickMouse) installs a **synchronous one-shot
  source-UUID/HitSelect epoch/settings intent** only after final native
  Criticals, HitSelect UUID/hurt-time and original vanilla GuiScreen
  checks. Each native call clears it in `finally`, including exceptions,
  native cooldown vetoes and failed controller ownership.
- The existing transformed original
  `PlayerControllerMP.attackEntity(EntityPlayer,Entity)`
  observation receives the ACTUAL vanilla Entity argument and is
  the only location advancing `READY → WAIT_FOR_HURT`.
  An exact matched remote UUID, unchanged HitSelect enabled/recovery
  policy and activation epoch, unchanged min/max hurt window,
  still-current observed original UUID and current READY state are all
  required. The one-shot intent is consumed before verification.
- Manual vanilla mouse clicks have **no synthetic HitSelect intent**;
  mismatched A/B UUID, unexpected reentrant controller, source reset,
  GUI suspension, module OFF→ON, or changed setting cannot consume the
  wrong player’s hurt/recovery credit.
- Existing V357 genuine original hurt rise (> max), then recovery back
  inside [min,max] is still required before another owned synthetic hit.
  No timeout claims fake damage. A real controller entry is only an
  attempted attack; an actual server damage/hurt event must still be
  observed. No synthetic controller calls, packets, UUID fabrication,
  movement writes or additional player attacks are added.

## Tests

- `Minecraft189HitSelectControllerRecoveryTest`: scheduler-only
  nonconsumption, exact native original UUID, double-entry veto, A/B
  per-player recovery isolation, OFF→ON/GUI/source epoch invalidation,
  settings change, and no fabricated hurt/ready edge.
- `scripts/test_mojang189_v364_hitselect_real_controller_recovery.py`:
  three exact native attack scopes, per-action finally cleanup,
  real controller observer, untouched original controller/UUID and
  dedicated-server acceptance.
- Exact-head Windows/Ubuntu Foundation CI and genuine original Mojang
  1.8.9 binary preflight remain mandatory.

V363 Foundation passed both platforms but inherited original-game V47
early A/B rejoin B-controller + dedicated-server-damage acceptance still
failed. V364 does not claim to fix that independent blocker. **Draft
only; do not merge until native acceptance and server damage pass.**
