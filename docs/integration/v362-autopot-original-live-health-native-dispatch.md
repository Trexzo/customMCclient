# V362 — AutoPot original-player live-health native dispatch

Minecraft Java 1.8.9 · combat AutoPot · GitHub-only draft
stacked on V361 AutoRod PR #869; parallel visuals untouched.

## Source gap

AutoPot `shouldAutoPot()` initially admits a scheduled potion from the
existing mapped health snapshot and physical right-button state.
`selectAutoPotSlot` and `mayExecuteAutoPotUse` correctly verify the
original selected inventory, mapped ItemPotion and selected ItemStack.
However, except for the optional Instant Healing II *conservation ranking*
mode, `mayExecuteAutoPotUse` did not recheck the original player's **live
health percentage** before actual `minecraft.customMcRightClickMouse`.
Original item, health and screen getters may synchronously reenter other
callbacks after the initial low-health scheduling decision.

A player who naturally healed or was replaced during this window could
otherwise spend a potion based on a stale health HUD reading. Physical
manual RMB can also take ownership after the earlier scheduling check.

## V362 boundary

- Preserve `mayExecuteAutoPotUse(int)` as **read-only actual source
  ItemPotion/currentItem verification**. The genuine V132 Mojang
  `/give` inventory-discovery acceptance must be able to prove item
  identity even at full health without creating a fake damage event.
- New production-only `mayDispatchAutoPotNativeUse(int)` first validates
  the existing exact potion lease. It then checks the *same original
  EntityPlayer* implements `Minecraft189PlayerHealthAccess`, and reads
  genuine `customMcMaxHealth()` and `customMcHealth()`. Both finite,
  strictly positive, current less than max, and current health percentage
  within the module's **live setting** are mandatory.
- Bracket the actual ItemPotion readbacks with original player / held
  source / original inventory reference checks; read original health
  again afterward. Any health change, source replacement or GUI change
  vetoes the synthetic potion right-click.
- A physical right-click held by the user vetoes final dispatch, even
  if it was not held during initial AutoPot scheduling. Recheck original
  vanilla GuiScreen *after* native health/item reads in the bridge's
  admission callback.
- No altered Pot > Rod > Melee combat priority, player controller,
  manual mouse input, source potion selection, cooldown timing,
  `AutoPotPitchLease` source-owned orientation or post-click native
  potion use completion. Existing source-only health conservation and
  Regeneration guards remain mandatory. Never infer a potion heal from
  an attempted right-click: original server health gain is separate.

## Verification

- `Minecraft189AutoPotLiveNativeDispatchTest`: real host low-HP
  positive, read-only full-HP item discovery, recovery/invalid HP veto,
  physical RMB takeover, source getter reentry, changed player,
  GUI and ItemPotion getter interference, changed threshold, pure
  native live health predicate.
- `scripts/test_mojang189_v362_autopot_live_native_health.py`:
  non-bypassable source/GUI/physical input boundary, genuine original
  ItemPotion and server-health acceptance requirements.
- Require exact-head Foundation Windows and Ubuntu plus original
  Mojang 1.8.9 binary preflight. The V361 V132 native discovery
  regression was specifically isolated and corrected in the branch
  below this draft; V47 original KillAura B/A controller/server damage
  remains a distinct release blocker until verified positive.

Draft only. Do not merge to main on source contracts or Foundation alone.
