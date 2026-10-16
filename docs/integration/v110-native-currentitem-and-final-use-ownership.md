# V110 — actual Mojang hotbar currentItem proof and final use ownership

## Cumulative branches

**V107 #573** (certified parent) → **V108 #574** original source-native
InventoryPlayer.currentItem and physical ClickGUI acceptance → **V109 #575**
AutoRod/AutoPot final action lease revalidation → **V110** this review
branch/document.

A verified GitHub comparison from V107 to the V109 tip showed **10 commits
across nine changed paths**, strictly ahead with zero behind before this
document. Keep all child PRs Draft. To avoid duplicate source changes,
eventual authorized integration should choose either the cumulative V110 or
another single reviewed integration strategy, not independently merge both
each child and the aggregate. `main` remains unchanged.

## V108 physical GUI and original local-player inventory evidence

The opt-in `Minecraft189OriginalCombatSlotAcceptance` observes the actual
transformed Mojang 1.8.9 local player's InventoryPlayer via
`Minecraft189RuntimeBridge.playerHotbarSlot`. It waits for 12 stable
source callback samples with a consistent player instance, inventory
identity and native selected-slot baseline.

The fixture enables the normal CombatSlot module via ModuleController,
selects a **different genuine InventoryPlayer.currentItem** using the
production HostRuntime combat-slot path, asserts that native field's
readback, and restores the originally captured slot through the same
normal `finally` path. A physical X11 Right Shift into a focused client
must open CustomMC ClickGUI and veto a module-owned slot write; after
physical menu close, a fresh selection must still restore the original
slot even if the GUI preferredSlot setting changes *during* the lease.
The fixture then normally disables CombatSlot, proves native currentItem
is unchanged, and emits four independently awaited acceptance markers.

It never calls `clickMouse`, modifies the server's inventory, issues a
synthetic attack, grants items or infers a server-approved hit.

**Observed evidence on V108 #574:** Foundation Windows + Ubuntu completed
success. The focused V108 original Mojang physical X11/currentItem test
step completed success on run 38938659818. The *entire* original client
suite still needs an exact-head completed-success conclusion before V108
or V110 can be certified overall.

## V109 production time-of-check/time-of-use protection

The native combat action chain can call
`swordBlock.customMcStopUsingItem()` between initial Rod/Pot admission
and the final `minecraft.customMcRightClickMouse()`. That real
source-mapped callback may change the original selected slot, ItemStack,
enabled module or focus state before the actual right click.

Production code now checks final native authority **after** the AutoBlock
release and **immediately before** the right click:
- Same captured original inventory and previous-slot lease; actual
  `currentItem` still points at module's recorded selected slot
- Current held ItemStack is exact source-mapped rod/potion type and
  contains a positive stack count
- Module remains enabled; the original ClickGUI does not own focus
- Ownership is checked on both sides of the mapped held-item getter
  because that getter can itself reenter and mutate currentItem

On failure: issue no synthetic right click, commit no cooldown and
release the already-acquired slot through the existing owner-safe
`finally`; do not fall through into a second synthetic action on the
same tick. Preserve another user's intervening currentItem selection.

Adversarial JUnit cases cover swapped/depleted stacks, zero/negative
counts, wrong previous-slot token, reentrant user changes during
held-item retrieval, module disable, actual CustomMC host GUI toggles,
and successfully reacquiring a new lease after a rejection. These are
source-host and interface tests, **not** real server rod projectile or
potion healing proofs.

## Exact-head release conditions

1. Full Foundation Windows and Ubuntu SUCCESS on **V110 tip**.
2. Entire pinned Mojang 1.8.9 original binary graphical and dedicated
   server workflow SUCCESS on the exact V110 commit, including V94 Step,
   V96 NoClip, V103 Reach and V108 CombatSlot physical X11 checkpoints,
   followed by all historical UUID/rejoin, Aura/FOV, PvP, Scaffold and
   negative world authority checks.
3. Do not mistake a targeted green native step for a whole workflow
   success, or mocked item-use hooks for verified original server damage,
   potion effects or fishing rod casts.
4. Keep the V107 certified commit as rollback boundary; do not merge to
   `main` without explicit user authorization.

This document records evidence observed when written, not a blanket claim
of full final V110 native runtime success.
