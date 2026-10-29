# V361 — AutoRod final original-native right-click ownership

Minecraft Java 1.8.9 · GitHub-only combat draft · stacked on
V360 AutoBlock PR #867. No changes to parallel visual UI.

## Exact gap

AutoRod already uses a genuine original InventoryPlayer/currentItem
lease and revalidates the same source ItemStack at native use. The
V360 AutoBlock release can synchronously reenter other source callbacks.
The original vanilla GuiScreen veto was then checked *after* the first
AutoRod native item verification. The later screen/native getter can
replace currentItem, held ItemStack, original player or physical mouse
input before original `minecraft.customMcRightClickMouse()`. Reusing
the earlier source verdict risks a stale rod click.

## V361 changes

1. Host `mayExecuteAutoRodUse` pins exact `tickHotbarPlayer`,
   `tickHeldItemAccess`, original selected inventory, original lease
   slot, and **held-item callback generation** across the original
   `fishEntity` and verified selected ItemStack/native getter sequence.
   A nested same-player held-item refresh invalidates the stale
   generation even if the exact rod type did not change.
2. Re-evaluates the existing **Require Attack Held** and **Pause While
   Right-Clicking** policies from actual current pointer state before
   and after the native getters. Manual RMB takeover cannot authorize
   an old rod attempt. Default rod slot, discovery/protection, hook,
   cooldown and item identity policies are not changed.
3. Bridge retains original action ordering: AutoBlock release,
   first real rod verification, original vanilla GuiScreen veto,
   **renewed** native rod source verification, **second** original
   GuiScreen veto, then exactly one vanilla `rightClickMouse`, one
   completed original cooldown and finally safe slot restoration.
4. Source tests V344 are updated to require **nine** original screen
   veto calls: original seven, V360's added post-sword one and V361's
   added post-rod one. No original assertions are deleted.

Existing `Minecraft189VerifiedCombatItemSlot` strict source-native
type/stack/slot/ItemStack identity verification, module captured
policy, and hook preservation remain mandatory. The
`Minecraft189CombatCycleArbiter` still permits one primary action:
POTION > ROD > MELEE, with reblock only after melee attempt. No
packet or manual vanilla mouse interception is introduced.

## Verification and release gate

- `Minecraft189AutoRodNativeDispatchOwnerTest`: full host fixture
  proves original inventory/source, physical held/released LMB,
  physical RMB, native held-item same/different-player reentry,
  in-place policy change during rod getter, active-hook veto,
  native hook getter reentry, GUI and user hotbar takeover.
- `scripts/test_mojang189_v361_autorod_final_native_owner.py`:
  source ordering, native owner generation, exact held ItemStack
  and original gameplay/damage gate contracts.
- Require exact-head Windows and Ubuntu Foundation CI plus original
  Mojang Minecraft 1.8.9 binary acceptance. The inherited V47
  original B/A rejoin positive controller/server-damage failure
  remains an explicit blocker until actually repaired; this
  independent AutoRod improvement does not certify that path.

Draft only. Do not merge to main on offline evidence alone.
