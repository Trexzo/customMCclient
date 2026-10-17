# V129 — original Rod/Potion selected ItemStack source stability

## Scope and integration ancestry

**Certified rollback ancestor:** `integration/v126-original-itemsword-native-transaction-authority`
(PR #592), head `c5b13602ac3aee7fce618a45d20c83cc9d82074b`.
Foundation Windows + Ubuntu and the complete pinned original Mojang
Minecraft 1.8.9 graphical/dedicated-client workflow **SUCCESS** on that
exact head. This is the verified cumulative source baseline.

- V127 #593 adds source **selected ItemStack wrapper membership**
  checks to AutoRod and AutoPot at both original slot admission and
  point of use.
- V128 #594 adds source **in-place stack count and type consistency**
  to those checks so metadata changed during a mapped getter cannot
  authorize native right-click.
- V129 collects both, plus this review and release-gate document,
  in a single integration on V126. The verified GitHub comparison
  V126→V128: **4 linear commits strictly ahead, zero behind, 2
  changed Java production/test files** before this document. Do
  not merge children and aggregate separately. All PRs Draft and
  `main` unchanged without explicit authorization.

## Grounded original Minecraft 1.8.9 source mapping

The existing class transformer maps:
- `Minecraft189PlayerHeldItemAccess.customMcHeldItem()` via
  original `EntityLivingBase.getEquipmentInSlot(0)`, returning a
  mapped actual `ItemStack` reference, *not* a cached metadata copy.
- `Minecraft189InventoryHotbarItemsAccess.customMcHotbarItems()` via
  `InventoryPlayer.mainInventory`, the actual native ItemStack[] field.
- `Minecraft189ItemStackAccess.customMcStackSize()` from original
  ItemStack stack size; `customMcIsFishingRod()` and
  `customMcIsPotion()` from original ItemStack Item subclasses.

This evidence was inspected in `Minecraft189ClassTransformer.java`
before the source changes. It is based on mapped types and reference
identity, not displayed item names, GUI icons, NBT assumptions,
invented server effects or an opaque cached snapshot.

## Production contract

| Source stage | Owner and item evidence |
| --- | --- |
| Original Rod selection | Native hotbar target selected by existing AutoRod module and original previous-slot/inventory lease captured |
| Original Potion selection | Native hotbar target selected by existing AutoPot module under independent source lease and the cross-module exclusive hotbar reservation |
| First source ItemStack admission | Mapped ItemStack positive count and exact `ItemFishingRod` / `ItemPotion` source class; module still enabled and same original selected-slot owner |
| V127 membership check | After metadata read, the exact inspected Java ItemStack wrapper must still occupy `InventoryPlayer.mainInventory[selected]` and match the original local player's freshly read held ItemStack |
| Getter reentry | Re-observe currentItem and original hotbar ItemStack[] **again** after the second held-item access; reject any replacement while current verification is in flight |
| V128 in-place stability | Re-read class and positive stack count **after the first membership proof**, then reconfirm membership; reject a formerly valid wrapper whose backing fields were depleted or changed to another Item class in place |
| Host owner | Original local player identity and native inventory remain the same; unknown or changed source identity fails closed |
| Last-moment original right click | Existing `HostRuntime.mayExecuteAutoRodUse` and `mayExecuteAutoPotUse` revalidate **after native AutoBlock stopUsingItem** and immediately before actual original `Minecraft.rightClickMouse()`; no GUI focus and same captured source lease |
| Native finally | If source evidence fails, skip synthetic right click/cooldown, and restore only the module-owned old player's selected hotbar slot using exact captured prior token; preserve a real new user slot |
| Success retry | A different but now-stable ItemStack of the **same valid source item class** may be used on a later independent check. No permanent rejection of normal inventory updates |

### Explicit limits

Two bounded observations are not a global atomic transaction;
another native game callback could still change source state after
the last check. These rules reduce known, deterministic reentrant
metadata races. They are *not* proof that a potion is healing, that a
fishing rod casts a projectile, or that the dedicated Minecraft server
approved an item use. The original mapper establishes item class,
stack size and wrapper membership only.

For pure old test adapters that implement only
`Minecraft189InventoryHotbarControl`, the V127 fallback verifies
two stable player held-item getter references and unchanged selected
slot. The **actual transformed original** InventoryPlayer supports
`Minecraft189InventoryHotbarItemsAccess`, so the native array
membership check is executed in the real client.

## Regression evidence

`Minecraft189VerifiedCombatItemSlotTest` was extended to use
an original-hotbar-array style test control and adversarial mutable
source ItemStacks:

1. A Rod ItemStack's type getter swaps the original selected array
   element to another valid Rod wrapper during first admission.
   The stale first wrapper must be rejected, hotbar restored, and
   a later stable Rod allowed.
2. A potion's type getter replaces its own selected array element
   with a Rod: initial potion selection rejected and restored.
3. During final Rod/Potion use admission, the type getter swaps the
   selected source array element for a new same-class wrapper.
   Deny the current action but allow a stable replacement at the
   *next independent* check.
4. A Rod type getter **depletes the same ItemStack wrapper in place**:
   initial admission rejected, original hotbar restored and fresh
   positive-count retry supported.
5. Potion count drops to zero inside mapped type getter at final
   use: no synthetic right click, lease held for actual finally;
   a later stable positive-count check may pass.
6. A Rod transforms into Potion or Potion transforms into Rod
   **in place** inside original type getter: reject even when the
   source ItemStack wrapper reference remains identical.

Existing source-native X11 Step/NoClip/Reach/CombatSlot proofs
remain mandatory, as do original UUID/target lock/rejoin,
Aura/FOV/PvP and Scaffold/world negative controls.

## Exact-head release gate and rollback

V127 exact head Foundation Windows+Ubuntu run 38988165418
**SUCCESS**; its full original Mojang suite must be separately
verified on the V127 SHA.

V128 original Foundation and pinned Mojang status must be checked
on its exact SHA; V129's own newly launched CI must be verified
independently. **Do not call V129 fully certified** until both the
Windows/Ubuntu Foundation and **entire pinned Mojang 1.8.9
graphical/dedicated original binary workflow** have completed
SUCCESS on the exact V129 commit. Targeted native GUI checks do
not replace full historical multiplayer checks. Never omit or
weaken negative tests merely because an older multiplayer
witness intermittently misses its expected marker.

Keep all child and aggregate PRs Draft and preserve certified
V126 as source rollback; **`main` unchanged** unless the user
separately authorizes a reviewed integration.
