# V206 — original AutoRod and AutoPot suspension revokes in-flight native use lease

## Confirmed Java source correctness gap
`Minecraft189AutoRodModule` and `Minecraft189AutoPotModule` implement genuine original Minecraft 1.8.9 `InventoryPlayer.currentItem` selected-hotbar owner leases. Both capture `lifecycleEpoch` when selecting a real rod/potion, and only `commitCompletedOriginalUse` after a genuine native Minecraft right click can charge cooldown for that same source lease. Their existing `onDisable/onEnable` correctly increment epoch, but their separate scheduler `suspend()` methods **only reset cooldown/remaining** without revoking `lifecycleEpoch`.

When ClickGUI, source mouse focus, combat ownership or an original native `currentItem` getter suspends a module **during a synchronous original hotbar selection or item-use callback**, old `selectedInventory`/slot/source-callback data still has a matching captured epoch. A completed old `rightClickMouse` could charge cooldown after suspension or retain old configured source-item authority. Resetting `remaining=0` alone is not equivalent to cancelling that in-flight native use lease. This is a direct source-code analysis; no native Mojang reproduction is claimed for this boundary.

## Production correction
- **AutoRod** `suspend()` increments its source `lifecycleEpoch` before resetting `remaining` and `activeCooldown`, so both `ownsSelectedSlot` and `commitCompletedOriginalUse` fail closed for an already-started original slot/action after suspension.
- **AutoPot** `suspend()` likewise increments its independent `lifecycleEpoch` and preserves its existing `reset()` of cooldown and pending eligibility.
- This does not change normal native use immediately after a valid NEW `selectSlot`: that new real original action captures the current epoch and can charge its own exact admitted cooldown if native right-click completes.
- **Crucial:** `restoreSlot` is NOT made contingent on current epoch. An original selection that was already performed must still roll back the actual selected hotbar slot in a synchronous `finally` *if* the source still points to the module-owned temporary slot; never overwrite a human-selected or another module-owned currentItem. No invented potion/rod ItemStack, packet, click, action, or cooldown.
- The existing `onDisable` increments epoch and calls `suspend()`; a second epoch advance there is intentional monotonic revocation, with no semantic effect on real cooldown.

## Deterministic tests
12 new JUnit Java source-adapter regressions: suspend during real rod/pot original in-flight completed use (both), suspend reentrantly within mapped native slot setter and restore (both), suspend in mapped original currentItem getter after selection (both), scheduler suspension not disabling module (both), duplicate suspension cannot resurrect old lease (both), user native hotbar switch must remain authoritative on final restore (both). Existing original Rod/Pot per-slot, source item identity, durability/potion metadata, new use cooldown and genuine client/server item tests remain intact.

Cumulative Draft child of V205 #671 (original held sword AutoBlock policy), V204 #670 (original TriggerBot UUID target through controller) and V200 #666 (true 3-player M419 native recovery). Parent result does not certify child. Require V206 own exact Foundation CI on Windows+Ubuntu, full pinned Mojang Minecraft 1.8.9 X11 original binary+vanilla dedicated-server 78-step run and independent same-SHA repeat before merge. PR Draft, `main` untouched.
