# V131 — Original Rod/Potion automatic hotbar item discovery

This **Draft** branch is stacked on V130 #596 at 9c9e7738e9b90394324dfc6fd51b922f95b20c38. V129 #595 is the latest fully certified rollback point. V130 Foundation Windows/Ubuntu PASSED, but complete official Mojang 1.8.9 CI was still in progress when V131 began. Do not independently merge child/aggregate PRs or merge main until the exact-head gate succeeds.

## Actual production
Two opt-in booleans, `combat.autoRod.autoDetectSlot` and `combat.autoPot.autoDetectSlot`, both default false and exposed/persisted in ClickGUI. Fixed configured-slot behavior is unchanged when disabled. With auto discovery enabled, scan the actual source-mapped `InventoryPlayer.mainInventory` `ItemStack[]` with positive count and source `ItemFishingRod` / `ItemPotion` type. Prefer a valid already-held slot, else lowest valid slot. Without the native mapped source array, fail closed. No display name heuristic.

Original currentItem must remain unchanged during getter reads; recheck selected source item array membership and type/count after getter reentry. The V130 source setter acknowledgment and V129 post-switch and final post-AutoBlock right-click gates remain mandatory. Selected slot is captured per attempt, with previous inventory/slot owner-safe finally restoration.

## Verification
New JUnit source-host tests cover default-off parity, off-slot native Rod/Potion discovery, held preference, empty/unrecognized candidates, native getter reentrant human slot change, stable retry, final item-use validation, restore and SettingRegistry persistence. Existing tests, original X11 GUI physical tests, UUID/FOV/PvP/Scaffold negative assertions are unchanged.

**Required:** Foundation Windows and Ubuntu SUCCESS, and complete pinned Mojang original 1.8.9 binary workflow SUCCESS on the exact V131 head. These host tests do *not* establish real dedicated-server fishing-rod casts or potion healing. That positive original-use proof remains outstanding.
