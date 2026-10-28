# V359 — KeepSprint: one original-player post-attack restore

Minecraft Java 1.8.9 · Combat KeepSprint · stacked on V358 WTap #865.

## Verified prior gate
V358 exact-head Foundation CI passed Windows+Ubuntu at
[run 40703979341](https://github.com/Trexzo/customMCclient/actions/runs/40703979341).
The original Mojang native preflight still **FAILED** its independent V47
A/B KillAura rejoin / strict original controller + server-damage requirement
at [run 40703979631](https://github.com/Trexzo/customMCclient/actions/runs/40703979631).
This KeepSprint work does not claim to fix that targeting failure.

## Identified native weakness
After one admitted *synthetic melee attack* the old
`restoreSprintAfterSyntheticClick` consumed its pending original player
grant and called the real player's `customMcOnGround`,
`customMcSneaking`, and `customMcSprinting` getters; then it could call
`customMcSetSprinting(true)` without checking the captured player,
module lifecycle, GUI or WTap authority after each getter. A genuine
mapped getter can synchronously reenter the host and replace the
original local player or change the module's policy.

It also issued a redundant native `setSprinting(true)` when the vanilla
attack never cancelled the original sprint bit.

## New state rules
- `Minecraft189KeepSprintRestoreLease`: one-shot original-player restore
  scope, revoked on a nested restore, new synthetic pre-attack admission,
  new original sprint host callback, GUI suspension, or shutdown.
- KeepSprint pre-attack approval now also records its exact
  `keepSprintAdmissionSerial`. The pending grant is consumed *before*
  any native getter, and can never be replayed after exceptions.
- Check original player identity, exact admission serial, module
  lifecycle epoch, live settings, WTap ownership, and ClickGUI before
  the first getter, **after each of the three native getters**, and
  immediately before the actual native sprint setter.
- Never call the native setter if `customMcSprinting()` is already true.
  A successful KeepSprint restore still uses the *same original player's*
  `setSprinting(true)` only after a real vanilla sprint loss.
- No new packets, target selection, mouse interception, alternate
  original entity identity, synthetic server hit, or attack callback.
  Original clickMouse / PlayerControllerMP / dedicated-server evidence
  remains unchanged.

## Validation
- JUnit `Minecraft189KeepSprintRestoreLeaseTest`: single-owner state,
  nested same/different owner revocation and GUI/source invalidation.
- Extended JUnit `Minecraft189SprintHostSourceLeaseTest`: one-click
  restoration, no-op native write, callback source change, nested
  pre-attack admission, GUI/policy edit, WTap enable and getter failure.
- Python `scripts/test_mojang189_v359_keepsprint_one_shot_restore.py`
  checks source-bound original callbacks and no changes to original
  Minecraft controller/damage oracles.

Exact-head Ubuntu/Windows Foundation and original Mojang 1.8.9 native
preflight are required; offline test success is not native acceptance.
Do not merge this draft stack into main while V47 is failing.
No parallel visuals files are touched.
