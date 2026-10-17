# V151 — exact source ItemStack and immutable use-policy leasing

## Cumulative parent and gameplay bug

The direct source parent is V150 PR #616 commit `89d45bba33aa8ffdd05e08a51b360c0f73550bc9`, with original Minecraft 1.8.9 binary preflight **78/78 green** on first attempt and Windows/Ubuntu Foundation green. The second **same SHA** full original run was in progress when V151 branched. No modifications to V150 or main.

V137–V149 already proved genuine original fishing-hook cast/reel, splash potion effect and Survival healing, original health-based potion ranking and rod/sword durability policies. However the earlier production AutoRod/AutoPot hotbar lease primarily pinned a numeric selected source slot. Reentrant Minecraft source getters, inventory synchronization or GUI setting callbacks could replace the exact native ItemStack with a different same-type rod/potion, or change slot discovery, durability, healing priority or pitch intent between discovery and the native right-click. Matching the slot and item **class** alone is not sufficient proof that the original selected item and GUI intent still own the use attempt.

## Real production logic changed

- `Minecraft189AutoRodModule` and `Minecraft189AutoPotModule` capture all settings which control their current synthetic use as part of the actual InventoryPlayer.currentItem lease. At final dispatch a change to configured slot, auto-detection, ranking, durability protection, minimum durability, active-hook preservation, user input policy, source health threshold, Regeneration veto, healing conservation, splash aim or tick cooldown **revokes** the right-click. The changed GUI value is never ignored to spend the old item.
- Only after the *existing* original ItemStack type/count/metadata, actual hotbar membership and source player held-item checks pass, the modules bind the **exact validated original ItemStack reference** to the synchronous attempt. An inventory sync replacing the held item with a different same-type item at the identical slot fails the final `stillOwnsRodAtUse` / `stillOwnsPotionAtUse` gate; no guessed packet or server outcome.
- The native scanner separately snapshots all **selection-dependent** settings before it calls mapped ItemStack accessors. If a reentrant callback changes that intent before selection, it fails closed **without writing the hotbar**. This is in addition to postselection captured-policy checks.
- Verified source item capture and policy are cleared by the existing owner-safe `finally` / `restoreSlot`. Changing the player's own hotbar before restore still preserves the player's choice; a fresh action can re-admit the newly equipped legitimate source item.
- Defaults, persisted setting keys, ordinary original fishing hook/potion behavior, attack ownership and all previous tests remain unchanged.

## Regression matrix

New Java tests exercise a same-type original-shaped rod replaced after a positive owner select, native healing potion replaced by an equally valid different ItemStack, fresh re-admission after rollback, changed late GUI durability priority, active-hook and potion ranking/splash pitch/health threshold, and genuine-shaped source type getter altering Auto Detect *during* discovery before any native currentItem mutation. Module tests systematically vary every source-lease policy setting and require lost authorization, original player hotbar preservation and fresh valid selection.

These are **adversarial unit tests** only; they do not constitute original-game or server proof.

## Acceptance and release

The cumulative V151 PR stays **Draft**. Require V151 exact-head Windows/Ubuntu Foundation and full pinned original Minecraft 1.8.9 GUI/dedicated-server preflight, including all original V144–V150 potion/sword/rod stages and FOV/GUI/Scaffold/PvP/UUID negatives, before reporting original-game compatibility. The independent V150 reliability rerun remains attached to the unchanged V150 SHA; do not conflate its evidence with new V151.
