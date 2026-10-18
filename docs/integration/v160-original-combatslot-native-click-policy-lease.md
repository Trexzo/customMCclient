# V160 — original CombatSlot selected-slot policy at the actual Minecraft attack boundary

## Parent and original CI

V160 is cumulative above V159 `fa003c5588927c4699229252d40ce5b3bbbbf9bb` (Draft PR #625), itself stacked over V158–V151. V159 Foundation is **SUCCESS Windows and Ubuntu**, but its full pinned Mojang original binary CI [39108548698](https://github.com/Trexzo/customMCclient/actions/runs/39108548698) was still in progress when this patch began. These inherited results cannot certify V160's different SHA.

Source production code inspected at that exact parent showed an authority gap: `Minecraft189CombatSlotModule.select` captured an original player's native `InventoryPlayer.currentItem` and temporarily switched to a configured slot. `Minecraft189HostRuntime.mayDispatchSyntheticMelee` subsequently validated original owner inventory/player/currentItem *but only checked that the fixed CombatSlot module was active*, not that the original `combat.combatSlot.preferredSlot` and `requirePlayerHit` policy remained the same **for that specific click** or that the module had not been disabled and reenabled. A GUI edit or synchronous mapped original setter callback could leave the previous selected slot still active and authorize a new synthetic original click under stale settings. This was distinct from AutoWeapon, which already validates selected ItemSword identity and its own live durability/ranking policy.

## Production implementation

- `Minecraft189CombatSlotModule` now captures the actual persisted preferred slot, Require Player Hit and enabled module lifecycle epoch when it acquires one original temporary native inventory selection. The epoch advances on each `onEnable` and `onDisable`.
- Selection only returns a successful native lease when the source setter really selected the desired slot **and** the original module/currentItem and the captured GUI policy/lifetime still agree. If the setter reentered settings or toggled the module, it rolls back only the exact temporary source slot it still owns; a different user-selected slot wins.
- New `ownsVerifiedSlotAtDispatch(inventory,original)` checks precise source inventory identity, currentItem index and original slot token plus captured preferred slot, Require Player Hit, enabled module lifecycle, **before and after** a mapped native currentItem getter (which may itself trigger a GUI/module callback).
- `Minecraft189HostRuntime.mayDispatchSyntheticMelee` now calls that predicate at the **actual original clickMouse/PlayerControllerMP.attackEntity** admission boundary for fixed CombatSlot. The existing AutoWeapon typed sword item, user source/hotbar and actual open ClickGUI vetoes remain mandatory; unmodified vanilla manual clicks and unrelated rod/potion modules are not routed through a forged CombatSlot proof.
- Restoration in the original finally remains independent of changed settings/module disable: it may put back the genuine original slot if *this* module's selected index still owns currentItem. It never overwrites a new user selection. A denied click cannot resurrect its original authorization.

No Minecraft player inventory, source ItemStack, server packet, target UUID, weapon type or attack damage is synthesized. Original CombatSlot setting names/defaults are not changed. The guard does not itself demonstrate a genuine original server PvP attack: real Mojang binary/dedicated server tests remain mandatory.

## Regression coverage

Four new adversarial cases in `Minecraft189CombatSlotModuleTest` with original-accessor mock objects:
1. positive exact current source selection; negative wrong inventory, previous-slot token, user-selected currentItem, edited PreferredSlot and RequirePlayerHit; invalidated click may still restore its *own* native slot;
2. disable→enable under identical settings and player cannot inherit previous click; an independent new source click may proceed;
3. synchronous original hotbar setter reentrantly edits either GUI policy or toggles module, denying stale selection and restoring only owned original slot;
4. original source currentItem getter reentrantly changes GUI setting or currentItem, denying stale dispatch and respecting actual new user-selected slot.

These tests are source Java lifecycle checks, not fabricated in-game acceptance. Parent V159 also had new tests of genuine M419 A closer than B server positioning while preserving exact native B UUID/server damage, and V158's actual original Scaffold grounding chamber. Neither older source issue is resolved merely by these CombatSlot tests.

## Release gate

Require exact-head V160 **Windows+Ubuntu Foundation** plus full original pinned Minecraft 1.8.9 binary graphical/X11 and dedicated server CI including M405/M419 genuine native target UUID and B-positive/A-zero server damage, M425 original physical ray/player hurt, original Scaffold ItemBlock, AutoRod/Pot and ClickGUI. For intermittent original tests, repeat exact SHA independently before claiming reliability. Keep PR Draft, no merge, main unchanged.
