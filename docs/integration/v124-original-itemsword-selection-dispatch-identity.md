# V124 — source-mapped ItemSword identity at selection and final attack

## One review surface and certified base

**Certified starting point:** V121 #587, commit
`4b465149cdbbddc41cffec9b6a6eebdb7428ead5`: Foundation on both
Windows and Ubuntu, and the complete pinned original Mojang 1.8.9
graphical/dedicated-server CI **SUCCESS**. This is the rollback anchor.

**Strict child lineage:**
- V122 #588: production AutoWeapon final source ItemSword identity and
  mapped stack verification, with actual HostRuntime attack dispatch gate
  and adversarial original inventory source-interface tests.
- V123 #589: production selection-time revalidation **after** native
  InventoryPlayer.currentItem setter, with owner-safe rollback if the
  selected source sword changed during the setter/getter.
- **V124**: cumulative integration documentation, no independent
  production behavior or relaxed historical tests.

The exact GitHub V121→V123 comparison confirms **six linear commits
strictly ahead**, **zero behind**, in four Java production/test files
before this integration document. Avoid merging both children and the
aggregate as separate overlapping code paths. All branches remain
Draft; `main` unchanged without explicit user authorization.

## Why source native class identity matters

Previously, AutoWeapon ranked verified `ItemSword` classes in
`InventoryPlayer.mainInventory` and selected the highest verified
base-damage slot. HostRuntime later confirmed that the selected source
`currentItem` index was unchanged before the actual original Mojang
`Minecraft.clickMouse` or `PlayerControllerMP.attackEntity` dispatch.

However, an original ItemStack at that same index may be replaced by
vanilla inventory synchronization or a reentrant callback between
item selection and attack. The index alone cannot certify the sword
still exists or is the same source Item type.

The already available `Minecraft189ClassTransformer` injects
`Minecraft189ItemStackAccess.customMcOriginalItemIdentity()` directly
from the **original obfuscated ItemStack Item field**; it does not
read a display name or require stable ItemStack-wrapper Java identity.
This mapping was inspected at exact source in
`Minecraft189ClassTransformer.java`. It is a real vanilla source
item reference; a new ItemStack object for the same `Item` singleton
should be allowed.

## Production correctness changes

| Stage | Verified invariant |
| --- | --- |
| AutoWeapon initial scan | Choose only positively counted, source-mapped ItemSword with finite positive source base damage; preserve ranked tie rules |
| Source Item identity capture | Record original `Item` singleton when mapped, source ItemSword base damage, selected slot, previous slot and original inventory reference |
| Native hotbar setter | Confirm the original currentItem points at the chosen verified sword slot, source module remains enabled and native inventory owner still holds the one-action lease |
| **V123 setter postcondition** | Re-read source selected ItemStack after the native setter; if no sword, depleted, different Item reference, changed damage or reentrant user slot mutation, fail selection and roll back only if module still owns its original selected target; clear lease in `finally` |
| **V122 final action postcondition** | The actual HostRuntime gate re-reads source mapped original inventory items **immediately before** the original attack callback; requires same inventory/previous-slot lease, selected currentItem, enabled module, ItemSword type, positive count, finite unchanged base damage and same mapped native Item singleton when available |
| Source callback reentrancy | Verify currentItem and module ownership **again after** native inventory/Item getters; GUI focus and source player/inventory identity are also rechecked at HostRuntime |
| Genuine vanilla no-switch attack | Retains preexisting vanilla click semantics when no temporary source combat item owner exists; fixed CombatSlot does not require sword ranking; AutoWeapon-only strict item identity affects its own verified selected sword lease |
| Synchronous native finally | Release only the recorded original inventory and exact original previous-slot token. A genuine user-selected different slot beats synthetic restoration |
| Original target authority | Preserve historical PvP UUID owner/lock, FOV, wall/range, Scaffold and dedicated-server negative controls unchanged; this work does not infer server-authorized hit or damage |

### Explicit limitations

The source `Item` singleton identity can prove that a vanilla
ItemStack wrapper was replaced with one for the same item, **not**
the persistence of the exact ItemStack instance, enchantments, NBT,
durability, weapon DPS or server inventory authorization. If a
non-native test adapter supplies null `customMcOriginalItemIdentity`,
the source-based class, stack count and finite base damage checks
still operate, but identical-looking substitute swords cannot be
distinguished by that optional identity. Do not claim otherwise.

Original Java host tests with source-interface doubles verify
deterministic behavior; they do **not** prove a real original Mojang
server sent an ItemStack replacement during an attack. The already
certified original-client physical X11 Step, NoClip, Reach and
CombatSlot tests remain required on V124 as regressions, not a
dedicated new in-game AutoWeapon sword-swap proof.

## Adversarial tests added in V122–V123

1. Original captured native ItemSword remains valid if Minecraft
   replaces its ItemStack wrapper but preserves the same original
   Item singleton and base damage.
2. Source selected sword becomes a non-sword or zero-count stack:
   reject actual melee dispatch.
3. A new sword with different base damage or different original
   Item singleton at the same native slot: reject dispatch.
4. Original inventory getter changes currentItem or opens actual
   CustomMC ClickGUI **while** verification is running: reject.
5. Native selected-slot setter itself replaces the selected sword
   with a non-sword or different Item singleton: V123 prevents
   publishing a successful selection/lease and restores old slot.
6. Original source getter changes user's selected currentItem
   during the post-setter revalidation: preserve the user's
   independent slot, clear the old temporary lease, and permit
   a later clean source selection.
7. Module disable/exception and prior user-selection protections
   remain from V106/V111/V119/V121; no existing tests removed.

## Mandatory certification before integration

- Exact **V124 head** Foundation CI SUCCESS on both Windows and Ubuntu,
  including all unit, lifecycle and source host regressions.
- Exact **V124 head** complete pinned original Mojang Minecraft
  1.8.9 binary/dedicated workflow SUCCESS including the genuine
  X11 Step/NoClip/Reach/CombatSlot state proofs and all historical
  UUID/rejoin, real original Aura/PvP, FOV, Scaffold/world negatives.
- Track V122 Windows initial dependency fetch error carefully:
  the first CI attempt failed with Maven Central **HTTP 403**
  resolving ASM `asm-analysis`, not a verified Java regression;
  same-commit failed-job rerun later passed both Foundation platforms.
  V122 original binary and V123/V124 separate exact-head jobs
  still require their own conclusions.
- Never claim full original-game sword ItemStack replacement or
  server damage evidence from focused host doubles.
- All child and cumulative PRs Draft, no `main` merge, and
  V121 certified commit retained as rollback.

This technical evidence record describes verified ancestry and
required future gates; it does not declare V124's pending official
binary workflow complete.
