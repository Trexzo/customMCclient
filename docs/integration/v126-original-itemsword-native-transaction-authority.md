# V126 — original 1.8.9 AutoWeapon source Item transaction ownership

## One cumulative review on certified V121

**Certified rollback:** V121 #587, commit `4b465149cdbbddc41cffec9b6a6eebdb7428ead5`: full Windows/Ubuntu Foundation and complete pinned original Mojang 1.8.9 graphical/dedicated-server CI **SUCCESS**.

Strict child lineage:
- V122 #588 — last-moment ItemSword class, count, original Item singleton and finite base damage verified on the actual original source selected hotbar slot *before* Minecraft.clickMouse/PlayerControllerMP.attackEntity.
- V123 #589 — additional mapped source ItemStack verification *after* original InventoryPlayer.currentItem setter and before publishing a successful module hotbar lease; invalid selected swords are rolled back only if the original module still owns the slot.
- V124 #590 — cumulative source proof and integration documentation.
- V125 #591 — detects a **new selected ItemStack wrapper substituted mid-native metadata getter** during final AutoWeapon authorization; this must revoke the current action even if item type and original Item singleton look identical.
- **V126** — single cumulative review and exact-head CI gate. No separate gameplay behavior.

GitHub V121→V125 compare confirms **9 linear commits ahead, 0 behind**, touching **4 Java production/JUnit source files** plus the V124 integration document before this final V126 review record. Keep one cumulative merge plan; do not merge all child PRs and then merge V126 too. All Draft, `main` unchanged.

## Grounded original source evidence

The source transformer `Minecraft189ClassTransformer.addItemStackOriginalItemIdentityGetter` injects `customMcOriginalItemIdentity()` by directly reading the obfuscated original `ItemStack.item` field. The native inventory accessor reads original `InventoryPlayer.mainInventory` through the mapped field. Hence the evidence is real original **Item reference** and selected ItemStack, *not* a display-name heuristic.

That reference can remain stable even if the Java ItemStack wrapper is replaced; new wrapper of the same Item is valid **between** independent calls. Conversely, a reentrant getter may replace `mainInventory[selected]` before metadata verification completes without changing `currentItem`; cached old wrapper metadata then is *not* evidence of the live selected item.

## Production owner proof

| Stage | Requirement |
| --- | --- |
| Ranked sword selection | Only actual mapped ItemSword class, positive source ItemStack count, finite positive source base damage; original selected slot determines tie priority |
| After native selected-slot setter | Exact original source inventory and item/selected hotbar still match. Native callback may have changed the Item, selected slot, or module activation. Reject and safely restore only still-module-owned source slot |
| Published AutoWeapon lease | Capture old original slot, new selected slot, source inventory, source Item singleton when available, finite base damage; never publish a silently ignored native slot setter |
| Final attack authorization | Live original `InventoryPlayer.mainInventory[selected]` must contain a still-positive mapped ItemSword of original source Item singleton (when mapped) and unchanged finite source base damage |
| Getter reentry — V125 | After reading those fields, retrieve current mapped hotbar array again; require the same **source ItemStack wrapper** still occupies the selected slot. An ItemStack replaced *during verification* fails this attempt; a stable replacement wrapper is eligible at the next independent check |
| Local player/currentItem source | Original player/inventory identity, module and exact original-slot token unchanged, real selected currentItem still owned, GUI not focused |
| Attack dispatch | Existing `Minecraft189RuntimeBridge` uses `HostRuntime.mayDispatchSyntheticMelee` **before** `Minecraft.clickMouse` and `attackEntity`; the final source sword check is included only for AutoWeapon-owned temporary hotbar selections |
| Finally | Restore exact module-owned original native slot only if it still belongs to the temporary action. Preserve real user-selected different currentItem |
| No-switch / fixed CombatSlot | Retain existing vanilla attack semantics. Do not demand ranked source sword evidence for paths which never selected a sword via AutoWeapon |

### Native limitations

This is source identity and in-process safety, **not** evidence of unchanged enchantments, NBT, durability, effective damage, server inventory synchronization, original remote player hit, or a successful automatic sword combat outcome. An unmapped test-only `Item` identity of null cannot distinguish equal-looking swords by object identity; source ItemSword class/count/base damage checks remain, and the actual original transformer provides the field.

The final native getter check is a fail-closed bounded source observation, not a global atomic transaction across network threads or arbitrary future user interactions.

## Adversarial regressions

Source-host Java/JUnit tests added across V122–V125 cover:
- Correct original sword stable at selection and final dispatch; entirely new ItemStack wrapper for same actual Item is legal.
- Wrong class, depleted ItemStack, finite base damage change, different original source Item singleton in same hotbar slot: dispatch rejected.
- Native source inventory getter changes user's selected currentItem, or opens actual CustomMC GUI while item metadata is being read: veto, preserve user's original selected slot.
- Native setter itself replaces source ItemSword with non-sword or other Item singleton: selection returns -1, safe owner rollback, no zombie lease and later legitimate retry.
- **V125** source `customMcOriginalItemIdentity()` getter changes the selected **array element** to another wrapper (one case non-sword and one case same original Item singleton); current read rejected even if stale original object metadata passes. Stable next independent verification succeeds for the same actual Item.

These are tests with source-interface Java doubles. Dedicated *real original game inventory ItemStack replacement during native AutoWeapon selection* and server hit/damage have **not** been observed.

## CI and release gate

At documentation time:
- V121 exact original and Foundation **SUCCESS**.
- V122 Windows Foundation initially blocked **before Java tests** by transient Maven Central ASM HTTP 403; same-SHA failed-job rerun made Foundation Windows/Ubuntu **SUCCESS**. V122 original binary first attempt failed on an older `M425 genuine manual melee unscoped from Aura UUID` original test. Same-SHA failed-job rerun requested, not a native sword test regression finding.
- V123 full pinned Mojang original-client binary **SUCCESS** and Foundation Windows/Ubuntu **SUCCESS**.
- V124 Foundation Windows/Ubuntu **SUCCESS**, first original binary failed in older original post-rejoin UUID-B marker fixture. Same-SHA rerun requested; do **not** call V124 full original certified unless rerun completes SUCCESS.
- V125 Foundation and full original binary verdicts need checking on its exact SHA.
- **V126 exact-head** Foundation Windows/Ubuntu and full pinned Mojang 1.8.9 original graphical/dedicated workflow must each complete **SUCCESS** before cumulative certification, preserving genuine physical X11 Step/NoClip/Reach/CombatSlot acceptance plus every old UUID, rejoin, Aura/PvP, FOV, Scaffold and world negative check.

This integration is **Draft**, targets V121, and does not merge to main. V121 remains rollback anchor. Do not weaken historical tests to get a green badge.
